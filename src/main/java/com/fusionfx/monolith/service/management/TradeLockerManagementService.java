package com.fusionfx.monolith.service.management;

import com.fusionfx.monolith.entity.TradeAccount;
import com.fusionfx.monolith.enums.AccountType;
import com.fusionfx.monolith.repo.TradeAccountRepo;
import com.fusionfx.monolith.service.auth.TradeLockerAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TradeLockerManagementService {

    private final TradeAccountRepo tradeAccountRepo;
    private final TradeLockerAuthService tradeLockerAuthService;

    /**
     * Checks the login for a TradeAccount. If the login is successful, the TradeAccount is saved in the database.
     *
     * @param tradeAccount The TradeAccount entity containing the login details.
     * @return A Mono<Boolean> indicating if the login was successful and account was saved.
     */
    public Mono<Boolean> checkLoginAndSave(TradeAccount tradeAccount) {
        // Use the TradeLockerAuthService to perform login check
        String baseUrl = tradeAccount.getType() == AccountType.LIVE
                ? "https://live.tradelocker.com/backend-api"
                : "https://demo.tradelocker.com/backend-api";

        return tradeLockerAuthService.getValidToken(tradeAccount, baseUrl)
                .flatMap(token -> {
                    // If login was successful, save the TradeAccount in the database
                    return tradeAccountRepo.save(tradeAccount)
                            .thenReturn(true);  // Return true to indicate success
                })
                .onErrorResume(e -> {
                    // In case of an error (login failed), log the error and return false
                    System.out.println("Login failed for TradeAccount: " + tradeAccount.getAccountNumber() + ". Error: " + e.getMessage());
                    return Mono.just(false);  // Return false to indicate failure
                });
    }
}

