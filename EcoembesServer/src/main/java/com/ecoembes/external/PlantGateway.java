package com.ecoembes.external;

import java.time.LocalDate;

import com.ecoembes.domain.AssignmentNotice;
import com.ecoembes.domain.AssignmentReceipt;
import com.ecoembes.domain.PlantCapacity;

public interface PlantGateway {

    PlantCapacity checkCapacity(LocalDate date);

    AssignmentReceipt registerAssignment(AssignmentNotice notice);
}