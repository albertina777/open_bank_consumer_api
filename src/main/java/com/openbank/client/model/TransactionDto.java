package com.openbank.client.model;


import lombok.Data;

@Data
public class TransactionDto {

    private String id;
    private String accountId;
    private String counterPartyAccount;
    private String counterPartyName;
    private String counterPartyLogoPath;
    private String transactionAmount;
    private String transactionCurrency;
    private String transactionType;
    private String description;
}
