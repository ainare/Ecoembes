package com.ecoembes.facade;

import java.time.LocalDate;

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

import com.ecoembes.dto.CreateDumpsterDTO;
import com.ecoembes.dto.DumpsterDTO;
import com.ecoembes.dto.DumpsterUsageDTO;
import com.ecoembes.dto.UpdateDumpsterDTO;
import com.ecoembes.dto.UpdateDumpsterResponseDTO;
import com.ecoembes.service.DumpsterAppService;
import com.ecoembes.dto.DumpsterStatusDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/dumpsters")
public class DumpsterController {

	private final DumpsterAppService dumpsterAppService;

	public DumpsterController(DumpsterAppService dumpsterAppService) {
		this.dumpsterAppService = dumpsterAppService;
	}

	/**
	 * POST /dumpsters - register a new dumpster.
	 */
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Dumpster created successfully")
	@PostMapping
	public ResponseEntity<DumpsterDTO> createDumpster(@Valid @RequestBody CreateDumpsterDTO request) {

		DumpsterDTO created = dumpsterAppService.createDumpster(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	/**
	 * PUT /dumpsters/{id} - record or replace a daily sensor reading.
	 */
	@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dumpster reading updated successfully")
	@PutMapping("/{id}")
	public ResponseEntity<UpdateDumpsterResponseDTO> updateDumpster(@PathVariable String id,
			@Valid @RequestBody UpdateDumpsterDTO request) {

		UpdateDumpsterResponseDTO updated = dumpsterAppService.updateDumpster(id, request);

		return ResponseEntity.ok(updated);
	}

	@GetMapping("/{id}/usage")
	public ResponseEntity<DumpsterUsageDTO> getDumpsterUsage(@PathVariable String id,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		DumpsterUsageDTO result = dumpsterAppService.getDumpsterUsage(id, startDate, endDate);

		return ResponseEntity.ok(result);
	}

	@GetMapping("/status")
	public ResponseEntity<DumpsterStatusDTO> getDumpstersStatus(@RequestParam String postalCode,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		DumpsterStatusDTO result = dumpsterAppService.getDumpstersStatus(postalCode, date);

		return ResponseEntity.ok(result);
	}

}