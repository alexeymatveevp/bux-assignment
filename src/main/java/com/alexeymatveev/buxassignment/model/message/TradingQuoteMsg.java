package com.alexeymatveev.buxassignment.model.message;

public class TradingQuoteMsg extends BodyMsg {

    private String securityId;

    private String currentPrice;

    public TradingQuoteMsg() {
    }

    public TradingQuoteMsg(String securityId, String currentPrice) {
        this.securityId = securityId;
        this.currentPrice = currentPrice;
    }

    @Override
    public String toString() {
        return "TradingQuoteMsg{" +
                "securityId='" + securityId + '\'' +
                ", currentPrice='" + currentPrice + '\'' +
                '}';
    }

    public String getSecurityId() {
        return securityId;
    }

    public String getCurrentPrice() {
        return currentPrice;
    }
}
