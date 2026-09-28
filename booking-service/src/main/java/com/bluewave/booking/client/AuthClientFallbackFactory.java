package com.bluewave.booking.client;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.UserPersonalDetailResponseDTO;
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
            public CommonApiResponse<UserPersonalDetailResponseDTO> getUserById(String userId) {
                log.error("AuthClient fallback triggered for getUserById {}. Error: {}", userId, cause.getMessage());
                return CommonApiResponse.<UserPersonalDetailResponseDTO>builder()
                        .success(false)
                        .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .message("Auth service is currently unavailable for user details: " + cause.getMessage())
                        .timestamp(LocalDateTime.now())
                        .data(null)
                        .build();
            }
        };
    }
}
