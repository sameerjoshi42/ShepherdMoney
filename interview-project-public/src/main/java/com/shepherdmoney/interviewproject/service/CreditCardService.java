package com.shepherdmoney.interviewproject.service;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface CreditCardService {

    Integer addCreditCardToUser(AddCreditCardToUserPayload payload);

    List<CreditCardView> getAllCardOfUser(int userId);

    Integer getUserIdForCreditCard(String creditCardNumber);

    CreditCard findCreditCardByNumber(String creditCardNumber);

    List<BalanceHistory> postBalance(UpdateBalancePayload[] payload);
}

