package gr.hua.dit.project.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    @Column(name = "address_street", nullable = false, length = 100)
    private String street;

    @Column(name = "address_number", length = 10)
    private String number;

    @Column(name = "address_zip_code", length = 10)
    private String zipCode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "label")
    private String label;

    public Address() {}

    public Address(String street,
                   String number,
                   String zipCode,
                   Double latitude,
                   Double longitude,
                   String label) {
        this.street = street;
        this.number = number;
        this.zipCode = zipCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.label = label;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return street + (number != null ? " " + number : "") + ", " + zipCode;
    }
}
