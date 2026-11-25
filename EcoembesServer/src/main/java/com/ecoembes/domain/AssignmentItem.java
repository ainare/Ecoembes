package com.ecoembes.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "assignment_items", uniqueConstraints = @UniqueConstraint(columnNames = { "assignment_id",
		"dumpster_id" }))
public class AssignmentItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "assignment_id", nullable = false)
	private Assignment assignment;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "dumpster_id", nullable = false)
	private Dumpster dumpster;

	protected AssignmentItem() {
	}

	public AssignmentItem(Assignment assignment, Dumpster dumpster) {
		this.assignment = assignment;
		this.dumpster = dumpster;
	}

	public Long getId() {
		return id;
	}

	public Assignment getAssignment() {
		return assignment;
	}

	public Dumpster getDumpster() {
		return dumpster;
	}
}
