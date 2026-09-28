package com.bluewave.booking.config;

import com.bluewave.constants.FeePolicyConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Centrally manages refund percentages for booking service operations.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "bookforge.policy")
public class FeePolicyConfig {

    private BigDecimal customerRefundPercent = FeePolicyConstants.DEFAULT_CUSTOMER_REFUND_PERCENT;
    private BigDecimal providerRetainedPercent = FeePolicyConstants.DEFAULT_PROVIDER_COMPENSATION_PERCENT;
    private BigDecimal platformRetainedPercent = FeePolicyConstants.DEFAULT_PLATFORM_RETAINED_PERCENT;
}
