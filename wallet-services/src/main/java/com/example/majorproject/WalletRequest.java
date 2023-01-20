package com.example.majorproject;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class WalletRequest {

    private String userName;
    private int amount;
}
