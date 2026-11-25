package com.ecoembes.facade;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecoembes.domain.Dumpster;
import com.ecoembes.domain.DumpsterReading;
import com.ecoembes.dto.CreateDumpsterDTO;
import com.ecoembes.dto.DumpsterDTO;
import com.ecoembes.dto.DumpsterReadingDTO;
import com.ecoembes.dto.DumpsterStatusDTO;
import com.ecoembes.dto.DumpsterUsageDTO;
import com.ecoembes.dto.UpdateDumpsterDTO;
import com.ecoembes.dto.UpdateDumpsterResponseDTO;
import com.ecoembes.service.DumpsterAppService;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/dumpsters")
@SecurityRequirement(name = "bearerAuth")
public class DumpsterController {

	private final DumpsterAppService dumpsterAppService;

	public DumpsterController(DumpsterAppService dumpsterAppService) {
		this.dumpsterAppService = dumpsterAppService;
	}

	@ApiResponse(responseCode = "201", description = "Dumpster created")
	@ApiResponse(responseCode = "400", description = "Invalid dumpster data")
	@ApiResponse(responseCode = "401", description = "Invalid or missing token")
	@ApiResponse(responseCode = "409", description = "Dumpster ID already exists")
	@PostMapping
	public ResponseEntity<DumpsterDTO> createDumpster(@Valid @RequestBody CreateDumpsterDTO request) {
		Dumpster created = dumpsterAppService.createDumpster(new Dumpster(request.getId(), request.getAddress(),
				request.getPostalCode(), request.getInitialCapacity()));

		DumpsterDTO response = new DumpsterDTO(created.id(), created.address(), created.postalCode(),
				created.initialCapacity());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@ApiResponse(responseCode = "200", description = "Reading saved")
	@ApiResponse(responseCode = "400", description = "Invalid reading data")
	@ApiResponse(responseCode = "401", description = "Invalid or missing token")
	@ApiResponse(responseCode = "404", description = "Dumpster not found")
	@PutMapping("/{id}")
	public ResponseEntity<UpdateDumpsterResponseDTO> updateDumpster(@PathVariable("id") String id,
			@Valid @RequestBody UpdateDumpsterDTO request) {
		DumpsterReading saved = dumpsterAppService.updateDumpster(id,
				new DumpsterReading(request.getDate(), request.getEstimatedContainers(), request.getFillLevel()));

		return ResponseEntity.ok(
				new UpdateDumpsterResponseDTO(id.trim(), saved.date(), saved.estimatedContainers(), saved.fillLevel()));
	}

	@GetMapping("/{id}/usage")
	public ResponseEntity<DumpsterUsageDTO> getDumpsterUsage(@PathVariable("id") String id,
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		List<DumpsterReadingDTO> readings = dumpsterAppService.getUsage(id, startDate, endDate).stream().map(
				reading -> new DumpsterReadingDTO(reading.date(), reading.estimatedContainers(), reading.fillLevel()))
				.toList();

		return ResponseEntity.ok(new DumpsterUsageDTO(id.trim(), startDate, endDate, readings));
	}

	@GetMapping("/status")
	public ResponseEntity<DumpsterStatusDTO> getDumpstersStatus(@RequestParam("postalCode") String postalCode,
			@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		List<DumpsterStatusDTO.DumpsterInfoDTO> dumpsters = dumpsterAppService.getStatus(postalCode, date).stream()
				.map(dumpster -> toDumpsterInfoDTO(dumpster, date)).toList();

		return ResponseEntity.ok(new DumpsterStatusDTO(postalCode.trim(), date, dumpsters));
	}

	private DumpsterStatusDTO.DumpsterInfoDTO toDumpsterInfoDTO(Dumpster dumpster, LocalDate date) {
		DumpsterReading reading = dumpsterAppService.getReading(dumpster.id(), date);
		return new DumpsterStatusDTO.DumpsterInfoDTO(dumpster.id(), dumpster.address(), reading.estimatedContainers(),
				reading.fillLevel());
	}
}
