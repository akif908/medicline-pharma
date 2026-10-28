package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import java.util.List;

public interface OrderService {
    Order placeOrder(Long userId, List<OrderItem> items, String deliveryAddress);
    List<Order> getUserOrders(Long userId);
    List<Order> getAllOrders();
    Order updateOrderStatus(Long orderId, String status);
}
