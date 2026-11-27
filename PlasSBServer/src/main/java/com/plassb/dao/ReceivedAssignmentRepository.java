package com.plassb.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.plassb.domain.ReceivedAssignment;

public interface ReceivedAssignmentRepository extends JpaRepository<ReceivedAssignment, String> {

}