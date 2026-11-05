
package com.ecoembes.domain;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Plant {

    private final String id;
    private final String name;
    private final Map<LocalDate, Double> availableCapacity;

    public Plant(String id, String name,
                 Map<LocalDate, Double> availableCapacity) {

        this.id = id;
        this.name = name;
        this.availableCapacity = new HashMap<>(availableCapacity);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getAvailableCapacity(LocalDate date) {
        return availableCapacity.get(date);
    }
}

