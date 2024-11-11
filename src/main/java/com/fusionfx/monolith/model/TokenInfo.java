package com.fusionfx.monolith.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {

    private String accessToken;
    private String refreshToken;
    private long expiryTime;

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiryTime;
    }

}
