package com.ecoembes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Request body for POST /dumpsters. */
public class CreateDumpsterDTO {

    @NotBlank(message = "Dumpster id is required")
    private String id;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Postal code is required")
    private String postalCode;

    @NotNull(message = "Initial capacity is required")
    @Positive(message = "Initial capacity must be greater than zero")
    private Integer initialCapacity;

    public CreateDumpsterDTO() {
    }

    public CreateDumpsterDTO(String id, String address, String postalCode,
                             Integer initialCapacity) {
        this.id = id;
        this.address = address;
        this.postalCode = postalCode;
        this.initialCapacity = initialCapacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Integer getInitialCapacity() {
        return initialCapacity;
    }

    public void setInitialCapacity(Integer initialCapacity) {
        this.initialCapacity = initialCapacity;
    }
}
