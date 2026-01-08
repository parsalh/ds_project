package gr.hua.dit.project.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(
        name = "menu_item",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_menu_item_restaurant_name",
                        columnNames = {"restaurant_id","name"} //trim, ignore case στο service
                )
        },
        indexes = {
                @Index(name = "idx_menu_item_restaurant", columnList = "restaurant_id")
        }
)
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnore
    private Restaurant restaurant;

    @NotBlank(message = "Name is required.")
    @Size(max = 100, message = "Name is too large (max 100)")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "Price is required.")
    @PositiveOrZero(message = "Price can't be negative.")
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "available", nullable = false)
    private Boolean available = true;

    @Size(max = 255, message = "Description is too large.")
    @Column(name = "description")
    private String description;

    @NotNull(message = "Type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ItemType type;

    @Column(name = "image_url")
    private String imageUrl;

    public MenuItem() {}

    public MenuItem(Long id,
                    Restaurant restaurant,
                    String name,
                    BigDecimal price,
                    Boolean available,
                    String description,
                    ItemType type,
                    String imageUrl) {

        this.id = id;
        this.restaurant = restaurant;
        this.name = name;
        this.price = price;
        this.available = available;
        this.description = description;
        this.type = type;
        this.imageUrl = imageUrl;
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

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "MenuItem{" +
                "restaurantId=" + restaurant.getId() +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", available=" + available +
                ", description='" + description + '\'' +
                '}';
    }
}
