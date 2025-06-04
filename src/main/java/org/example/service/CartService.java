package org.example.service;

import org.example.entity.CartItem;
import org.example.entity.Product;
import org.example.entity.User;
import org.example.repository.CartItemRepository;
import org.example.repository.ProductRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public void addProductToCart(String username, Long productId) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product)
                .orElse(new CartItem());
        cartItem.setProduct(product);
        cartItem.setUser(user);
        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartItemRepository.save(cartItem);
    }

    public void updateQuantity(String username, Long productId, int quantity) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        CartItem cartItem = cartItemRepository.findByUserAndProduct(user, product).orElseThrow();
        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    public void removeFromCart(String username, Long productId) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        cartItemRepository.findByUserAndProduct(user, product)
                .ifPresent(cartItemRepository::delete);
    }

    public List<CartItem> getCartItems(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return cartItemRepository.findByUser(user);
    }

    public void clearCart(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        cartItemRepository.deleteByUser(user);
    }

    public double calculateTotal(String username) {
        List<CartItem> cartItems = getCartItems(username);
        return cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

}