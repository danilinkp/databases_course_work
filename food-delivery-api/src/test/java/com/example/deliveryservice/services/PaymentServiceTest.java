package com.example.deliveryservice.services;

import com.example.deliveryservice.entity.*;
import com.example.deliveryservice.exceptions.ResourceAlreadyExistsException;
import com.example.deliveryservice.repository.AdminRepository;
import com.example.deliveryservice.repository.OrderRepository;
import com.example.deliveryservice.repository.PaymentRepository;
import com.example.deliveryservice.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private PaymentService paymentService;

    private UUID customerId;
    private UUID restaurantId;
    private Order order;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        Customer customer = Customer.builder().id(customerId).build();
        Restaurant restaurant = Restaurant.builder().id(restaurantId).build();
        order = Order.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .restaurant(restaurant)
                .status(OrderStatus.confirmed)
                .totalAmount(BigDecimal.TEN)
                .build();
    }

    private CustomUserDetails customer(UUID id) {
        return new CustomUserDetails("c@e.com", "p", "customer_role", id.toString(), true);
    }

    private CustomUserDetails systemAdmin() {
        return new CustomUserDetails("a@e.com", "p", "system_admin_role", UUID.randomUUID().toString(), true);
    }

    private CustomUserDetails restaurantAdmin(UUID id) {
        return new CustomUserDetails("r@e.com", "p", "restaurant_admin_role", id.toString(), true);
    }

    @Test
    void create_byOrderCustomer_succeeds() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId(order.getId())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment payment = paymentService.create(order.getId(), PaymentMethod.card, customer(customerId));

        assertThat(payment.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.pending);
    }

    @Test
    void create_byForeignCustomer_isDenied() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.create(order.getId(), PaymentMethod.card, customer(UUID.randomUUID())))
                .isInstanceOf(AccessDeniedException.class);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void create_forCancelledOrder_isRejected() {
        order.setStatus(OrderStatus.cancelled);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.create(order.getId(), PaymentMethod.card, customer(customerId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void create_whenPaymentExists_isRejected() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId(order.getId())).thenReturn(true);

        assertThatThrownBy(() -> paymentService.create(order.getId(), PaymentMethod.card, systemAdmin()))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void getById_byOrderCustomer_succeeds() {
        Payment payment = Payment.builder().id(UUID.randomUUID()).order(order)
                .paymentStatus(PaymentStatus.pending).build();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertThat(paymentService.getById(payment.getId(), customer(customerId))).isSameAs(payment);
    }

    @Test
    void getById_byForeignCustomer_isDenied() {
        Payment payment = Payment.builder().id(UUID.randomUUID()).order(order)
                .paymentStatus(PaymentStatus.pending).build();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.getById(payment.getId(), customer(UUID.randomUUID())))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getById_byOwningRestaurantAdmin_succeeds() {
        Payment payment = Payment.builder().id(UUID.randomUUID()).order(order)
                .paymentStatus(PaymentStatus.pending).build();
        UUID adminId = UUID.randomUUID();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(adminRepository.ownsRestaurant(adminId, restaurantId)).thenReturn(true);

        assertThat(paymentService.getById(payment.getId(), restaurantAdmin(adminId))).isSameAs(payment);
    }

    @Test
    void process_byNonAdmin_isDenied() {
        assertThatThrownBy(() -> paymentService.process(UUID.randomUUID(), "tx", "gw", customer(customerId)))
                .isInstanceOf(AccessDeniedException.class);
        verify(paymentRepository, never()).findById(any());
    }

    @Test
    void process_byAdmin_movesToProcessingAndStampsTime() {
        Payment payment = Payment.builder().id(UUID.randomUUID()).order(order)
                .paymentStatus(PaymentStatus.pending).build();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.process(payment.getId(), "tx-1", "stripe", systemAdmin());

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.processing);
        assertThat(result.getProcessedAt()).isNotNull();
        assertThat(result.getExternalTransactionId()).isEqualTo("tx-1");
    }

    @Test
    void complete_fromProcessing_stampsCompletedAt() {
        Payment payment = Payment.builder().id(UUID.randomUUID()).order(order)
                .paymentStatus(PaymentStatus.processing).build();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.complete(payment.getId(), systemAdmin());

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.completed);
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    void complete_whenNotProcessing_isRejected() {
        Payment payment = Payment.builder().id(UUID.randomUUID()).order(order)
                .paymentStatus(PaymentStatus.pending).build();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.complete(payment.getId(), systemAdmin()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fail_completedPayment_isRejected() {
        Payment payment = Payment.builder().id(UUID.randomUUID()).order(order)
                .paymentStatus(PaymentStatus.completed).build();
        lenient().when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.fail(payment.getId(), "err", systemAdmin()))
                .isInstanceOf(IllegalStateException.class);
    }
}
