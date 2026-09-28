package com.bluewave.client;

import com.bluewave.dto.CommonApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTH-SERVICE", fallbackFactory = AuthClientFallbackFactory.class)
public interface AuthClient {

    @GetMapping("/api/v1/profile/internal/provider/{providerId}/stripe-account")
    CommonApiResponse<String> getProviderStripeAccountId(@PathVariable("providerId") String providerId);
}
