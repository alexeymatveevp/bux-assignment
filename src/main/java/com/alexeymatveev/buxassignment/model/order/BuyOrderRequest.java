package com.alexeymatveev.buxassignment.model.order;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class BuyOrderRequest {

    private String productId;

    private Price investingAmount;

    private Integer leverage;

    private DirectionType direction;

    public BuyOrderRequest() {
    }

    public BuyOrderRequest(String productId, Price investingAmount, Integer leverage, DirectionType direction) {
        this.productId = productId;
        this.investingAmount = investingAmount;
        this.leverage = leverage;
        this.direction = direction;
    }

    public String getProductId() {
        return productId;
    }

    public Price getInvestingAmount() {
        return investingAmount;
    }

    public Integer getLeverage() {
        return leverage;
    }

    public DirectionType getDirection() {
        return direction;
    }
}
