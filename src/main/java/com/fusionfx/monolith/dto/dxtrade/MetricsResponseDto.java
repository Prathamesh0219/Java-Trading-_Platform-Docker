package com.fusionfx.monolith.dto.dxtrade;

import lombok.Data;
import java.util.List;

@Data
public class MetricsResponseDto {
    private List<AccountMetricsDto> metrics;
}
