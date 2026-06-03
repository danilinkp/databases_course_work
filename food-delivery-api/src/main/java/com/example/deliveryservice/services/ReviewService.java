package com.example.deliveryservice.services;

import com.example.deliveryservice.dto.command.CreateReviewCommand;
import com.example.deliveryservice.dto.command.UpdateReviewCommand;
import com.example.deliveryservice.entity.Customer;
import com.example.deliveryservice.entity.Order;
import com.example.deliveryservice.entity.OrderStatus;
import com.example.deliveryservice.entity.Review;
import com.example.deliveryservice.exceptions.ResourceAlreadyExistsException;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.repository.CustomerRepository;
import com.example.deliveryservice.repository.OrderRepository;
import com.example.deliveryservice.repository.ReviewRepository;
import com.example.deliveryservice.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public Review create(UUID orderId, CreateReviewCommand command, CustomUserDetails currentUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        // Verify the review belongs to a delivered order
        if (order.getStatus() != OrderStatus.delivered) {
            throw new IllegalStateException("Review can only be left for delivered orders");
        }

        // Verify that the order belongs to the current customer
        Customer currentCustomer = customerRepository.findById(UUID.fromString(currentUser.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (!order.getCustomer().getId().equals(currentCustomer.getId())) {
            throw new SecurityException("You can only review your own orders");
        }

        if (reviewRepository.existsByOrderId(orderId)) {
            throw new ResourceAlreadyExistsException("Review already exists for this order");
        }

        Review review = Review.builder()
                .order(order)
                .restaurantRating(command.restaurantRating())
                .courierRating(command.courierRating())
                .deliverySpeed(command.deliverySpeed())
                .comment(command.comment())
                .build();

        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public Review getByOrderId(UUID orderId, CustomUserDetails currentUser) {
        Review review = reviewRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for order: " + orderId));

        // Verify access
        if (!verifyAccess(review, orderId, currentUser)) {
            throw new SecurityException("Access denied");
        }
        return review;
    }

    @Transactional(readOnly = true)
    public List<Review> getByRestaurantId(UUID restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId);
    }

    public Review update(UUID orderId, UpdateReviewCommand command, CustomUserDetails currentUser) {
        Review review = getByOrderId(orderId, currentUser);

        if (command.restaurantRating() != null) review.setRestaurantRating(command.restaurantRating());
        if (command.courierRating() != null) review.setCourierRating(command.courierRating());
        if (command.deliverySpeed() != null) review.setDeliverySpeed(command.deliverySpeed());
        if (command.comment() != null) review.setComment(command.comment());

        return reviewRepository.save(review);
    }

    private boolean verifyAccess(Review review, UUID orderId, CustomUserDetails currentUser) {
        String role = currentUser.getRole();
        UUID currentUserId = UUID.fromString(currentUser.getId());

        // System admins can access any review
        if ("system_admin_role".equals(role)) {
            return true;
        }

        // Customers can access their own reviews
        Order order = review.getOrder();
        if ("customer_role".equals(role) && order.getCustomer().getId().equals(currentUserId)) {
            return true;
        }

        return false;
    }
}
