package com.example.deliveryservice.services;

import com.example.deliveryservice.dto.command.CreateReviewCommand;
import com.example.deliveryservice.dto.command.UpdateReviewCommand;
import com.example.deliveryservice.entity.Order;
import com.example.deliveryservice.entity.OrderStatus;
import com.example.deliveryservice.entity.Review;
import com.example.deliveryservice.exceptions.ResourceAlreadyExistsException;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.repository.OrderRepository;
import com.example.deliveryservice.repository.ReviewRepository;
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

    public Review create(UUID orderId, CreateReviewCommand command) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.delivered) {
            throw new IllegalStateException("Review can only be left for delivered orders");
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
    public Review getByOrderId(UUID orderId) {
        return reviewRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for order: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<Review> getByRestaurantId(UUID restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId);
    }

    public Review update(UUID orderId, UpdateReviewCommand command) {
        Review review = getByOrderId(orderId);

        if (command.restaurantRating() != null) review.setRestaurantRating(command.restaurantRating());
        if (command.courierRating() != null) review.setCourierRating(command.courierRating());
        if (command.deliverySpeed() != null) review.setDeliverySpeed(command.deliverySpeed());
        if (command.comment() != null) review.setComment(command.comment());

        return reviewRepository.save(review);
    }
}
