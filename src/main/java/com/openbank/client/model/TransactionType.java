package com.openbank.client.model;

import lombok.Data;

@Data
public class TransactionType {

    private double totalAmt;
    private String transactionType;
    private String transactionCurrency;
}