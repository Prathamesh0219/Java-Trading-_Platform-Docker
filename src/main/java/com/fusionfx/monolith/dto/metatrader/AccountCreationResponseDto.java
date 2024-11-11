package com.fusionfx.monolith.dto.metatrader;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationResponseDto {
    private String id;
    private String state;
}
