package com.plassb.facade;

public record AssignmentResult(
        AssignmentReceipt receipt,
        boolean created
) {
	
}