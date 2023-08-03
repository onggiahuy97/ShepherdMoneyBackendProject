package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@RestController
public class CreditCardController {

    // TODO: wire in CreditCard repository here using @Autowired annotation
    
    private CreditCardRepository creditCardRepository;

    @Autowired(required = false)
    public CreditCardController(CreditCardRepository creditCardRepository) {
        this.creditCardRepository = creditCardRepository;
    }

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // TODO: Create a credit card entity, and then associate that credit card with user with given userId
        //       Return 200 OK with the credit card id if the user exists and credit card is successfully associated with the user
        //       Return other appropriate response code for other exception cases
        //       Do not worry about validating the card number, assume card number could be any arbitrary format and length

        try {
            CreditCard creditCard = new CreditCard();
            creditCard.setIssuanceBank(payload.getCardIssuanceBank());
            creditCard.setNumber(payload.getCardNumber());
            creditCard.setId(payload.getUserId());

            creditCardRepository.save(creditCard);

            return ResponseEntity.ok(creditCard.getId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        
    }

    @GetMapping("/credit-card/all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser() {
        // TODO: return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null
        List<CreditCard> creditCards = creditCardRepository.findAll();
        List<CreditCardView> creditCardViews = creditCards.stream()
        .map(this::mapToCreditCardView)
        .collect(Collectors.toList());

        return ResponseEntity.ok(creditCardViews);
    }

    // Helper method to map CreditCard to CreditCardView
    private CreditCardView mapToCreditCardView(CreditCard creditCard) {
        return CreditCardView.builder()
                .issuanceBank(creditCard.getIssuanceBank())
                .number(creditCard.getNumber())
                .build();
    }

    @GetMapping("/credit-card")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // TODO: Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request
        List<CreditCard> creditCards = creditCardRepository.findAll();

    // Search for the credit card with the given creditCardNumber
        for (CreditCard creditCard : creditCards) {
            if (creditCardNumber.equals(creditCard.getNumber())) {
                // If the credit card is found, return the associated user id in a 200 OK response
                return ResponseEntity.ok(creditCard.getId());
            }
        }

        // If no such user exists, return 400 Bad Request
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<String> postMethodName(@RequestBody UpdateBalancePayload[] payload) {
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a transaction of {date: 4/10, amount: 10}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 110}]
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.
        for (UpdateBalancePayload update : payload) {
            String creditCardNumber = update.getCreditCardNumber();
            Instant transactionTime = update.getTransactionTime();
            double transactionAmount = update.getTransactionAmount();

            // Retrieve the credit card entity from the database based on the credit card number
            CreditCard creditCard = creditCardRepository.findAll().stream()
                    .filter(card -> card.getNumber().equals(creditCardNumber))
                    .findFirst()
                    .orElse(null);

            if (creditCard == null) {
                // If no such credit card exists, return 400 Bad Request
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Credit card not found: " + creditCardNumber);
            }

            double currentBalance = creditCard.getBalanceHistory().isEmpty() ? 0 : creditCard.getBalanceHistory().get(0).getBalance();

            // Update the credit card's balance history with the new transaction data
            BalanceHistory newBalanceEntry = new BalanceHistory();
            newBalanceEntry.setDate(transactionTime);
            newBalanceEntry.setBalance(currentBalance + transactionAmount);

            creditCard.getBalanceHistory().add(0, newBalanceEntry);

            creditCardRepository.save(creditCard);
        }

        // Save the updated credit card entities back to the database (assuming you are using JPA or some other ORM)
        // creditCardRepository.saveAll(Arrays.asList(creditCards));

        // Return 200 OK if the update is done and successful
        return ResponseEntity.ok("Balance history updated successfully.");
        
    }
    
}
