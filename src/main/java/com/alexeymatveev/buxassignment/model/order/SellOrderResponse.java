package com.alexeymatveev.buxassignment.model.order;

import java.time.Instant;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class SellOrderResponse {

    private String id;

    private String positionId;

//    private Product product; // dont need it in this app

    private Price profitAndLoss;

    private Price investingAmount;

    private Price price;

    private Integer leverage;

    private DirectionType direction;

    private PositionType type;

    private Instant dateCreated;

    public SellOrderResponse() {
    }

    public SellOrderResponse(String id, String positionId, Price profitAndLoss, Price investingAmount, Price price, Integer leverage, DirectionType direction, PositionType type, Instant dateCreated) {
        this.id = id;
        this.positionId = positionId;
        this.profitAndLoss = profitAndLoss;
        this.investingAmount = investingAmount;
        this.price = price;
        this.leverage = leverage;
        this.direction = direction;
        this.type = type;
        this.dateCreated = dateCreated;
    }

    public String getId() {
        return id;
    }

    public String getPositionId() {
        return positionId;
    }

    public Price getProfitAndLoss() {
        return profitAndLoss;
    }

    public Price getInvestingAmount() {
        return investingAmount;
    }

    public Price getPrice() {
        return price;
    }

    public Integer getLeverage() {
        return leverage;
    }

    public DirectionType getDirection() {
        return direction;
    }

    public PositionType getType() {
        return type;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }
}
