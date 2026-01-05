package gr.hua.dit.project.web.ui;

import gr.hua.dit.project.core.model.*;
import gr.hua.dit.project.core.repository.PersonRepository;
import gr.hua.dit.project.core.service.CustomerOrderService;
import gr.hua.dit.project.core.service.MenuItemService;
import gr.hua.dit.project.core.service.RestaurantService;
import gr.hua.dit.project.core.service.model.CreateOrderItemRequest;
import gr.hua.dit.project.core.service.model.CreateOrderRequest;
import gr.hua.dit.project.core.security.CurrentUser;
import gr.hua.dit.project.core.security.CurrentUserProvider;
import gr.hua.dit.project.web.rest.dto.Cart;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/restaurants")
public class CustomerOrderController {

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;
    private final CustomerOrderService customerOrderService;
    private final CurrentUserProvider currentUserProvider;
    private final PersonRepository personRepository;

    public CustomerOrderController(RestaurantService restaurantService,
                                   MenuItemService menuItemService,
                                   CustomerOrderService customerOrderService,
                                   CurrentUserProvider currentUserProvider,
                                   PersonRepository personRepository) {
        this.restaurantService = restaurantService;
        this.menuItemService = menuItemService;
        this.customerOrderService = customerOrderService;
        this.currentUserProvider = currentUserProvider;
        this.personRepository = personRepository;
    }

    private Cart getCart(HttpSession session){
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null){
            cart = new Cart();
            session.setAttribute("cart",cart);
        }
        return cart;
    }

    @GetMapping("/{restaurantId}/menu")
    public String viewMenu(@PathVariable Long restaurantId,
                           Model model,
                           HttpSession session) {
        Restaurant restaurant = restaurantService.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        List<MenuItem> menuItems = menuItemService.findByRestaurantId(restaurantId);

        Map<ItemType, List<MenuItem>> groupedItems =
                menuItems.stream()
                        .filter(MenuItem::getAvailable)
                        .collect(Collectors.groupingBy(MenuItem::getType));

        Cart cart = getCart(session);

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("groupedMenuItems", groupedItems);
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cart.getTotalPrice());
        model.addAttribute("totalQuantity", cart.getTotalQuantity());

        return "restaurantMenuView";
    }

    @PostMapping("/{restaurantId}/cart/add")
    public String addToCart(@PathVariable Long restaurantId,
                            @RequestParam Long menuItemId,
                            HttpSession session){
        MenuItem item = menuItemService.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        Cart cart = getCart(session);
        cart.addItem(item);

        return "redirect:/restaurants/"+restaurantId+"/menu";
    }

    @PostMapping("/{restaurantId}/cart/remove")
    public String removeFromCart(@PathVariable Long restaurantId,
                                 @RequestParam Long menuItemId,
                                 HttpSession session){
        Cart cart = getCart(session);
        cart.removeItem(menuItemId);

        return "redirect:/restaurants/"+restaurantId+"/menu";
    }

    @GetMapping("/{restaurantId}/order/finalize")
    public String finalizeOrder(@PathVariable Long restaurantId,
                                Model model,
                                HttpSession session) {

        CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        Restaurant restaurant = restaurantService.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        Person customer = personRepository.findById(currentUser.id())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = getCart(session);
//        if (cart.getTotalQuantity() == 0){
//            return "redirect:/restaurants/"+restaurantId+"/menu";
//        }

        BigDecimal deliveryFee = restaurant.getDeliveryFee() != null ? restaurant.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal cartTotal = cart.getTotalPrice() != null ? cart.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal grandTotal = cartTotal.add(deliveryFee);

        model.addAttribute("restaurant", restaurant);
        model.addAttribute("user", customer);
        model.addAttribute("cart", cart);
        model.addAttribute("grandTotal", grandTotal);

        return "customerFinalizeOrder";
    }

    @PostMapping("/{restaurantId}/order")
    public String placeOrder(@PathVariable Long restaurantId,
                             @RequestParam(value = "deliveryAddress", required = false) String deliveryAddress,
                             @RequestParam(value = "serviceType", defaultValue = "DELIVERY") String serviceTypeStr,
                             @ModelAttribute OrderForm orderForm,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        if (orderForm.getItems() == null || orderForm.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty (No items received)");
        }

        // Μετατροπή δεδομένων
        List<CreateOrderItemRequest> itemRequests = orderForm.getItems().stream()
                .filter(i -> i.getQuantity() != null && i.getQuantity() > 0)
                .map(formItem -> new CreateOrderItemRequest(
                        formItem.getMenuItemId(),
                        formItem.getQuantity()
                ))
                .collect(Collectors.toList());

        if (itemRequests.isEmpty()) {
            throw new RuntimeException("Cart is empty (All items had 0 quantity)");
        }

        ServiceType serviceType = ServiceType.valueOf(serviceTypeStr);

        CreateOrderRequest orderRequest = new CreateOrderRequest(
                        restaurantId,
                deliveryAddress, // Προσοχή: Η σειρά ορισμάτων πρέπει να ταιριάζει με τον constructor του Record σου
                        serviceType,
                itemRequests
                );

        try {
            var view = customerOrderService.createOrder(orderRequest);

            // Καθαρισμός session
            getCart(session).clear();

            // --- ΔΙΟΡΘΩΣΗ: Redirect στο σωστό URL (/restaurants/order/{id}/track) ---
            return "redirect:/restaurants/order/" + view.id() + "/track";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/restaurants/" + restaurantId + "/order/finalize";
        }
    }

    @GetMapping("/order/{orderId}/track")
    public String trackOrderPage(@PathVariable Long orderId, Model model) {
        // Ανάκτηση της παραγγελίας
        var order = customerOrderService.getCustomerOrder(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Πέρασμα δεδομένων στο HTML
        model.addAttribute("orderId", orderId);
        model.addAttribute("order", order); // <--- ΑΥΤΟ ΕΛΕΙΠΕ και χρειάζεται για το script του καλαθιού

        return "customerFinalizeOrderTracker";
    }

    public static class OrderForm {
        private List<OrderFormItem> items = new ArrayList<>();

        public List<OrderFormItem> getItems() {
            return items;
        }

        public void setItems(List<OrderFormItem> items) {
            this.items = items;
        }
    }

    public static class OrderFormItem {
        private Long menuItemId;
        private Integer quantity;

        public Long getMenuItemId() {
            return menuItemId;
        }

        public void setMenuItemId(Long menuItemId) {
            this.menuItemId = menuItemId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

}