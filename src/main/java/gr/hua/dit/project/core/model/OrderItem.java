package gr.hua.dit.project.core.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "order_item"
)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    //TODO image_url σαν/απο το MenuItem

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    public OrderItem() {}

    public OrderItem(Long id,
                     String name,
                     BigDecimal price,
                     Integer quantity,
                     BigDecimal subtotal,
                     CustomerOrder order,
                     MenuItem menuItem) {

        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
        this.order = order;
        this.menuItem = menuItem;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public CustomerOrder getCustomerOrder() {
        return order;
    }

    public void setCustomerOrder(CustomerOrder order) {
        this.order = order;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                ", order=" + (order != null ? order.getId() : null) +
                '}';
    }
}
