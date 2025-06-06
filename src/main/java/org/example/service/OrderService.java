package org.example.service;

import org.example.entity.*;
import org.example.repository.CompanyRepository;
import org.example.repository.OrderRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository, CartService cartService, UserRepository userRepository, CompanyRepository companyRepository, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Order createOrder(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<CartItem> cartItems = cartService.getCartItems(username);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.NEW);

        Company company = cartItems.get(0).getProduct().getCompany();
        order.setCompany(company); //todo zabezpieczenie aby można było zmówić tylko z jednej firmy na raz

        for (CartItem cartItem : cartItems) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(cartItem.getProduct());
            item.setQuantity(cartItem.getQuantity());
            order.getItems().add(item);
        }

        orderRepository.save(order);
        cartService.clearCart(username);

        notificationService.sendNotification(
                username,
                "Zamówienie #" + order.getId() + " zostało złożone i oczekuje na potwierdzenie.",
                NotificationType.NEW_ORDER_SUBMITTED
        );

        Set<User> ownersToNotify = new HashSet<>();
        for (CartItem item : cartItems) {
            User owner = item.getProduct().getCompany().getOwner();
            ownersToNotify.add(owner);
        }

        for (User owner : ownersToNotify) {
            notificationService.sendNotification(
                    owner.getUsername(),
                    "Otrzymałeś nowe zamówienie #" + order.getId() + " od użytkownika " + username + ".",
                    NotificationType.NEW_ORDER_SUBMITTED
            );
        }

        return order;
    }

    public List<Order> getOrdersForUser(String username) {
        return orderRepository.findByUserUsername(username);
    }

    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        notificationService.sendNotification(
                order.getUser().getUsername(),
                "Twoje zamówienie #" + order.getId() + " zostało potwierdzone.",
                NotificationType.ORDER_CONFIRMED
        );
    }

    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.CANCELED);
        order.setCancellationReason(reason);
        orderRepository.save(order);

        notificationService.sendNotification(
                order.getUser().getUsername(),
                "Twoje zamówienie #" + order.getId() + " zostało anulowane. Powód: " + reason,
                NotificationType.ORDER_CANCELED
        );
    }

    public List<Order> getOrdersForOwner(String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername).orElseThrow();
        Company company = companyRepository.findByOwner(owner).orElseThrow();
        return orderRepository.findByCompany(company);
    }
}