package gr.hua.dit.project.core.service.model;

/**
 * DTO for specifying an item in a create order request.
 */
public record CreateOrderItemRequest(
        Long menuItemId,
        Integer quantity
) {}