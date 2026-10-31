package com.ecoembes.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ecoembes.domain.Employee;
import com.ecoembes.domain.Session;

//ANE QUITA ESTE COMENTARIO ES SIMPLEMENTE PARA Q SEPAS:
// Esta clase será la encargada de gestionar las sesiones.
//Ane implementará internamente el almacenamiento en memoria, preferiblemente mediante un Map concurrente.

@Service
public class SessionService {

    public void createSession(String token, Employee employee) {

        // TODO: Ane implementará el almacenamiento
        // de la sesión en memoria.
    }

    public Optional<Session> findSessionByToken(String token) {

        // TODO: Ane implementará la búsqueda
        // de sesiones por token.

        return Optional.empty();
    }

    public void deleteSession(String token) {

        // TODO: Ane implementará la eliminación
        // de sesiones.
    }
}