package com.alexeymatveev.buxassignment.model.order;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class Price {

    private CurrencyType currency;

    private Integer decimals;

    private Float amount;

    public Price() {
    }

    public Price(CurrencyType currency, Integer decimals, Float amount) {
        this.currency = currency;
        this.decimals = decimals;
        this.amount = amount;
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    public Integer getDecimals() {
        return decimals;
    }

    public Float getAmount() {
        return amount;
    }
}
