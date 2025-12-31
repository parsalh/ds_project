package gr.hua.dit.project.core.service;

import gr.hua.dit.project.core.model.OrderStatus;
import gr.hua.dit.project.core.service.model.CreateOrderRequest;
import gr.hua.dit.project.core.service.model.CustomerOrderView;

import java.util.Optional;

public interface CustomerOrderService {
    CustomerOrderView createOrder(CreateOrderRequest request);

    Optional<CustomerOrderView> getCustomerOrder(Long id);

    void updateOrderStatus(Long orderId, OrderStatus status);
}