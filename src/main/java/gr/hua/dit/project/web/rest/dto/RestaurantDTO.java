package gr.hua.dit.project.web.rest.dto;

import gr.hua.dit.project.core.model.Address;
import gr.hua.dit.project.core.model.Cuisine;
import gr.hua.dit.project.core.model.OpenHour;
import gr.hua.dit.project.core.model.ServiceType;
import gr.hua.dit.project.web.rest.OpenHourDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class RestaurantDTO {

    private Long id;
    private Long ownerId;
    private String name;
    private Address addressInfo;
    private Set<Cuisine> cuisines;
    private List<String> menu;
    private BigDecimal minimumOrderAmount;
    private BigDecimal deliveryFee;
    private ServiceType serviceType;
    private List<OpenHourDTO> openHours;
    private String imageUrl;
    private boolean isOpen;

    public RestaurantDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddressInfo() {
        return addressInfo;
    }

    public void setAddressInfo(Address addressInfo) {
        this.addressInfo = addressInfo;
    }

    public Set<Cuisine> getCuisines() {
        return cuisines;
    }

    public void setCuisines(Set<Cuisine> cuisines) {
        this.cuisines = cuisines;
    }

    public List<String> getMenu() {
        return menu;
    }

    public void setMenu(List<String> menu) {
        this.menu = menu;
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

    public List<OpenHourDTO> getOpenHours() {
        return openHours;
    }

    public void setOpenHours(List<OpenHourDTO> openHours) {
        this.openHours = openHours;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}
