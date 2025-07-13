package org.example.service;

import org.example.entity.*;
import org.example.repository.CartItemRepository;
import org.example.repository.ProductRepository;
import org.example.repository.SavedCartRepository;
import org.example.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SavedCartRepository savedCartRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository, SavedCartRepository savedCartRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.savedCartRepository = savedCartRepository;
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

    @Transactional
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

    public void saveCurrentCart(String name, String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<CartItem> currentCartItems = cartItemRepository.findByUser(user);

        SavedCart savedCart = new SavedCart();
        savedCart.setName(name);
        savedCart.setUser(user);

        for (CartItem item : currentCartItems) {
            SavedCartItem savedItem = new SavedCartItem();
            savedItem.setProduct(item.getProduct());
            savedItem.setQuantity(item.getQuantity());
            savedItem.setSavedCart(savedCart);
            savedCart.getItems().add(savedItem);
        }

        savedCartRepository.save(savedCart);
    }

    @Transactional
    public void loadSavedCart(Long cartId, String username) throws AccessDeniedException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nie znaleziono użytkownika: " + username));

        SavedCart savedCart = savedCartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zapisanego koszyka o ID: " + cartId));

        if (!savedCart.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Nie masz dostępu do tego koszyka.");
        }

        cartItemRepository.deleteByUser(user);

        for (SavedCartItem savedItem : savedCart.getItems()) {
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(savedItem.getProduct());
            cartItem.setQuantity(savedItem.getQuantity());
            cartItemRepository.save(cartItem);
        }
    }

//    public void renameSavedCart(Long cartId, String newName, String username) {
//        SavedCart cart = null;
//        try {
//            cart = savedCartRepository.findByIdAndUserUsername(cartId, username)
//                    .orElseThrow(() -> new AccessDeniedException("Brak dostępu do koszyka"));
//        } catch (AccessDeniedException e) {
//            throw new RuntimeException(e);
//        }
//
//        cart.setName(newName);
//        savedCartRepository.save(cart);
//    }
//
//    public List<SavedCart> getSavedCarts(String username) {
//        return savedCartRepository.findAllByUserUsername(username);
//    }

//    @Transactional
//    public void removeProductFromSavedCart(Long cartId, Long productId, String username) {
//        SavedCart savedCart = null;
//        try {
//            savedCart = savedCartRepository.findByIdAndUserUsername(cartId, username)
//                    .orElseThrow(() -> new AccessDeniedException("Brak dostępu do koszyka"));
//        } catch (AccessDeniedException e) {
//            throw new RuntimeException(e);
//        }
//
//        savedCart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
//
//        savedCartRepository.save(savedCart);
//    }

    @Transactional
    public void renameSavedCart(Long cartId, String newName, String username) {
        SavedCart cart = null;
        try {
            cart = savedCartRepository.findByIdAndUserUsername(cartId, username)
                    .orElseThrow(() -> new AccessDeniedException("Brak dostępu do koszyka."));
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
        cart.setName(newName);
        savedCartRepository.save(cart);
    }

    @Transactional
    public void deleteSavedCart(Long cartId, String username) {
        SavedCart cart = null;
        try {
            cart = savedCartRepository.findByIdAndUserUsername(cartId, username)
                    .orElseThrow(() -> new AccessDeniedException("Brak dostępu do koszyka."));
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
        savedCartRepository.delete(cart);
    }

    @Transactional
    public void removeProductFromSavedCart(Long cartId, Long productId, String username) {
        SavedCart cart = null;
        try {
            cart = savedCartRepository.findByIdAndUserUsername(cartId, username)
                    .orElseThrow(() -> new AccessDeniedException("Brak dostępu do koszyka."));
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        savedCartRepository.save(cart);
    }

    public List<SavedCart> getSavedCarts(String username) {
        return savedCartRepository.findAllByUserUsername(username);
    }


}