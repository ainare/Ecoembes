package com.ecoembes.domain;

import java.util.Objects;

public class Employee {

    private final String employeeId;
    private final String email;
    private final String password;

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