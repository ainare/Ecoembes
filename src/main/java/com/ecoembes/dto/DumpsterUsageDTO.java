package com.ecoembes.dto;

import java.time.LocalDate;
import java.util.List;

public class DumpsterUsageDTO {

	private String dumpsterId;
	private LocalDate startDate;
	private LocalDate endDate;
	private List<DumpsterReadingDTO> readings;
	
	public DumpsterUsageDTO() {
		
	}
	
	public DumpsterUsageDTO(String dumpsterId, LocalDate startDate,
            LocalDate endDate, List<DumpsterReadingDTO> readings) {
        this.dumpsterId = dumpsterId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.readings = readings;
    }

	public String getDumpsterId() {
		return dumpsterId;
	}

	public void setDumpsterId(String dumpsterId) {
		this.dumpsterId = dumpsterId;
	}

	public LocalDate getStartDate() {
		return startDate;
	}                                                                                                                                                    

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public List<DumpsterReadingDTO> getReadings() {
		return readings;
	}

	public void setReadings(List<DumpsterReadingDTO> readings) {
		this.readings = readings;
	}
	
	
	
}
