package com.fusionfx.monolith.service.data;

import com.fusionfx.monolith.dto.AccountDto;
import com.fusionfx.monolith.dto.tradelocker.AccountDetailsConfigDto;
import com.fusionfx.monolith.entity.TradeAccount;
import com.fusionfx.monolith.enums.AccountType;
import com.fusionfx.monolith.service.auth.TradeLockerAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TradeLockerDataService {

    private static final String DEMO_BASE_URL = "https://demo.tradelocker.com/backend-api";
    private static final String LIVE_BASE_URL = "https://live.tradelocker.com/backend-api";

    private final WebClient.Builder webClientBuilder;
    private final TradeLockerAuthService tradeLockerAuthService;

    // Get the base URL based on the account type (LIVE or DEMO)
    private String getBaseUrl(AccountType accountType) {
        return accountType == AccountType.LIVE ? LIVE_BASE_URL : DEMO_BASE_URL;
    }

    /**
     * Fetch account configuration details from the TradeLocker API.
     *
     * @param tradeAccount The TradeAccount entity containing account details.
     * @return A Mono containing AccountDetailsConfigDto.
     */
    private Mono<AccountDetailsConfigDto> getAccountDetailsConfig(TradeAccount tradeAccount) {
        String baseUrl = getBaseUrl(tradeAccount.getType());

        return tradeLockerAuthService.getValidToken(tradeAccount, baseUrl)
                .flatMap(token -> {
                    WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

                    return webClient.get()
                            .uri("/trade/config")
                            .headers(headers -> {
                                headers.set("accNum", "1");
                                headers.setBearerAuth(token);
                            })
                            .retrieve()
                            .bodyToMono(AccountDetailsConfigDto.class)  // Map response to DTO
                            .onErrorMap(error -> new RuntimeException("Error fetching account details config", error));
                });
    }

    /**
     * Fetch account state and map it to the accountDetailsConfig columns.
     *
     * @param tradeAccount The TradeAccount entity containing account details.
     * @return A Mono containing the mapped account details.
     */
    private Mono<Map<String, Object>> getAccountState(TradeAccount tradeAccount) {
        String baseUrl = getBaseUrl(tradeAccount.getType());

        return tradeLockerAuthService.getValidToken(tradeAccount, baseUrl)
                .flatMap(token -> {
                    WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

                    return webClient.get()
                            .uri("/trade/accounts/{accountId}/state", tradeAccount.getAccountNumber())
                            .headers(headers -> {
                                headers.set("accNum", "1");
                                headers.setBearerAuth(token);
                            })
                            .retrieve()
                            .bodyToMono(Map.class)
                            .flatMap(stateResponse -> {
                                List<Double> accountDetailsData = (List<Double>) ((Map<String, Object>) stateResponse.get("d")).get("accountDetailsData");

                                // Fetch the account details config and map it to the state data
                                return getAccountDetailsConfig(tradeAccount)
                                        .map(accountDetailsConfig -> {
                                            Map<String, Object> mappedAccountDetails = new HashMap<>();
                                            List<AccountDetailsConfigDto.Config.Column> columns = accountDetailsConfig.getD().getAccountDetailsConfig().getColumns();

                                            // Ensure the number of columns matches the data in accountDetailsData
                                            for (int i = 0; i < columns.size(); i++) {
                                                String columnId = columns.get(i).getId();
                                                // Map the column ID to the corresponding accountDetailsData value
                                                mappedAccountDetails.put(columnId, accountDetailsData.get(i));
                                            }

                                            return mappedAccountDetails;
                                        });
                            });
                })
                .onErrorMap(error -> new RuntimeException("Error fetching or mapping account state", error));
    }

    /**
     * Fetch the complete account details from TradeLocker by running parallel calls for account state and account config.
     *
     * @param tradeAccount The TradeAccount entity containing account details.
     * @return A Mono containing the final account details.
     */
    public Mono<AccountDto> fetchTradeLockerAccount(TradeAccount tradeAccount) {
        // Run getAccountState and getAccountDetailsConfig in parallel
        return Mono.zip(getAccountState(tradeAccount), getAccountDetailsConfig(tradeAccount))
                .map(tuple -> {
                    Map<String, Object> accountState = tuple.getT1(); // getAccountState response
                    AccountDetailsConfigDto accountDetailsConfig = tuple.getT2(); // getAccountDetailsConfig response

                    // Build the AccountDto from accountState
                    var account = AccountDto.builder()
                            .balance((Double) accountState.get("balance"))
                            .equity((Double) accountState.get("projectedBalance"))
                            .openNetPnl((Double) accountState.get("openNetPnL"))
                            .positionCount((Integer) accountState.get("positionsCount"))
                            .build();

                    // Use BigDecimal to calculate margin level and avoid scientific notation
                    BigDecimal equity = BigDecimal.valueOf(account.getEquity());
                    BigDecimal maintMarginReq = BigDecimal.valueOf((Double) accountState.get("maintMarginReq"));
                    BigDecimal leverage = BigDecimal.valueOf(tradeAccount.getLeverage());

                    // Calculate the margin level
                    BigDecimal marginLevel = equity.divide(maintMarginReq.divide(leverage, 10, RoundingMode.HALF_UP), 10, RoundingMode.HALF_UP);

                    // Set the margin level
                    account.setMarginLevel(marginLevel.doubleValue());

                    return account;
                })
                .onErrorMap(error -> new RuntimeException("Error fetching TradeLocker account", error));
    }
}
