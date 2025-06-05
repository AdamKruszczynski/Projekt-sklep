package org.example.service;

import org.example.entity.*;
import org.example.repository.CompanyRepository;
import org.example.repository.OrderRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public OrderService(OrderRepository orderRepository, CartService cartService, UserRepository userRepository, CompanyRepository companyRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
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
    }

    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(OrderStatus.CANCELED);
        order.setCancellationReason(reason);
        orderRepository.save(order);
    }

    public List<Order> getOrdersForOwner(String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername).orElseThrow();
        Company company = companyRepository.findByOwner(owner).orElseThrow();
        return orderRepository.findByCompany(company);
    }
}