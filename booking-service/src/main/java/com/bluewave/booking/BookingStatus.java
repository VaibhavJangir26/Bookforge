package com.bluewave.booking;

public enum BookingStatus {
    PENDING_PAYMENT, // Slot temporarily held for 10 minutes
    CONFIRMED,       // Payment successfully processed
    CANCELLED,       // Cancelled by Customer or Provider before start time
    EXPIRED,         // Payment window timed out (10-minute hold elapsed)
    COMPLETED,       // Slot time ended; Customer checked out
    REFUNDED
}
