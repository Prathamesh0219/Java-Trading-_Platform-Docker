package com.fusionfx.monolith.controller.tradelocker;

import com.fusionfx.monolith.dto.AccountDto;
import com.fusionfx.monolith.entity.TradeAccount;
import com.fusionfx.monolith.repo.TradeAccountRepo;
import com.fusionfx.monolith.service.data.TradeLockerDataService;
import com.fusionfx.monolith.service.management.TradeLockerManagementService;
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

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tradelocker")
public class TradeLockerController {

    private final TradeAccountRepo tradeAccountRepo;
    private final TradeLockerDataService tradeLockerDataService;
    private final TradeLockerManagementService tradeLockerManagementService;

    /**
     * POST endpoint to check login and save the TradeAccount if successful.
     *
     * @param tradeAccount The TradeAccount object containing the login details.
     * @return A Mono<Boolean> indicating if the login and save were successful.
     */
    @PostMapping("/add-account")
    public Mono<Boolean> loginAndSave(@RequestBody TradeAccount tradeAccount,
                                      @AuthenticationPrincipal Jwt jwt) {
        // Extract the userId from the JWT token
        String userId = jwt.getClaimAsString("sub");
        tradeAccount.setUserId(userId);

        return tradeLockerManagementService.checkLoginAndSave(tradeAccount);
    }

    /**
     * GET endpoint to fetch TradeLocker account details.
     *
     * @param accountNumber The account number.
     * @param jwt           The JWT token containing the user information.
     * @return A Mono containing the account details.
     */
    @GetMapping("/account-details")
    public Mono<AccountDto> getAccountDetails(@RequestParam String accountNumber,
                                              @AuthenticationPrincipal Jwt jwt) {
        // Extract the userId from the JWT token
        String userId = jwt.getClaimAsString("sub");

        // Fetch the TradeAccount by accountNumber and userId to verify ownership
        return tradeAccountRepo.findByIdAndUserId(accountNumber, userId)
                .flatMap(tradeAccount -> {
                    // If TradeAccount exists and the userId matches, fetch the account details
                    return tradeLockerDataService.fetchTradeLockerAccount(tradeAccount);
                })
                .switchIfEmpty(Mono.error(new IllegalAccessException("TradeAccount not found or not owned by the user")));
    }

}
