package gr.hua.dit.project.core.service.model;

import gr.hua.dit.project.core.model.OrderStatus;
import gr.hua.dit.project.core.model.ServiceType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CustomerOrderView(
        Long id,
        PersonView customer,
        Long restaurantId,
        String restaurantName,
        String deliveryAddress,
        List<OrderItemView> items,
        OrderStatus orderStatus,
        ServiceType serviceType,
        BigDecimal totalPrice,
        Instant createdAt
) {}
