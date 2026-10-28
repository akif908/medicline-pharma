package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final OrderService orderService;

    @Data
    public static class StatPeriod {
        private double revenue;
        private double profit;
        private int ordersCount;
    }

    @Data
    public static class TopMedicineStat {
        private String medicineName;
        private int quantitySold;
        private double revenue;
    }

    @Data
    public static class DashboardStats {
        private StatPeriod weekly;
        private StatPeriod monthly;
        private StatPeriod yearly;
        private List<TopMedicineStat> topSelling;
    }

    @GetMapping("/stats")
    public DashboardStats getStats() {
        List<Order> allOrders = orderService.getAllOrders();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime oneWeekAgo = now.minusWeeks(1);
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        LocalDateTime oneYearAgo = now.minusYears(1);

        StatPeriod weekly = new StatPeriod();
        StatPeriod monthly = new StatPeriod();
        StatPeriod yearly = new StatPeriod();

        Map<String, TopMedicineStat> medicineStatsMap = new HashMap<>();

        for (Order order : allOrders) {
            boolean isApproved = "APPROVED".equalsIgnoreCase(order.getStatus()) || "DELIVERED".equalsIgnoreCase(order.getStatus());
            if (!isApproved) continue;

            LocalDateTime oTime = order.getTimestamp();
            if (oTime == null) oTime = LocalDateTime.now();

            double revenue = order.getTotalPrice();
            double profit = revenue * 0.30; // 30% profit margin

            // Weekly
            if (oTime.isAfter(oneWeekAgo)) {
                weekly.setRevenue(weekly.getRevenue() + revenue);
                weekly.setProfit(weekly.getProfit() + profit);
                weekly.setOrdersCount(weekly.getOrdersCount() + 1);
            }

            // Monthly
            if (oTime.isAfter(oneMonthAgo)) {
                monthly.setRevenue(monthly.getRevenue() + revenue);
                monthly.setProfit(monthly.getProfit() + profit);
                monthly.setOrdersCount(monthly.getOrdersCount() + 1);
            }

            // Yearly
            if (oTime.isAfter(oneYearAgo)) {
                yearly.setRevenue(yearly.getRevenue() + revenue);
                yearly.setProfit(yearly.getProfit() + profit);
                yearly.setOrdersCount(yearly.getOrdersCount() + 1);
            }

            // Medicine aggregated stats
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getMedicine() == null) continue;
                    String medName = item.getMedicine().getName();
                    TopMedicineStat medStat = medicineStatsMap.computeIfAbsent(medName, k -> {
                        TopMedicineStat stat = new TopMedicineStat();
                        stat.setMedicineName(medName);
                        return stat;
                    });
                    medStat.setQuantitySold(medStat.getQuantitySold() + item.getQuantity());
                    medStat.setRevenue(medStat.getRevenue() + item.getPrice());
                }
            }
        }

        List<TopMedicineStat> topSelling = medicineStatsMap.values().stream()
                .sorted(Comparator.comparingInt(TopMedicineStat::getQuantitySold).reversed())
                .limit(5)
                .collect(Collectors.toList());

        DashboardStats stats = new DashboardStats();
        stats.setWeekly(weekly);
        stats.setMonthly(monthly);
        stats.setYearly(yearly);
        stats.setTopSelling(topSelling);

        return stats;
    }
}
