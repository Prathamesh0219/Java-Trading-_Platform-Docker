package com.fusionfx.monolith.dto.metatrader;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationRequestDto {
    private String login;
    private String password;
    private String name;
    private String server;
    private String platform;
    private int magic;
}
