package com.ecoembes.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecoembes.domain.Plant;

public interface PlantRepository extends JpaRepository<Plant, String> {

}
