package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final com.example.demo.service.MedicineService medicineService;

    public static class OrderRequest {
        private Long userId;
        private List<OrderItemRequest> items;
        private String deliveryAddress;
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public List<OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderItemRequest> items) { this.items = items; }
        public String getDeliveryAddress() { return deliveryAddress; }
        public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    }

    public static class OrderItemRequest {
        private Long medicineId;
        private int quantity;
        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    @PostMapping
    public org.springframework.http.ResponseEntity<?> placeOrder(@RequestBody OrderRequest request) {
        try {
            List<OrderItem> items = new java.util.ArrayList<>();
            for (OrderItemRequest itemReq : request.getItems()) {
                com.example.demo.model.Medicine medicine = medicineService.getMedicineById(itemReq.getMedicineId())
                        .orElseThrow(() -> new RuntimeException("Medicine not found: " + itemReq.getMedicineId()));
                
                OrderItem item = new OrderItem();
                item.setMedicine(medicine);
                item.setQuantity(itemReq.getQuantity());
                items.add(item);
            }
            Order order = orderService.placeOrder(request.getUserId(), items, request.getDeliveryAddress());
            return org.springframework.http.ResponseEntity.ok(order);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable Long userId) {
        return orderService.getUserOrders(userId);
    }

    @GetMapping("/all")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }
}
