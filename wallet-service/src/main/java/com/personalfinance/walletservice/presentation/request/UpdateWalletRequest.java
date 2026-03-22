package com.personalfinance.walletservice.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateWalletRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
