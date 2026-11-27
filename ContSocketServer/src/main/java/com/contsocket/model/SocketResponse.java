package com.contsocket.model;

public record SocketResponse(
        boolean success,
        String errorMessage,
        PlantCapacity capacity,
        AssignmentReceipt receipt
) {
    public static SocketResponse okCapacity(PlantCapacity capacity) {
        return new SocketResponse(true, null, capacity, null);
    }

    public static SocketResponse okReceipt(AssignmentReceipt receipt) {
        return new SocketResponse(true, null, null, receipt);
    }

    public static SocketResponse error(String message) {
        return new SocketResponse(false, message, null, null);
    }
}
