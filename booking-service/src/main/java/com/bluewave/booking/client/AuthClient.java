package com.bluewave.booking.client;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.UserPersonalDetailResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTH-SERVICE", fallbackFactory = AuthClientFallbackFactory.class)
public interface AuthClient {

    @GetMapping("/api/v1/profile/{userId}")
    CommonApiResponse<UserPersonalDetailResponseDTO> getUserById(@PathVariable("userId") String userId);

}
