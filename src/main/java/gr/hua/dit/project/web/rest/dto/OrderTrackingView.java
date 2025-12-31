package gr.hua.dit.project.web.rest.dto;

import gr.hua.dit.project.core.model.OrderStatus;

public record OrderTrackingView(
        OrderStatus status,
        Integer etaMins,
        double[] restaurantLocation,
        double[] customerLocation
) {}
