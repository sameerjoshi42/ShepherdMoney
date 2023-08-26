package com.shepherdmoney.interviewproject.vo.request;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateBalancePayload {

    @NotBlank(message = "cc number is required")
    private String creditCardNumber;

    @NotBlank(message = "txn time is required")
    private Instant transactionTime;

    @NotBlank(message = "txn amount is required")
    private double transactionAmount;
}
