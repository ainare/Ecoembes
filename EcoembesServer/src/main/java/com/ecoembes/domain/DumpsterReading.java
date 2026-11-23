package com.ecoembes.domain;

import java.time.LocalDate;

public record DumpsterReading(
        LocalDate date, Integer estimatedContainers, FillLevel fillLevel
) {}
