package com.fusionfx.monolith.entity;

import com.fusionfx.monolith.enums.AccountType;
import com.fusionfx.monolith.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Encrypted;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trade_accounts")
public class TradeAccount {

    @Id
    private String id;                 // Unique identifier for the trade account (MongoDB document ID)
    private String userId;             // Unique identifier for the user who owns this account
    private String name;               // Name of the trade account
    private String broker;             // Broker name
    private String propFirm;           // Prop Firm
    private Platform platform;         // Platform type (Enum)
    @Encrypted
    private String accountNumber;      // Account number
    @Encrypted
    private String cloudAccountNumber; // Account number on Cloud servers (MetaTrader)
    @Encrypted
    private String email;              // Associated email
    private String username;           // Associated Username
    @Encrypted
    private String password;           // Account password
    private AccountType type;          // Live or Demo (Enum)
    private int leverage;              // Leverage of the account
    private String connectionUrl;      // Connection URL for the broker
    private String server;             // Server
    private String domain;             // Domain

    @CreatedDate
    private LocalDateTime createdAt;   // Automatically populated when the document is created

    @LastModifiedDate
    private LocalDateTime editedAt;    // Automatically updated when the document is modified

}
