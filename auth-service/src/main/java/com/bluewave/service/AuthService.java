package com.bluewave.service;

import com.bluewave.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {


    public  Map<String, Boolean> checkAvailable(String email, String username) {
        return  null;
    }

    public String signup(SignupRequestDTO dto) {
        return  null;
    }

    public  AuthResponseDTO verify( OtpVerfiyRequestDTO dto) {
        return  null;
    }

    public  AuthResponseDTO login( LoginRequestDTO dto) {
        return  null;
    }

    public  AuthResponseDTO refresh( AuthNewTokenRequestDTO dto) {
        return  null;
    }

    public String logout( AuthNewTokenRequestDTO dto) {
        return  null;
    }
}
