package gr.hua.dit.project.core.service.model;

import java.util.List;

public record CreateOrderRequest(
        Long restaurantId,
        String deliveryAddress,
        List<CreateOrderItemRequest> items
) {}