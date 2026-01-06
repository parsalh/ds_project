package gr.hua.dit.project.core.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "restaurant",
        indexes = {
                @Index(name = "idx_service_type", columnList = "service_type"),
                @Index(name = "idx_restaurant_owner", columnList = "owner_id")
        }
)
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Person owner;

    @NotBlank(message = "Name is required.")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Valid
    @Embedded
    private Address addressInfo = new Address();

    @NotEmpty(message = "Choose at least one cuisine.")
    @ElementCollection(targetClass = Cuisine.class)
    @CollectionTable(
            name = "restaurant_cuisines",
            joinColumns = @JoinColumn(name = "restaurant_id")
    )
    @Enumerated(EnumType.STRING)
    @Column
    private Set<Cuisine> cuisines = new HashSet<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> menu = new ArrayList<>();

    @NotNull(message = "Minimum order amount is required.")
    @PositiveOrZero(message = "Minimum order amount can't be negative.")
    @Column(name = "minimum_order_amount", nullable = false)
    private BigDecimal minimumOrderAmount;

    @NotNull(message = "Delivery fee is required.")
    @PositiveOrZero(message = "Delivery fee can't be negative.")
    @Column(name = "delivery_fee", nullable = false)
    private BigDecimal deliveryFee;

    @NotNull(message = "Service type is required.")
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 10)
    private ServiceType serviceType;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpenHour> openHours = new ArrayList<>();

    @Column(name = "image_url")
    private String imageUrl;

    public Restaurant() {}

    public Restaurant(Long id,
                      Person owner,
                      String name,
                      Address addressInfo,
                      Set<Cuisine> cuisines,
                      List<MenuItem> menu,
                      BigDecimal minimumOrderAmount,
                      BigDecimal deliveryFee,
                      ServiceType serviceType,
                      List<OpenHour> openHours,
                      String imageUrl) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.addressInfo = addressInfo;
        this.cuisines = cuisines;
        this.menu = menu;
        this.minimumOrderAmount = minimumOrderAmount;
        this.deliveryFee = deliveryFee;
        this.serviceType = serviceType;
        this.openHours = openHours;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public void setMinimumOrderAmount(BigDecimal minimumOrderAmount) {
        this.minimumOrderAmount = minimumOrderAmount;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public List<OpenHour> getOpenHours() {
        return openHours;
    }

    public void setOpenHours(List<OpenHour> openHours) {
        this.openHours = openHours;
    }

    public List<MenuItem> getMenu() {
        return menu;
    }
    public void setMenu(List<MenuItem> menu) {
        this.menu = menu;
    }

    public Set<Cuisine> getCuisines() {
        return cuisines;
    }

    public void setCuisines(Set<Cuisine> cuisines) {
        this.cuisines = cuisines;
    }

    public Address getAddressInfo() {
        return addressInfo;
    }

    public void setAddressInfo(Address addressInfo) {
        this.addressInfo = addressInfo;
    }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isOpen() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek currentDayJava = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();

        for (OpenHour oh : this.openHours) {
            if (oh.getDayOfWeek() != null &&
                    oh.getDayOfWeek().name().equals(currentDayJava.name())) {

                if (oh.getOpenTime() != null && oh.getCloseTime() != null) {
                    LocalTime open = oh.getOpenTime();
                    LocalTime close = oh.getCloseTime();

                    if (open.isBefore(close)) {
                        if (currentTime.isAfter(open) && currentTime.isBefore(close)) {
                            return true;
                        }
                    }
                    else {
                        if (currentTime.isAfter(open) || currentTime.isBefore(close)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "name='" + name + '\'' +
                ", address='" + addressInfo + '\'' +
                ", cuisines=" + cuisines +
                ", serviceType=" + serviceType +
                '}';
    }
}
