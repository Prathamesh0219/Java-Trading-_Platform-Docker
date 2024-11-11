package com.fusionfx.monolith.service.management;

import com.fusionfx.monolith.entity.TradeAccount;
import com.fusionfx.monolith.enums.AccountType;
import com.fusionfx.monolith.repo.TradeAccountRepo;
import com.fusionfx.monolith.service.auth.DxTradeAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DxTradeManagementService {

    private final TradeAccountRepo tradeAccountRepo;
    private final DxTradeAuthService dxTradeAuthService;

    /**
     * Checks the login for a TradeAccount. If the login is successful, the TradeAccount is saved in the database.
     *
     * @param tradeAccount The TradeAccount entity containing the login details.
     * @return A Mono<Boolean> indicating if the login was successful and account was saved.
     */
    public Mono<Boolean> checkLoginAndSave(TradeAccount tradeAccount) {
        return dxTradeAuthService.getValidToken(tradeAccount)
                .flatMap(token -> {
                    // If login was successful, save the TradeAccount in the database
                    return tradeAccountRepo.save(tradeAccount)
                            .thenReturn(true);  // Return true to indicate success
                })
                .onErrorResume(e -> {
                    // In case of an error (login failed), log the error and return false
                    log.error("Login failed for DX Trade: " + tradeAccount.getAccountNumber() + ". Error: " + e.getMessage());
                    return Mono.just(false);  // Return false to indicate failure
                });
    }
}
