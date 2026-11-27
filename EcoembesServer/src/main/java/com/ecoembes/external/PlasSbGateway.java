package com.ecoembes.external;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import com.ecoembes.domain.AssignmentNotice;
import com.ecoembes.domain.AssignmentReceipt;
import com.ecoembes.domain.PlantCapacity;

public class PlasSbGateway implements PlantGateway {

	    private final URI baseUri;
	    private final Duration timeout;
	    private final HttpClient httpClient;
	    private final JsonMapper jsonMapper;
	
	    public PlasSbGateway(URI baseUri, Duration timeout) {
	        this.baseUri = baseUri;
	        this.timeout = timeout;
	
	        this.httpClient = HttpClient.newBuilder()
	                .connectTimeout(timeout)
	                .build();
	
	        this.jsonMapper = JsonMapper.builder()
	                .findAndAddModules()
	                .build();
	    }
	
	    @Override
	    public PlantCapacity checkCapacity(LocalDate date) {
	
	        URI uri = baseUri.resolve("/capacity?date=" + date);
	
	        HttpRequest request = HttpRequest.newBuilder().uri(uri).timeout(timeout)
	                .GET().build();
	
	        HttpResponse<String> response = send(request);
	
	        if (response.statusCode() != 200) {
	            throw plantError(response.statusCode());
	        }
	
	        try {
	            return jsonMapper.readValue(response.body(), PlantCapacity.class);
	
	        } catch (JacksonException e) {
	            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Respuesta JSON inválida de PlasSB", e);
	        }
	    }
	
	    @Override
	    public AssignmentReceipt registerAssignment(AssignmentNotice notice) {
	
	        String json;
	
	        try {
	            json = jsonMapper.writeValueAsString(notice);
	
	        } catch (JacksonException e) {
	            throw new IllegalStateException("No se pudo convertir la asignación a JSON", e);
	        }
	
	        HttpRequest request = HttpRequest.newBuilder().uri(baseUri.resolve("/assignments")).timeout(timeout)
	                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json))
	                .build();
	
	        HttpResponse<String> response = send(request);
	
	        if (response.statusCode() != 201 && response.statusCode() != 200) {
	            throw plantError(response.statusCode());
	        }
	
	        try {
	            return jsonMapper.readValue(response.body(), AssignmentReceipt.class);
	
	        } catch (JacksonException e) {
	            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
	                    "Respuesta JSON inválida de PlasSB", e);
	        }
	    }
	
	    private HttpResponse<String> send(HttpRequest request) {
	
	        try {
	            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	
	        } catch (InterruptedException e) {
	
	            Thread.currentThread().interrupt();
	
	            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Comunicación interrumpida con PL-01", e);
	
	        } catch (IOException e) {
	
	            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"No se puede contactar con PL-01", e);
	        }
	    }
	
	    private ResponseStatusException plantError(int status) {
	
	        return switch (status) {
	
	            case 400 -> new ResponseStatusException(
	                    HttpStatus.BAD_REQUEST,
	                    "Petición inválida para PL-01");
	
	            case 404 -> new ResponseStatusException(
	                    HttpStatus.NOT_FOUND,
	                    "No existe capacidad para esa fecha");
	
	            case 409 -> new ResponseStatusException(
	                    HttpStatus.CONFLICT,
	                    "PL-01 ha rechazado la asignación");
	
	            default -> new ResponseStatusException(
	                    HttpStatus.SERVICE_UNAVAILABLE,
	                    "Error de comunicación con PL-01. HTTP " + status);
	        };
	    }
}