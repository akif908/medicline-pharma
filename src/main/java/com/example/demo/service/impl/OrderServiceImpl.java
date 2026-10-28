package com.example.demo.service.impl;

import com.example.demo.model.Notification;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.model.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.FileExportService;
import com.example.demo.service.MedicineService;
import com.example.demo.service.OrderService;
import com.example.demo.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * OrderServiceImpl — demonstrates multiple thread/concurrency patterns:
 *
 *  1. ExecutorService (Executors.newFixedThreadPool) — used internally to
 *     run stock validation tasks in parallel when an order has multiple items.
 *
 *  2. @Async("orderExecutor") — offloads post-order work (notification, logging)
 *     to the Spring-managed thread pool defined in AsyncConfig, freeing the
 *     HTTP request thread immediately after the order is persisted.
 *
 *  3. @Scheduled — a cron job that runs every night at 02:00 to clean up
 *     old read notifications, using the background task thread pool.
 *
 *  4. Future<Boolean> — each stock-check task returns a Future so the main
 *     thread can inspect results before committing.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MedicineService medicineService;
    private final NotificationRepository notificationRepository;
    private final AuditLogger auditLogger;
    private final FileExportService fileExportService;

    // Fixed thread pool for parallel item-level stock validation
    private final ExecutorService stockCheckPool = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread t = new Thread(r);
                t.setName("stock-check-" + t.getId());
                t.setDaemon(true); // does not block JVM shutdown
                return t;
            }
    );

    @Override
    @Transactional
    public Order placeOrder(Long userId, List<OrderItem> items, String deliveryAddress) {
        User user = userRepository.findById(userId).orElseThrow();

        // ── Step 1: Submit stock-check tasks in parallel via ExecutorService ──
        // Each item's stock reduction runs as a separate Callable on a worker thread,
        // maximising throughput for multi-item orders.
        List<Future<Boolean>> stockFutures = items.stream()
                .map(item -> stockCheckPool.submit(() -> {
                    System.out.printf("[%s] Checking stock for medicine %d × %d%n",
                            Thread.currentThread().getName(),
                            item.getMedicine().getId(), item.getQuantity());
                    return medicineService.reduceStock(
                            item.getMedicine().getId(), item.getQuantity());
                }))
                .toList();

        // ── Step 2: Collect results — block only until all checks are done ──
        for (int i = 0; i < stockFutures.size(); i++) {
            try {
                boolean ok = stockFutures.get(i).get(); // blocks this request thread briefly
                if (!ok) {
                    throw new RuntimeException(
                            "Insufficient stock for: " + items.get(i).getMedicine().getName());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Stock check interrupted", e);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Stock check failed", e);
            }
        }

        // ── Step 3: Build and persist the order synchronously ──
        Order order = new Order();
        order.setUser(user);
        order.setDeliveryAddress(deliveryAddress);
        order.setStatus("APPROVED");

        double total = 0;
        for (OrderItem item : items) {
            item.setOrder(order);
            item.setPrice(item.getMedicine().getPrice() * item.getQuantity());
            total += item.getPrice();
        }
        order.setItems(items);
        order.setTotalPrice(total);
        Order savedOrder = orderRepository.save(order);

        // ── Step 4: Post-order work is async — HTTP response returns immediately ──
        sendOrderNotificationAsync(savedOrder.getId(), user);

        // ── Step 5: Audit log + file receipt (async, non-blocking) ─────────────
        auditLogger.log("ORDER_PLACED",
                user.getName(),
                String.format("Order #%d | Total: $%.2f | Items: %d",
                        savedOrder.getId(), savedOrder.getTotalPrice(), items.size()));
        fileExportService.exportOrderReceiptToFile(savedOrder.getId());

        System.out.printf("[%s] Order #%d placed — post-processing handed to async thread%n",
                Thread.currentThread().getName(), savedOrder.getId());
        return savedOrder;
    }

    /**
     * Runs on the "orderExecutor" thread pool (configured in AsyncConfig).
     * Sends the user notification without blocking the HTTP response.
     */
    @Async("orderExecutor")
    public void sendOrderNotificationAsync(Long orderId, User user) {
        try {
            // Simulate slight processing delay (e.g., generating a receipt)
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Notification n = new Notification();
        n.setUser(user);
        n.setMessage("✅ Order #" + orderId + " has been confirmed and is being processed!");
        notificationRepository.save(n);
        System.out.printf("[%s] Notification saved for order #%d%n",
                Thread.currentThread().getName(), orderId);
    }

    /**
     * @Scheduled cleanup job — runs every day at 02:00 AM.
     * Deletes notifications that have been read AND are older than 30 days.
     * Uses the Spring scheduling thread (org.springframework.scheduling.* pool).
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<Notification> old = notificationRepository.findAll().stream()
                .filter(n -> n.isRead() && n.getCreatedAt() != null
                        && n.getCreatedAt().isBefore(cutoff))
                .toList();

        if (!old.isEmpty()) {
            notificationRepository.deleteAll(old);
            System.out.printf("[%s] Scheduled cleanup: removed %d old read notifications (older than %s)%n",
                    Thread.currentThread().getName(), old.size(), cutoff);
        }
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);

        if ("APPROVED".equals(status)) {
            Notification n = new Notification();
            n.setUser(savedOrder.getUser());
            n.setMessage("Your order #" + savedOrder.getId() + " has been approved!");
            notificationRepository.save(n);
        }
        return savedOrder;
    }
}
