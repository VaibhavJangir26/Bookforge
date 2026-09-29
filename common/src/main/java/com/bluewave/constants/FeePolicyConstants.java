package com.bluewave.constants;

import java.math.BigDecimal;

public final class FeePolicyConstants {

    private FeePolicyConstants() {}

    public static final BigDecimal DEFAULT_PLATFORM_COMMISSION_PERCENT = new BigDecimal("10.0");

    public static final long CANCELLATION_NOTICE_HOURS = 24L;

    public static final BigDecimal DEFAULT_CUSTOMER_REFUND_PERCENT = new BigDecimal("80.0");
    public static final BigDecimal DEFAULT_PROVIDER_COMPENSATION_PERCENT = new BigDecimal("15.0");
    public static final BigDecimal DEFAULT_PLATFORM_RETAINED_PERCENT = new BigDecimal("5.0");
}
