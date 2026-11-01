package com.ecoembes.domain;

public class Session {

    private final String token;
    private final Employee employee;

    public Session(String token, Employee employee) {
        this.token = token;
        this.employee = employee;
    }
 
    public String getToken() {
        return token;
    }

    public Employee getEmployee() {
        return employee;
    }
}