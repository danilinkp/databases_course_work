package com.example.deliveryservice.services;

import com.example.deliveryservice.dto.command.CreateOrderCommand;
import com.example.deliveryservice.entity.*;
import com.example.deliveryservice.exceptions.ResourceNotFoundException;
import com.example.deliveryservice.repository.*;
import com.example.deliveryservice.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private DishRepository dishRepository;
    @Mock private CourierRepository courierRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private PricingService pricingService;

    @InjectMocks
    private OrderService orderService;

    private UUID restaurantId;
    private Order order;

    @BeforeEach
    void setUp() {
        restaurantId = UUID.randomUUID();
        Customer customer = Customer.builder().id(UUID.randomUUID()).build();
        Restaurant restaurant = Restaurant.builder().id(restaurantId).build();
        order = Order.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .restaurant(restaurant)
                .status(OrderStatus.pending)
                .build();
    }

    private CustomUserDetails systemAdmin() {
        return new CustomUserDetails("a@e.com", "p", "system_admin_role", UUID.randomUUID().toString(), true);
    }

    private CustomUserDetails restaurantAdmin(UUID id) {
        return new CustomUserDetails("r@e.com", "p", "restaurant_admin_role", id.toString(), true);
    }

    private CustomUserDetails customer(UUID id) {
        return new CustomUserDetails("c@e.com", "p", "customer_role", id.toString(), true);
    }

    @Test
    void confirm_pendingOrder_setsStatusAndConfirmedAt() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.confirm(order.getId(), systemAdmin());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.confirmed);
        assertThat(result.getConfirmedAt()).isNotNull();
    }

    @Test
    void confirm_nonPendingOrder_isRejected() {
        order.setStatus(OrderStatus.preparing);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.confirm(order.getId(), systemAdmin()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancel_stampsCancelledAtAndFreesCourier() {
        Courier courier = Courier.builder().id(UUID.randomUUID()).isAvailable(false).build();
        order.setStatus(OrderStatus.confirmed);
        order.setCourier(courier);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.cancel(order.getId(), systemAdmin());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.cancelled);
        assertThat(result.getCancelledAt()).isNotNull();
        assertThat(courier.getIsAvailable()).isTrue();
        verify(courierRepository).save(courier);
    }

    @Test
    void cancel_deliveredOrder_isRejected() {
        order.setStatus(OrderStatus.delivered);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancel(order.getId(), systemAdmin()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void completeDelivery_fromDelivering_setsDeliveredAtAndFreesCourier() {
        Courier courier = Courier.builder().id(UUID.randomUUID()).isAvailable(false).build();
        order.setStatus(OrderStatus.delivering);
        order.setCourier(courier);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.completeDelivery(order.getId(), systemAdmin());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.delivered);
        assertThat(result.getDeliveredAt()).isNotNull();
        assertThat(courier.getIsAvailable()).isTrue();
    }

    @Test
    void getByRestaurantId_owningRestaurantAdmin_returnsOrders() {
        UUID adminId = UUID.randomUUID();
        when(adminRepository.ownsRestaurant(adminId, restaurantId)).thenReturn(true);
        when(restaurantRepository.existsById(restaurantId)).thenReturn(true);
        when(orderRepository.findByRestaurantId(restaurantId)).thenReturn(List.of(order));

        List<Order> result = orderService.getByRestaurantId(restaurantId, restaurantAdmin(adminId));

        assertThat(result).containsExactly(order);
    }

    @Test
    void getByRestaurantId_foreignRestaurantAdmin_isRejected() {
        UUID adminId = UUID.randomUUID();
        when(adminRepository.ownsRestaurant(adminId, restaurantId)).thenReturn(false);

        assertThatThrownBy(() -> orderService.getByRestaurantId(restaurantId, restaurantAdmin(adminId)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(orderRepository, never()).findByRestaurantId(any());
    }

    @Test
    void create_forForeignCustomer_isRejected() {
        UUID customerId = UUID.randomUUID();
        CreateOrderCommand command = new CreateOrderCommand(restaurantId, null, null, null, null, List.of());

        assertThatThrownBy(() -> orderService.create(customerId, command, customer(UUID.randomUUID())))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(orderRepository, never()).save(any());
    }
}
