package com.ecoembes.domain;

import java.time.LocalDate;
import java.util.List;

public record Assignment(
        String assignmentId,
        Plant plant,
        Employee employee,
        LocalDate date,
        List<Dumpster> dumpsters,
        int totalPackagingContainers
) {
    public Assignment {
        dumpsters = List.copyOf(dumpsters);
    }

    public int totalDumpsters() {
        return dumpsters.size();
    }
}
