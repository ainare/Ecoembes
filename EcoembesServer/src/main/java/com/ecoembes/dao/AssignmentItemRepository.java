package com.ecoembes.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecoembes.domain.AssignmentItem;

public interface AssignmentItemRepository extends JpaRepository<AssignmentItem, Long> {

}
