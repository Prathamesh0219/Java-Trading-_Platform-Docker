package com.fusionfx.monolith.service.data;

import com.fusionfx.monolith.AppProperties;
import com.fusionfx.monolith.dto.metatrader.AccountDetailsDto;
import com.fusionfx.monolith.entity.TradeAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaTraderDataService {

    private final AppProperties appProperties;
    private final WebClient.Builder webClientBuilder;

    /**
     * Fetches account information from the MetaTrader API for the given account.
     * <p>
     * This method sends a GET request to the MetaTrader API using the account ID
     * stored in the {@link TradeAccount} entity and retrieves detailed information about the account.
     * It returns the response mapped to an {@link AccountDetailsDto} object.
     * The method logs successful retrieval of account information and handles errors
     * by logging the error details.
     *
     * @param tradeAccount The {@link TradeAccount} entity containing the account ID and other account details.
     * @return A {@link Mono} containing the account information mapped to {@link AccountDetailsDto}.
     */
    public Mono<AccountDetailsDto> fetchAccountInformation(TradeAccount tradeAccount) {
        WebClient webClient = webClientBuilder.build();

        return webClient.get()
                .uri("https://mt-client-api-v1.london.agiliumtrade.ai/users/current/accounts/{accountId}/account-information", tradeAccount.getCloudAccountNumber())
                .header("Accept", "application/json")
                .header("auth-token", appProperties.getMetaApi().getApiKey())
                .retrieve()
                .bodyToMono(AccountDetailsDto.class) // Map the response directly to the DTO
                .doOnSuccess(response -> log.info("Successfully retrieved account information: {}", response))
                .doOnError(error -> log.error("Error retrieving account information: {}", error.getMessage()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)) // Retry 3 times with a 2-second backoff
                        .doBeforeRetry(retrySignal -> log.warn("Retrying to fetch account information, attempt: {}", retrySignal.totalRetries() + 1)))
                .map(acctInfo -> {
                    // Set tradeAccount fields if null
                    if (tradeAccount.getBroker() == null) {
                        tradeAccount.setBroker(acctInfo.getBroker());
                    }
                    if (tradeAccount.getLeverage() == 0) { // Assuming leverage defaults to 0 when unset
                        tradeAccount.setLeverage(acctInfo.getLeverage());
                    }
                    return acctInfo;
                });
    }

}
