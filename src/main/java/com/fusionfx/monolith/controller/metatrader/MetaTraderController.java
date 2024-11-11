package com.fusionfx.monolith.controller.metatrader;

import com.fusionfx.monolith.dto.metatrader.AccountCreationRequestDto;
import com.fusionfx.monolith.dto.metatrader.AccountCreationResponseDto;
import com.fusionfx.monolith.dto.metatrader.AccountDetailsDto;
import com.fusionfx.monolith.repo.TradeAccountRepo;
import com.fusionfx.monolith.service.auth.MetaTraderAuthService;
import com.fusionfx.monolith.service.data.MetaTraderDataService;
import com.fusionfx.monolith.service.management.MetaTraderManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/metatrader")
public class MetaTraderController {


    private final TradeAccountRepo tradeAccountRepo;
    private final MetaTraderDataService metaTraderDataService;
    private final MetaTraderAuthService metaTraderAuthService;
    private final MetaTraderManagementService metaTraderManagementService;

    /**
     * Endpoint to create a new MetaTrader trading account.
     *
     * @param jwt            The JWT token containing the user information
     * @param accountDetails The details for the account to be created
     * @return A Mono containing the account creation response
     */
    @PostMapping("/add-account")
    public Mono<ResponseEntity<AccountCreationResponseDto>> createTradingAccount(
            @AuthenticationPrincipal Jwt jwt,  // Extract JWT token
            @RequestBody AccountCreationRequestDto accountDetails) {

        // Extract userId from the JWT claims (assuming it's in the "sub" claim)
        String userId = jwt.getSubject();

        return metaTraderManagementService.createTradingAccount(userId, accountDetails)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(error -> {
                    return Mono.just(ResponseEntity.badRequest().body(new AccountCreationResponseDto()));
                });
    }

    /**
     * Endpoint to fetch account information for a given account ID.
     *
     * @param jwt      The JWT token containing user information
     * @param accountNumber The account ID for which information is being requested
     * @return A Mono containing the account information
     */
    @GetMapping("/account-details")
    public Mono<ResponseEntity<AccountDetailsDto>> fetchAccountInformation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String accountNumber) {

        // Extract userId from JWT claims
        String userId = jwt.getSubject();

        // Find the TradeAccount associated with this user and accountId
        return tradeAccountRepo.findByIdAndUserId(accountNumber, userId)
                .flatMap(tradeAccount -> metaTraderDataService.fetchAccountInformation(tradeAccount)
                        .map(ResponseEntity::ok))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().body(null)));
    }

    /**
     * New Endpoint to fetch an auth token for a given account ID.
     *
     * @param jwt      The JWT token containing user information
     * @param accountId The account ID for which the auth token is being requested
     * @return A Mono containing the auth token as a String
     */
    @GetMapping("/auth-token")
    public Mono<ResponseEntity<String>> getAuthToken(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String accountId) {

        // Extract userId from JWT claims (if needed)
        String userId = jwt.getSubject();

        // Check if the account belongs to the authenticated user
        return tradeAccountRepo.findById(accountId)
                .flatMap(tradeAccount -> {
                    // Ensure the account belongs to the authenticated user
                    if (!tradeAccount.getUserId().equals(userId)) {
                        // If the user does not own the account, return 403 Forbidden
                        return Mono.just(ResponseEntity.status(403).body("User does not own this account"));
                    }

                    // Proceed to fetch the auth token if the user owns the account
                    return metaTraderAuthService.getAuthToken(tradeAccount.getCloudAccountNumber(), 24)
                            .map(ResponseEntity::ok);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(404).body("Account not found"))) // Handle account not found
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().body("Error fetching token"))); // Handle errors
    }
}
