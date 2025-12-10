package gr.hua.dit.project.core.service.model;

import java.math.BigDecimal;

public record OrderItemView(
        String name,
        BigDecimal price,
        Integer quantity,
        BigDecimal subtotal
) {}