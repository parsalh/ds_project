package gr.hua.dit.project.core.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "menu_item"
)
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "available", nullable = false)
    private Boolean available = true;

    @Column(name = "description")
    private String description;

    //TODO image_url of menu item

    public MenuItem() {}

    public MenuItem(Long id,
                    Restaurant restaurant,
                    String name,
                    BigDecimal price,
                    Boolean available,
                    String description) {

        this.id = id;
        this.restaurant = restaurant;
        this.name = name;
        this.price = price;
        this.available = available;
        this.description = description;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
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

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "restaurant=" + restaurant +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", available=" + available +
                ", description='" + description + '\'' +
                '}';
    }
}
