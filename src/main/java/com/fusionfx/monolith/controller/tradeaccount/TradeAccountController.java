package com.fusionfx.monolith.controller.tradeaccount;

import com.fusionfx.monolith.entity.TradeAccount;
import com.fusionfx.monolith.repo.TradeAccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trade-accounts")
public class TradeAccountController {

    private final TradeAccountRepo tradeAccountRepo;

    // Get all trade accounts for the current user (by extracting userId from JWT)
    @GetMapping
    public Flux<TradeAccount> getAllTradeAccountsByUserId(@AuthenticationPrincipal Jwt jwt) {
        // Extract userId from the JWT
        String userId = jwt.getClaimAsString("sub");

        // Return all trade accounts associated with the user
        return tradeAccountRepo.findAllByUserId(userId);
    }

    // Get a specific trade account by its account ID
    @GetMapping("/account/{accountId}")
    public Mono<TradeAccount> getTradeAccountById(@PathVariable String accountId,
                                                  @AuthenticationPrincipal Jwt jwt) {
        // Extract userId from the JWT (optional for further verification)
        String userId = jwt.getClaimAsString("sub");

        // Fetch the trade account by its ID
        return tradeAccountRepo.findById(accountId)
                .filter(account -> account.getUserId().equals(userId)); // Ensure the user owns the account
    }

    // Delete a trade account by its ID
    @DeleteMapping("/{accountId}")
    public Mono<Void> deleteTradeAccount(@PathVariable String accountId,
                                         @AuthenticationPrincipal Jwt jwt) {
        // Extract userId from the JWT
        String userId = jwt.getClaimAsString("sub");

        // Ensure the account being deleted belongs to the authenticated user
        return tradeAccountRepo.findById(accountId)
                .filter(account -> account.getUserId().equals(userId)) // Check ownership
                .flatMap(existingAccount -> tradeAccountRepo.deleteById(accountId)); // Delete account
    }
}
