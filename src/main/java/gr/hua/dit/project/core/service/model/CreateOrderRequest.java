package gr.hua.dit.project.core.service.model;

import gr.hua.dit.project.core.model.ServiceType;
import java.util.List;

public record CreateOrderRequest(
        Long restaurantId,
        String deliveryAddress,
        ServiceType serviceType,
        List<CreateOrderItemRequest> items
) {}