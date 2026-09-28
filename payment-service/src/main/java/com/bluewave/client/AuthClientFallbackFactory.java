package com.bluewave.client;

import com.bluewave.dto.CommonApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class AuthClientFallbackFactory implements FallbackFactory<AuthClient> {

    @Override
    public AuthClient create(Throwable cause) {
        return new AuthClient() {
            @Override
            public CommonApiResponse<String> getProviderStripeAccountId(String providerId) {
                log.error("AuthClient fallback triggered for getProviderStripeAccountId {}. Reason: {}", providerId, cause.getMessage());
                return CommonApiResponse.<String>builder()
                        .success(false)
                        .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .message("Auth service currently unavailable for Stripe account query: " + cause.getMessage())
                        .timestamp(LocalDateTime.now())
                        .data(null)
                        .build();
            }
        };
    }
}
