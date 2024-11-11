package com.fusionfx.monolith.dto.metatrader;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsDto {
    private String platform;
    private String broker;
    private String currency;
    private String server;
    private double balance;
    private double equity;
    private double margin;
    private double freeMargin;
    private int leverage;
    private String name;
    private long login;
    private double credit;
    private boolean tradeAllowed;
    private boolean investorMode;
    private String marginMode;
    private String type;
}
