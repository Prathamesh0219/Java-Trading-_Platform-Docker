package com.fusionfx.monolith.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountDto {
    private double     balance;       // Account balance
    private double     equity;        // Account equity
    private Double     marginLevel;   // Margin level
    private double     openNetPnl;    // Open Net Profit and Loss
    private int        positionCount; // Open Position Count
}
