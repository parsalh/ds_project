package gr.hua.dit.project.web.rest;

import gr.hua.dit.project.core.service.CustomerOrderService;
import gr.hua.dit.project.core.service.model.CreateOrderRequest;
import gr.hua.dit.project.core.service.model.CustomerOrderView;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderRestController {

    private final CustomerOrderService customerOrderService;

    public OrderRestController(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @PostMapping
    public ResponseEntity<CustomerOrderView> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest){
        CustomerOrderView newOrder = customerOrderService.createOrder(createOrderRequest);
        return ResponseEntity.ok(newOrder);
    }

    @GetMapping
    public ResponseEntity<List<CustomerOrderView>> getMyOrders(){
        return ResponseEntity.ok(customerOrderService.getMyOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrderView> getOrder(@PathVariable Long id){
        return customerOrderService.getCustomerOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
