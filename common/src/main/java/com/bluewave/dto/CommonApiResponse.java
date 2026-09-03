package com.bluewave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonApiResponse<T> {

    private String status;
    private boolean success;
    private LocalDateTime timestamp;
    private T data;
    private String message;

}
