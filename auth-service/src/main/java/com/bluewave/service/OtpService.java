package com.bluewave.service;

import com.bluewave.dto.OtpVerfiyRequestDTO;
import com.bluewave.dto.SignupRequestDTO;
import com.bluewave.exception.BadRequestException;
import com.bluewave.exception.GernalServerError;
import com.bluewave.exception.TooManyRequestException;
import com.bluewave.utils.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;


    private static final String OTP_PREFIX="OTP:";
    private static final String OTP_ATTEMPTED_PREFIX="ATTEMPTED:";
    private static final String OTP_COOLDOWN_PREFIX="COOLDOWN:";
    private static final String OTP_PAYLOAD_PREFIX="PAYLOAD:";

    private static final int OTP_EXPIRE_IN_MIN=5;
    private static final int COOLDOWN_SECONDS=120;
    private static final int MAX_ATTEMPTED=3;


    private String generateOTP(){
        SecureRandom random=new SecureRandom();
        int num=100000+ random.nextInt(900000);
        return String.valueOf(num);
    }

    @Transactional
    public void generateAndSendOTP(SignupRequestDTO dto){
        String normalizeEmail=dto.getEmail();
        String cooldownKey=OTP_COOLDOWN_PREFIX+normalizeEmail;

        if(Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))){
            throw new TooManyRequestException("new sending otp available after 2min");
        }
        String otp=generateOTP();
        String otpKey=OTP_PREFIX+normalizeEmail;
        String otpAttemptKey=OTP_ATTEMPTED_PREFIX+normalizeEmail;
        String otpPayloadKey=OTP_PAYLOAD_PREFIX+normalizeEmail;

        try{
            String signupData=objectMapper.writeValueAsString(dto);

            redisTemplate.opsForValue().set(otpKey,otp, Duration.ofMinutes(OTP_EXPIRE_IN_MIN));
            redisTemplate.opsForValue().set(otpAttemptKey,"0", Duration.ofMinutes(OTP_EXPIRE_IN_MIN));
            redisTemplate.opsForValue().set(otpPayloadKey,signupData, Duration.ofMinutes(OTP_EXPIRE_IN_MIN));
            redisTemplate.opsForValue().set(cooldownKey,"true", Duration.ofSeconds(COOLDOWN_SECONDS));

            String emailBody = String.format("Your BookForge verification code is: %s.\nThis code is valid for %d minutes.", otp, OTP_EXPIRE_IN_MIN);
            emailService.sendEmail(normalizeEmail, "Verification Code", emailBody);


        } catch (Exception e) {
            clearOtpSession(normalizeEmail);
            log.error("error while sending error in otp service {}", e.getMessage());
            throw new GernalServerError(e.toString());
        }


    }


    @Transactional
    public SignupRequestDTO verifyAndValidateOTP(OtpVerfiyRequestDTO dto){
        String normalizeEmail=dto.getEmail();
        String otpKey=OTP_PREFIX+normalizeEmail;
        String otpAttemptKey=OTP_ATTEMPTED_PREFIX+normalizeEmail;
        String otpPayloadKey=OTP_PAYLOAD_PREFIX+normalizeEmail;

        String cachedOTP=redisTemplate.opsForValue().get(otpKey);
        if(cachedOTP==null||cachedOTP.isEmpty()){
            throw new BadRequestException("either otp is expired or not valid, send again");
        }

        Long increaseAttempt = redisTemplate.opsForValue().increment(otpAttemptKey);
        if (increaseAttempt != null && increaseAttempt > MAX_ATTEMPTED) {
            clearOtpSession(normalizeEmail);
            throw new TooManyRequestException("max limit reached for OTP verification attempts.");
        }
        if (!cachedOTP.equals(dto.getOtp().trim())) {
            long remainingAttempts = Math.max(0, MAX_ATTEMPTED - (increaseAttempt != null ? increaseAttempt : 0));
            throw new BadRequestException("Invalid OTP. Remaining attempts: " + remainingAttempts);
        }

        String payloadData = redisTemplate.opsForValue().get(otpPayloadKey);
        if (payloadData == null || payloadData.isEmpty()) {
            throw new BadRequestException("Registration session expired. Please try again.");
        }

        try {
            SignupRequestDTO signupDTO = objectMapper.readValue(payloadData, SignupRequestDTO.class);
            clearOtpSession(normalizeEmail);
            return signupDTO;
        } catch (Exception e) {
            log.error("Failed to process payload data for {}: {}", normalizeEmail, e.getMessage());
            throw new GernalServerError("Verification processing failed. Please try again.");
        }
    }



    private void clearOtpSession(String email) {
        redisTemplate.delete(OTP_PREFIX + email);
        redisTemplate.delete(OTP_ATTEMPTED_PREFIX + email);
        redisTemplate.delete(OTP_PAYLOAD_PREFIX + email);
        redisTemplate.delete(OTP_COOLDOWN_PREFIX + email);
        log.info("Redis OTP session cleaned for {}", email);
    }

}
