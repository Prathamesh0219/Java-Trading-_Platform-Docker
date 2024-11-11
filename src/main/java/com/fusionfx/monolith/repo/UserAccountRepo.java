package com.fusionfx.monolith.repo;

import com.fusionfx.monolith.entity.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountRepo extends ReactiveMongoRepository<UserAccount, String> {

}
