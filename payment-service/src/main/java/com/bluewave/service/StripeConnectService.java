package com.bluewave.service;

import com.bluewave.dto.CommonApiResponse;
import com.bluewave.dto.StripeConnectOnboardingResponseDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeConnectService {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * 1. Creates a Stripe Express Connected Account for a provider.
     * 2. Generates an AccountLink hosted onboarding URL.
     */
    public CommonApiResponse<StripeConnectOnboardingResponseDTO> createProviderOnboardingLink(String providerEmail, String providerId) {
        try {
            // 1. Create Express Account
            AccountCreateParams accountParams = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setEmail(providerEmail)
                    .setCapabilities(
                            AccountCreateParams.Capabilities.builder()
                                    .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder().setRequested(true).build())
                                    .setTransfers(AccountCreateParams.Capabilities.Transfers.builder().setRequested(true).build())
                                    .build()
                    )
                    .putMetadata("providerId", providerId)
                    .build();

            Account account = Account.create(accountParams);
            log.info("Created Stripe Connected Account: {} for providerId: {}", account.getId(), providerId);

            // 2. Generate Account Onboarding URL
            AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
                    .setAccount(account.getId())
                    .setRefreshUrl(frontendUrl + "/dashboard/provider.html?stripe=refresh")
                    .setReturnUrl(frontendUrl + "/dashboard/provider.html?stripe=success&accountId=" + account.getId())
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();

            AccountLink accountLink = AccountLink.create(linkParams);

            return CommonApiResponse.<StripeConnectOnboardingResponseDTO>builder()
                    .success(true)
                    .status(HttpStatus.OK.value())
                    .message("Onboarding link generated successfully")
                    .timestamp(LocalDateTime.now())
                    .data(StripeConnectOnboardingResponseDTO.builder()
                            .stripeAccountId(account.getId())
                            .onboardingUrl(accountLink.getUrl())
                            .build())
                    .build();

        } catch (StripeException e) {
            log.error("Stripe Connect onboarding error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initiate Stripe Connect: " + e.getMessage());
        }
    }
}
