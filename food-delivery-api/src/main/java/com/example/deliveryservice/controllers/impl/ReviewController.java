package com.example.deliveryservice.controllers.impl;

import com.example.deliveryservice.dto.command.CreateReviewCommand;
import com.example.deliveryservice.dto.command.UpdateReviewCommand;
import com.example.deliveryservice.dto.response.ReviewResponse;
import com.example.deliveryservice.security.CustomUserDetails;
import com.example.deliveryservice.services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Отзывы", description = """
        Endpoints для создания и управления отзывами о ресторанах. \
        Клиенты могут оставлять отзывы после получения заказа.
        """)
@SecurityRequirement(name = "bearer-auth")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/order/{orderId}")
    @Operation(
        summary = "Создать отзыв",
        description = "Оценка и отзыв о completed заказе. Клиент может поставить рейтинг (1-5 звезд) и написать комментарий. Отзыв влияет на рейтинг ресторана. Доступно только клиентам."
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@PathVariable UUID orderId,
                                 @RequestBody @Valid CreateReviewCommand command,
                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        // Only customers can create reviews
        if (!"customer_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Only customers can create reviews");
        }
        return ReviewResponse.fromEntity(reviewService.create(orderId, command, currentUser));
    }

    @GetMapping("/order/{orderId}")
    @Operation(
        summary = "Получить отзыв о заказе",
        description = "Возвращает отзыв для конкретного заказа если он был оставлен. Доступно всем пользователям."
    )
    public ReviewResponse getByOrder(@PathVariable UUID orderId,
                                     @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ReviewResponse.fromEntity(reviewService.getByOrderId(orderId, currentUser));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(
        summary = "Получить отзывы ресторана",
        description = "Возвращает все отзывы о ресторана с их рейтингами и комментариями. Используется для анализа качества обслуживания. Доступно администраторам ресторана и системным администраторам."
    )
    public List<ReviewResponse> getByRestaurant(@PathVariable UUID restaurantId,
                                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        // Only restaurant admins, system admins can view reviews for their restaurant
        if (!"restaurant_admin_role".equals(currentUser.getRole())
                && !"system_admin_role".equals(currentUser.getRole())) {
            throw new AccessDeniedException("Access denied");
        }
        return reviewService.getByRestaurantId(restaurantId).stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }

    @PatchMapping("/order/{orderId}")
    @Operation(
        summary = "Обновить отзыв",
        description = "Редактирование своего отзыва: изменение рейтинга или комментария. Доступно автору отзыва."
    )
    public ReviewResponse update(@PathVariable UUID orderId,
                                 @RequestBody @Valid UpdateReviewCommand command,
                                 @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ReviewResponse.fromEntity(reviewService.update(orderId, command, currentUser));
    }
}