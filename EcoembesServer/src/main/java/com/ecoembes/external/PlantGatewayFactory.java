package com.ecoembes.external;

import java.net.URI;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PlantGatewayFactory {
	
	    private final String plasSbBaseUrl;
	    private final long timeoutMs;
	
	    public PlantGatewayFactory(
	    		@Value("${ecoembes.plants.plas-sb.base-url:http://localhost:8082}") String plasSbBaseUrl,

	    		@Value("${ecoembes.plants.timeout-ms:3000}") long timeoutMs) {
	
	        this.plasSbBaseUrl = plasSbBaseUrl;
	        this.timeoutMs = timeoutMs;
	    }
	
	    public PlantGateway forPlant(String plantId) {
	
	        if ("PL-01".equals(plantId)) {
	            return new PlasSbGateway(
	                    URI.create(plasSbBaseUrl),
	                    Duration.ofMillis(timeoutMs)
	            );
	        }
	
	        throw new ResponseStatusException(
	                HttpStatus.NOT_FOUND,
	                "No existe un adaptador para la planta " + plantId
	        );
	    }
}