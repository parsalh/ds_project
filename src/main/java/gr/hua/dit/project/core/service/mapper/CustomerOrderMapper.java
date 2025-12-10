package gr.hua.dit.project.core.service.mapper;

import gr.hua.dit.project.core.model.CustomerOrder;
import gr.hua.dit.project.core.model.OrderItem;
import gr.hua.dit.project.core.service.model.CustomerOrderView;
import gr.hua.dit.project.core.service.model.OrderItemView;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerOrderMapper {

    private final PersonMapper personMapper;

    public CustomerOrderMapper(PersonMapper personMapper) {
        this.personMapper = personMapper;
    }

    public CustomerOrderView toView(final CustomerOrder order) {
        if (order == null) {
            return null;
        }

        final List<OrderItemView> items;
        if (order.getOrderItems() != null) {
            items = order.getOrderItems().stream()
                    .map(this::toItemView)
                    .collect(Collectors.toList());
        } else {
            items = Collections.emptyList();
        }

        return new CustomerOrderView(
                order.getId(),
                personMapper.convertPersonToPersonView(order.getCustomer()),
                order.getRestaurant() != null ? order.getRestaurant().getId() : null,
                order.getRestaurant() != null ? order.getRestaurant().getName() : null,
                order.getDeliveryAddress(),
                items,
                order.getOrderStatus(),
                order.getServiceType(),
                order.getTotalPrice(),
                order.getCreatedAt()
        );
    }

    private OrderItemView toItemView(final OrderItem item) {
        if (item == null) {
            return null;
        }
        return new OrderItemView(
                item.getName(),
                item.getPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}