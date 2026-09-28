package com.bluewave.config;

import com.bluewave.constants.FeePolicyConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Centrally manages platform fee and refund policy configurations.
 * All values default to FeePolicyConstants and can be customized via application.properties.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "bookforge.policy")
public class FeePolicyConfig {

    /**
     * Standard platform commission on completed bookings (Default: 10.0%)
     */
    private BigDecimal platformCommissionPercent = FeePolicyConstants.DEFAULT_PLATFORM_COMMISSION_PERCENT;

    /**
     * Percentage refunded to customer when cancelling before 24h cutoff (Default: 80.0%)
     */
    private BigDecimal customerRefundPercent = FeePolicyConstants.DEFAULT_CUSTOMER_REFUND_PERCENT;

    /**
     * Percentage retained for venue provider when customer cancels before 24h cutoff (Default: 15.0%)
     */
    private BigDecimal providerRetainedPercent = FeePolicyConstants.DEFAULT_PROVIDER_COMPENSATION_PERCENT;

    /**
     * Percentage retained by platform for payment/processing when cancelled (Default: 5.0%)
     */
    private BigDecimal platformRetainedPercent = FeePolicyConstants.DEFAULT_PLATFORM_RETAINED_PERCENT;
}
