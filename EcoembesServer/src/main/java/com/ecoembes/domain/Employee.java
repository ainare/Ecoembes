package com.ecoembes.domain;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee {

	@Id
	private String employeeId;

	private String email;
	private String password;

	protected Employee() {
	}

	public Employee(String employeeId, String email, String password) {
		this.employeeId = employeeId;
		this.email = email;
		this.password = password;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public String getEmail() {
		return email;
	}

	public boolean matchesPassword(String password) {
		return Objects.equals(this.password, password);
	}
}
