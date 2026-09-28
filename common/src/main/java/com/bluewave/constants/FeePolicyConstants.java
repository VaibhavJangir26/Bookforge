package com.bluewave.constants;

import java.math.BigDecimal;

/**
 * =========================================================================
 *                   GLOBAL FEE & CANCELLATION POLICY CONSTANTS
 * =========================================================================
 * Single source of truth for platform commission and refund split policies.
 * Can be overridden in application.properties under "bookforge.policy.*".
 * =========================================================================
 */
public final class FeePolicyConstants {

    private FeePolicyConstants() {}

    /**
     * Standard Platform Commission on successful bookings (10%)
     */
    public static final BigDecimal DEFAULT_PLATFORM_COMMISSION_PERCENT = new BigDecimal("10.0");

    /**
     * 24-Hour Notice Cutoff window for cancellations
     */
    public static final long CANCELLATION_NOTICE_HOURS = 24L;

    /**
     * Cancellation Policy Breakdown (when cancelled >= 24 hours prior):
     * 1. 80% refunded to Customer
     * 2. 15% retained as compensation for Venue Provider
     * 3. 5% retained as Platform processing fee
     */
    public static final BigDecimal DEFAULT_CUSTOMER_REFUND_PERCENT = new BigDecimal("80.0");
    public static final BigDecimal DEFAULT_PROVIDER_COMPENSATION_PERCENT = new BigDecimal("15.0");
    public static final BigDecimal DEFAULT_PLATFORM_RETAINED_PERCENT = new BigDecimal("5.0");
}
