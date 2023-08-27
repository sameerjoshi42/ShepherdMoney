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

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        //transaction - [{date: 4/12, amount: 12}, {date: 4/10, amount: 10} {date: 4/12, amount: 10}]
        String ccNumber = payload[0].getCreditCardNumber();
        CreditCard creditCard = this.findCreditCardByNumber(ccNumber);
        Arrays.sort(payload, Comparator.comparing(UpdateBalancePayload::getTransactionTime)); // sort using time
        System.out.println("first entry after sorting - " + payload[0].getTransactionTime());

        //case 1 - if card has not any balance history -
        System.out.println("total size" + creditCard.getBalanceHistoryList().size());
        if (creditCard.getBalanceHistoryList().isEmpty()) {
            Double sum = payload[0].getTransactionAmount(); // 10
            Instant currInstant = payload[0].getTransactionTime(); // 4/10
            String currDate = this.dateFormat(Date.from(payload[0].getTransactionTime()));
            List<BalanceHistory> history = new ArrayList<>();

            for (int i = 1; i < payload.length; i++) {
                if(!currDate.equals(this.dateFormat(Date.from(payload[i].getTransactionTime())))){
                    BalanceHistory balanceHistory = new BalanceHistory();
                    balanceHistory.setBalance(sum);
                    balanceHistory.setDate(currInstant);
                    balanceHistory.setCreditCard(creditCard);
                    currInstant = payload[i].getTransactionTime();
                    currDate = this.dateFormat(Date.from(payload[i].getTransactionTime()));
                    history.add(balanceHistory);
                }

                sum = sum + payload[i].getTransactionAmount(); //20
            }
            BalanceHistory balanceHistory = new BalanceHistory();
            balanceHistory.setBalance(sum);
            balanceHistory.setDate(currInstant); // for the last entry
            balanceHistory.setCreditCard(creditCard);
            history.add(balanceHistory);
            Collections.reverse(history);
            Instant today = Instant.now(); // check only date part
            String todayDate = this.dateFormat(Date.from(today));
            System.out.println(Date.from(today));

            if (!todayDate.equals(Date.from(history.get(0).getDate()))) {
                Double amount = history.get(0).getBalance();
                BalanceHistory balanceHistory1 = new BalanceHistory();
                balanceHistory1.setBalance(amount);
                balanceHistory1.setDate(today);
                balanceHistory1.setCreditCard(creditCard);
                history.add(0, balanceHistory1);

            }
            creditCard.setBalanceHistoryList(history);
            this.creditCardRepository.save(creditCard);

            return history;
        }

        List<BalanceHistory> currentBalanceHistory = creditCard.getBalanceHistoryList();
        Map<String,Double> map = new TreeMap<>();
        for(BalanceHistory hist:currentBalanceHistory){
            map.put(this.dateFormat(Date.from(hist.getDate())),hist.getBalance());
        }
        for(UpdateBalancePayload txn:payload){ // add new history date
            String txnDate = this.dateFormat(Date.from(txn.getTransactionTime()));
            if(!map.containsKey(txnDate)){
                BalanceHistory balanceHistory = new BalanceHistory();

                balanceHistory.setBalance(0.0);
                balanceHistory.setDate(txn.getTransactionTime());
                balanceHistory.setCreditCard(creditCard);
                currentBalanceHistory.add(balanceHistory);
            }
        }
        Collections.sort(currentBalanceHistory,Comparator.comparing(BalanceHistory::getDate)); // sort using dates
        for(int i=1;i<currentBalanceHistory.size();i++){
            if(currentBalanceHistory.get(i).getId()==0){
                Double balance = currentBalanceHistory.get(i-1).getBalance(); // set prev date balance to new entries
                currentBalanceHistory.get(i).setBalance(balance);
            }
        }
        for(UpdateBalancePayload txn:payload){
            String date = this.dateFormat(Date.from(txn.getTransactionTime()));
            Date date1 = this.convertToDate(date);
            for(BalanceHistory hist: currentBalanceHistory){
                String dt = this.dateFormat(Date.from(hist.getDate()));
                Date dt1 = this.convertToDate(dt);
                if(date1.before(dt1) || date1.equals(dt1)){
                    Double bal = hist.getBalance();
                    hist.setBalance(bal+txn.getTransactionAmount());
                }


            }
        }


        Collections.reverse(currentBalanceHistory);
        Instant today = Instant.now();
        String todayDate = this.dateFormat(Date.from(today));
        System.out.println(Date.from(today));

        if (!todayDate.equals(Date.from(currentBalanceHistory.get(0).getDate()))) {
            Double amount = currentBalanceHistory.get(0).getBalance();
            BalanceHistory balanceHistory1 = new BalanceHistory();
            balanceHistory1.setBalance(amount);
            balanceHistory1.setDate(today);
            balanceHistory1.setCreditCard(creditCard);
            currentBalanceHistory.add(0, balanceHistory1);

        }
        creditCard.setBalanceHistoryList(currentBalanceHistory);
        this.creditCardRepository.save(creditCard);


        return currentBalanceHistory;

    }

    private String dateFormat(Date date){
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String outputDateStr = outputDateFormat.format(date);
        return outputDateStr;
    }

    public Date convertToDate(String date) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

        try {
            Date date1 = dateFormat.parse(date);
            return date1;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}


