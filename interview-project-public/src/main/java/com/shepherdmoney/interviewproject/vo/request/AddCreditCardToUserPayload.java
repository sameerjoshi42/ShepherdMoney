package com.shepherdmoney.interviewproject.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCreditCardToUserPayload {

    @NotNull(message = "userId is required")
    private Integer userId;

    @NotBlank(message = "cardIssuanceBank is required")
    private String cardIssuanceBank;

    @NotBlank(message = "cardNumber is required")
    private String cardNumber;
}
