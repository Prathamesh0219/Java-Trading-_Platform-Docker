package com.fusionfx.monolith.service.management;

import com.fusionfx.monolith.AppProperties;
import com.fusionfx.monolith.dto.metatrader.AccountCreationResponseDto;
import com.fusionfx.monolith.dto.metatrader.AccountCreationRequestDto;
import com.fusionfx.monolith.entity.TradeAccount;
import com.fusionfx.monolith.enums.Platform;
import com.fusionfx.monolith.repo.TradeAccountRepo;
import com.fusionfx.monolith.service.data.MetaTraderDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaTraderManagementService {

    private final AppProperties appProperties;
    private final TradeAccountRepo tradeAccountRepo;
    private final WebClient.Builder webClientBuilder;
    private final MetaTraderDataService metaTraderDataService;


    /**
     * Create a new trading account on the MT provisioning API and persist the account if successful.
     *
     * @param accountDetails The account details to send in the request body
     * @return A Mono containing the response from the MT API
     */
    public Mono<AccountCreationResponseDto> createTradingAccount(String userId, AccountCreationRequestDto accountDetails) {

        // Validate the platform field
        if (!"mt4".equalsIgnoreCase(accountDetails.getPlatform()) && !"mt5".equalsIgnoreCase(accountDetails.getPlatform())) {
            return Mono.error(new IllegalArgumentException("Invalid platform: " + accountDetails.getPlatform() + ". Allowed values are 'mt4' or 'mt5'."));
        }

        // Determine the platform enum value based on the string
        Platform platform;
        if ("mt4".equalsIgnoreCase(accountDetails.getPlatform())) {
            platform = Platform.METATRADER4;
        } else {
            platform = Platform.METATRADER5;
        }

        WebClient webClient = webClientBuilder
                .baseUrl("https://mt-provisioning-api-v1.agiliumtrade.agiliumtrade.ai").build();

        return webClient.post()
                .uri("/users/current/accounts")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("auth-token", appProperties.getMetaApi().getApiKey())
                .header("transaction-id", UUID.randomUUID().toString().replace("-", ""))
                .bodyValue(accountDetails)
                .retrieve()
                .bodyToMono(AccountCreationResponseDto.class)
                .flatMap(response -> {
                    log.info("Successfully created trading account: {}", response);

                    // If successful, save the TradeAccount entity in the database
                    TradeAccount newTradeAccount = new TradeAccount();
                    newTradeAccount.setUserId(userId);
                    newTradeAccount.setName(accountDetails.getName());
                    newTradeAccount.setPlatform(platform);
                    newTradeAccount.setAccountNumber(accountDetails.getLogin());
                    newTradeAccount.setCloudAccountNumber(response.getId());
                    newTradeAccount.setAccountNumber(response.getId());
                    newTradeAccount.setServer(accountDetails.getServer());
                    newTradeAccount.setCreatedAt(LocalDateTime.now());
                    newTradeAccount.setEditedAt(LocalDateTime.now());

                    return tradeAccountRepo.save(newTradeAccount)
                            .thenReturn(response);
                })
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException webClientException = (WebClientResponseException) error;
                        String responseBody = webClientException.getResponseBodyAsString();  // Get the response body
                        log.error("Error creating trading account: {}, response body: {}", error.getMessage(), responseBody);
                    } else {
                        log.error("Error creating trading account: {}", error.getMessage());
                    }
                });
    }
}
