package com.fusionfx.monolith.dto.dxtrade;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountMetricsDto {

    private String account;
    private int version;
    private double equity;
    private double balance;
    private double availableBalance;
    private double availableFunds;
    private double credit;
    private double marginFree;
    private double openPL;
    private double totalPL;
    private double margin;
    private int openPositionsCount;
    private int openOrdersCount;
}
