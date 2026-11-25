package com.ecoembes.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecoembes.domain.Dumpster;

public interface DumpsterRepository extends JpaRepository<Dumpster, String> {

	List<Dumpster> findByPostalCode(String postalCode);

}
