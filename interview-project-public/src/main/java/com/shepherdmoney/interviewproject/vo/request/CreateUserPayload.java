package com.shepherdmoney.interviewproject.vo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserPayload {

    @NotBlank(message = "name should not be empty")
    private String name;

    @NotBlank(message = "email should not be empty")
    private String email;
}
