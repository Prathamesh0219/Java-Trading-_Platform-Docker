package com.fusionfx.monolith.controller.dxtrade;

import com.fusionfx.monolith.dto.AccountDto;
import com.fusionfx.monolith.entity.TradeAccount;
import com.fusionfx.monolith.repo.TradeAccountRepo;
import com.fusionfx.monolith.service.data.DxTradeDataService;
import com.fusionfx.monolith.service.management.DxTradeManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dxtrade")
public class DxTradeController {

    private final TradeAccountRepo tradeAccountRepo;
    private final DxTradeDataService dxTradeDataService;
    private final DxTradeManagementService dxTradeManagementService;

    @PostMapping("/add-account")
    public Mono<Boolean> loginAndSave(@RequestBody TradeAccount tradeAccount,
                                      @AuthenticationPrincipal Jwt jwt) {
        // Extract the userId from the JWT token
        String userId = jwt.getClaimAsString("sub");
        tradeAccount.setUserId(userId);

        return dxTradeManagementService.checkLoginAndSave(tradeAccount);
    }

    @GetMapping("/account-details")
    public Mono<AccountDto> getAccountDetails(@RequestParam String accountNumber,
                                              @AuthenticationPrincipal Jwt jwt) {
        // Extract the userId from the JWT token
        String userId = jwt.getClaimAsString("sub");

        // Fetch the TradeAccount by accountNumber and userId to verify ownership
        return tradeAccountRepo.findByIdAndUserId(accountNumber, userId)
                .flatMap(tradeAccount -> {
                    // If TradeAccount exists and the userId matches, fetch the account details
                    return dxTradeDataService.getAccountMetrics(tradeAccount);
                })
                .switchIfEmpty(Mono.error(new IllegalAccessException("TradeAccount not found or not owned by the user")));
    }
}
