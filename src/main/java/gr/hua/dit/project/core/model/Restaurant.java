package gr.hua.dit.project.core.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "address", nullable = false, length = 100)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> menu = new ArrayList<>();

    @Column(name = "minimum_order_amount", nullable = false)
    private BigDecimal minimumOrderAmount;

    @Column(name = "delivery_fee", nullable = false)
    private BigDecimal deliveryFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 10)
    private ServiceType serviceType;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpenHour> openHours = new ArrayList<>();

    public Restaurant() {}

    public Restaurant(Person owner,
                      String name,
                      String address,
                      Double latitude,
                      Double longitude,
                      List<MenuItem> menu,
                      BigDecimal minimumOrderAmount,
                      BigDecimal deliveryFee,
                      ServiceType serviceType,
                      List<OpenHour> openHours) {

        this.owner = owner;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.menu = (menu != null ? menu : new ArrayList<>());
        this.minimumOrderAmount = minimumOrderAmount;
        this.deliveryFee = deliveryFee;
        this.serviceType = serviceType;
        this.openHours = (openHours != null ? openHours : new ArrayList<>());

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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

    @Override
    public String toString() {
        return "Restaurant{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", serviceType=" + serviceType +
                '}';
    }

}
