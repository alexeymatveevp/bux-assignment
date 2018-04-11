package com.alexeymatveev.buxassignment.model.order;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class BuyOrderResponse {

    private String id;

    private String positionId;

//    private Product product; // dont need it in this app

    private Price investingAmount;

    private Price price;

    private Integer leverage;

    private DirectionType direction;

    private PositionType type;

    private Instant dateCreated;

    public BuyOrderResponse() {
    }

    public BuyOrderResponse(String id, String positionId, Price investingAmount, Price price, Integer leverage, DirectionType direction, PositionType type, Instant dateCreated) {
        this.id = id;
        this.positionId = positionId;
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
