package org.example.service;

import org.example.entity.*;
import org.example.repository.CompanyRepository;
import org.example.repository.OrderRepository;
import org.example.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.TreeMap;

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
    public Order createOrder(String username, LocalDateTime createdAt) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<CartItem> cartItems = cartService.getCartItems(username);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Koszyk jest pusty");
        }

        Company company = cartItems.get(0).getProduct().getCompany();
        boolean allFromSameCompany = cartItems.stream()
                .allMatch(item -> item.getProduct().getCompany().getId().equals(company.getId()));

        if (!allFromSameCompany) {
            throw new RuntimeException("Można złożyć zamówienie tylko z jednej firmy na raz.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.NEW);
        order.setCompany(company);
        order.setPickupTime(createdAt);

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

        User owner = company.getOwner();
        notificationService.sendNotification(
                owner.getUsername(),
                "Otrzymałeś nowe zamówienie #" + order.getId() + " od użytkownika " + username + ".",
                NotificationType.NEW_ORDER_SUBMITTED
        );

        List<User> moderators = userRepository.findByCompanyAndRole(company, User.Role.MODERATOR);
        for (User moderator : moderators) {
            notificationService.sendNotification(
                    moderator.getUsername(),
                    "Twoja firma otrzymała nowe zamówienie #" + order.getId() + " od użytkownika " + username + ".",
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

    public List<Order> getOrdersForCompany(String name) {
        User user = userRepository.findByUsername(name).orElseThrow();
        if (user.getRole() == User.Role.OWNER){
            Company company = companyRepository.findByOwner(user).orElseThrow();
            return orderRepository.findByCompany(company);
        } else if (user.getRole() == User.Role.MODERATOR) {
            Company company = companyRepository.findById(user.getCompany().getId()).orElseThrow();
            return orderRepository.findByCompany(company);
        }
        throw new RuntimeException("Access denied: user is neither OWNER nor MODERATOR");
    }

    public Map<LocalDate, List<Order>> getUpcomingOrdersGroupedByDate(String username) {
        List<Order> orders = orderRepository
                .findByUser_UsernameAndPickupTimeAfterOrderByPickupTime(username, LocalDateTime.now());

        return orders.stream()
                .collect(Collectors.groupingBy(order -> order.getPickupTime().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()));
    }

    public Map<LocalDate, List<Order>> getUpcomingOrdersForCompanyGrouped(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika"));

        Company company = user.getCompany();
        if (company == null) {
            throw new IllegalStateException("Użytkownik nie ma przypisanej firmy");
        }

        List<Order> orders = orderRepository
                .findByCompanyAndPickupTimeAfterOrderByPickupTime(company, LocalDateTime.now());

        return orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getPickupTime().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()
                ));
    }

}