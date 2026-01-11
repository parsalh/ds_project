package gr.hua.dit.project.core.service.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for specifying an item in a create order request.
 */
public record CreateOrderItemRequest(
        @NotNull(message = "Menu Item ID is required")
        Long menuItemId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {}