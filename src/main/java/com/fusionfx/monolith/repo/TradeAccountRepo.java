package com.fusionfx.monolith.repo;

import com.fusionfx.monolith.entity.TradeAccount;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TradeAccountRepo extends ReactiveMongoRepository<TradeAccount, String> {
    Flux<TradeAccount> findAllByUserId(String userId);
    Mono<TradeAccount> findByIdAndUserId(String accountNumber, String userId);

}

