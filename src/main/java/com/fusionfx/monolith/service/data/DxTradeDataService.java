package com.fusionfx.monolith.service.data;

import com.fusionfx.monolith.AppProperties;
import com.fusionfx.monolith.dto.AccountDto;
import com.fusionfx.monolith.dto.dxtrade.AccountMetricsDto;
import com.fusionfx.monolith.dto.dxtrade.MetricsResponseDto;
import com.fusionfx.monolith.entity.TradeAccount;
import com.fusionfx.monolith.service.auth.DxTradeAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DxTradeDataService {

    private final WebClient.Builder webClientBuilder;
    private final DxTradeAuthService dxTradeAuthService;

    public Mono<AccountDto> getAccountMetrics(final TradeAccount tradeAccount) {

        return dxTradeAuthService.getValidToken(tradeAccount)
                .flatMap(token -> {
                    WebClient webClient = webClientBuilder.build();

                    return webClient.get()
                            .uri(tradeAccount.getConnectionUrl() + "/dxsca-web/accounts/default:{accountNumber}/metrics", tradeAccount.getAccountNumber())
                            .header(HttpHeaders.AUTHORIZATION, "DXAPI " + token)
                            .retrieve()
                            .bodyToMono(MetricsResponseDto.class)
                            .flatMap(metricsDto -> {

                                var accountMetricsDto = metricsDto.getMetrics().get(0);

                                return Mono.just(
                                        AccountDto.builder()
                                                .balance(accountMetricsDto.getBalance())
                                                .equity(accountMetricsDto.getEquity())
                                                .openNetPnl(accountMetricsDto.getOpenPL())
                                                .marginLevel(accountMetricsDto.getMargin())
                                                .positionCount(accountMetricsDto.getOpenPositionsCount())
                                                .build());
                            });
                });
    }

}
