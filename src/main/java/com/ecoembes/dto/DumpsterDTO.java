package com.ecoembes.dto;

/** Response body returned after creating a dumpster. */
public class DumpsterDTO {

    private String id;
    private String address;
    private String postalCode;
    private Integer initialCapacity;

    public DumpsterDTO() {
    }

    public DumpsterDTO(String id, String address, String postalCode,
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
