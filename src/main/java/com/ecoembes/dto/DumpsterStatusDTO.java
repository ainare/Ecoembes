
package com.ecoembes.dto;

import java.time.LocalDate;
import java.util.List;

public class DumpsterStatusDTO {

    private String postalCode;
    private LocalDate date;
    private List<DumpsterInfoDTO> dumpsters;

    public DumpsterStatusDTO() {
    }

    public DumpsterStatusDTO(String postalCode, LocalDate date, List<DumpsterInfoDTO> dumpsters) {
        this.postalCode = postalCode;
        this.date = date;
        this.dumpsters = dumpsters;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<DumpsterInfoDTO> getDumpsters() {
        return dumpsters;
    }

    public void setDumpsters(List<DumpsterInfoDTO> dumpsters) {
        this.dumpsters = dumpsters;
    }

    public static class DumpsterInfoDTO {

        private String id;
        private String address;
        private int estimatedContainers;
        private FillLevel fillLevel;

        public DumpsterInfoDTO() {
        }

        public DumpsterInfoDTO(String id, String address,
                int estimatedContainers, FillLevel fillLevel) {
            this.id = id;
            this.address = address;
            this.estimatedContainers = estimatedContainers;
            this.fillLevel = fillLevel;
        }

        public String getId() {
            return id;
        }

        public String getAddress() {
            return address;
        }

        public int getEstimatedContainers() {
            return estimatedContainers;
        }

        public FillLevel getFillLevel() {
            return fillLevel;
        }
    }
}
