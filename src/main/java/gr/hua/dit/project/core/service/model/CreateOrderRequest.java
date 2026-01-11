package gr.hua.dit.project.core.service.model;

import gr.hua.dit.project.core.model.ServiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(

        @NotNull(message = "Restaurant ID is required.")
        Long restaurantId,
        String deliveryAddress,

        @NotNull(message = "Service type is required")
        ServiceType serviceType,

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<CreateOrderItemRequest> items
) {}