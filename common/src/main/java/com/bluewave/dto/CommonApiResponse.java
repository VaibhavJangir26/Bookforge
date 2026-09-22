package com.bluewave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer status;
    private boolean success;
    private LocalDateTime timestamp;
    private T data;
    private String message;

}
