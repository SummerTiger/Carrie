package com.vending.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @NotBlank(message = "Address is required")
    @Column(name = "location_address", nullable = false)
    private String address;

    @Column(name = "location_city")
    private String city;

    @Column(name = "location_province")
    private String province;

    @Column(name = "location_postal_code")
    private String postalCode;

    @Column(name = "location_latitude")
    private Double latitude;

    @Column(name = "location_longitude")
    private Double longitude;

    @Column(name = "location_building_name")
    private String buildingName;

    @Column(name = "location_floor")
    private String floor;

    @Column(name = "location_contact_name")
    private String contactName;

    @Column(name = "location_contact_phone")
    private String contactPhone;

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null) sb.append(address);
        if (city != null) sb.append(", ").append(city);
        if (province != null) sb.append(", ").append(province);
        if (postalCode != null) sb.append(" ").append(postalCode);
        return sb.toString();
    }
}
