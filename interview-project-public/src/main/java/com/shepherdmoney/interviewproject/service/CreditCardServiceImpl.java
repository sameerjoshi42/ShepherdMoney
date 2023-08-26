package com.shepherdmoney.interviewproject.service;

import com.shepherdmoney.interviewproject.exception.InterviewProjectException;
import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CreditCardServiceImpl implements CreditCardService {

    private CreditCardRepository creditCardRepository;

    private UserRepository userRepository;

    @Autowired
    public CreditCardServiceImpl(CreditCardRepository creditCardRepository, UserRepository userRepository) {
        this.creditCardRepository = creditCardRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Integer addCreditCardToUser(AddCreditCardToUserPayload payload) {
        // TODO: Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format a
        //       nd length
        Optional<User> optionalUser = this.userRepository.findById(payload.getUserId());
        if (optionalUser.isEmpty()) {
            throw new InterviewProjectException("user not found");
        }
        User user = optionalUser.get();
        CreditCard creditCard = new CreditCard();
        creditCard.setNumber(payload.getCardNumber());
        creditCard.setIssuanceBank(payload.getCardIssuanceBank());
        creditCard.setUser(user);
        this.creditCardRepository.save(creditCard);

        return creditCard.getId();
    }

    @Override
    public List<CreditCardView> getAllCardOfUser(int userId) {
        Optional<User> optionalUser = this.userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new InterviewProjectException("user not found");
        }
        User user = optionalUser.get();
        if (user.getCreditCardList() == null) {
            return new ArrayList<>();
        }
        List<CreditCardView> creditCardViewList = user.getCreditCardList().stream().map((cc) ->
                new CreditCardView(cc.getIssuanceBank(), cc.getNumber())
        ).collect(Collectors.toList());
        return creditCardViewList;
    }

    @Override
    public Integer getUserIdForCreditCard(String creditCardNumber) {
        Optional<CreditCard> creditCardOptional = this.creditCardRepository.findByNumber(creditCardNumber);
        if (creditCardOptional.isEmpty()) {
            throw new InterviewProjectException("credit card not found");
        }
        CreditCard creditCard = creditCardOptional.get();
        return creditCard.getUser().getId();
    }

    @Override
    public CreditCard findCreditCardByNumber(String creditCardNumber) {
        Optional<CreditCard> creditCardOptional = this.creditCardRepository.findByNumber(creditCardNumber);
        if (creditCardOptional.isEmpty()) {
            throw new InterviewProjectException("credit card not found");
        }
        return creditCardOptional.get();
    }

    @Override
    public List<BalanceHistory> postBalance(UpdateBalancePayload[] payload) {
        String ccNumber = payload[0].getCreditCardNumber();
        CreditCard creditCard = this.findCreditCardByNumber(ccNumber);
        Arrays.sort(payload, Comparator.comparing(UpdateBalancePayload::getTransactionTime));

        //case 1 - if card has not any balance history -
        System.out.println("fgfgfgfgfgfg"+creditCard.getBalanceHistoryList().size());
        if (creditCard.getBalanceHistoryList().isEmpty()) {
            Double sum = payload[0].getTransactionAmount();
            Instant currInstant = payload[0].getTransactionTime();
            List<BalanceHistory> history = new ArrayList<>();

            for (int i = 1; i < payload.length; i++) {
                if (currInstant.compareTo(payload[i].getTransactionTime()) != 0) {
                    BalanceHistory balanceHistory = new BalanceHistory();
                    balanceHistory.setBalance(sum);
                    balanceHistory.setDate(currInstant);
                    balanceHistory.setCreditCard(creditCard);
                    currInstant = payload[i].getTransactionTime();
                    history.add(balanceHistory);
                }
                sum = sum + payload[i].getTransactionAmount();
            }
            Instant today = Instant.now();
            if(today.compareTo(history.get(0).getDate())!=0){
                Double amount = history.get(0).getBalance();
                BalanceHistory balanceHistory = new BalanceHistory();
                balanceHistory.setBalance(amount);
                balanceHistory.setDate(today);
                balanceHistory.setCreditCard(creditCard);
                history.add(0,balanceHistory);

            }
            creditCard.setBalanceHistoryList(history);
            this.creditCardRepository.save(creditCard);

            return history;
        }
        List<BalanceHistory> currentBalanceHistory = creditCard.getBalanceHistoryList();
        for (UpdateBalancePayload txn : payload) {
            for (BalanceHistory balanceHistory : currentBalanceHistory) {
                if (balanceHistory.getDate().compareTo(txn.getTransactionTime()) >= 0) {
                    balanceHistory.setBalance(balanceHistory.getBalance() + txn.getTransactionAmount());
                }
            }
            Instant today = Instant.now();
            if(today.compareTo(currentBalanceHistory.get(0).getDate())!=0){
                Double amount = currentBalanceHistory.get(0).getBalance();
                BalanceHistory balanceHistory = new BalanceHistory();
                balanceHistory.setBalance(amount);
                balanceHistory.setDate(today);
                balanceHistory.setCreditCard(creditCard);
                currentBalanceHistory.add(0,balanceHistory);

            }

            creditCard.setBalanceHistoryList(currentBalanceHistory);
            this.creditCardRepository.save(creditCard);

        }
        return currentBalanceHistory;
    }
}
