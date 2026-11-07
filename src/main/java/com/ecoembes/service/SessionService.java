package com.ecoembes.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.ecoembes.domain.Employee;
import com.ecoembes.domain.Session;

@Service
public class SessionService {

    
    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    public void createSession(String token, Employee employee) {
        if (token != null && employee != null) {
            Session session = new Session(token, employee);
            activeSessions.put(token, session);
        }
    }

    public Optional<Session> findSessionByToken(String token) {
        if (token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(activeSessions.get(token));
    }

    public void deleteSession(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }
}