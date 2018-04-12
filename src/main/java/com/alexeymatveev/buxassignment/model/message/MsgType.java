package com.alexeymatveev.buxassignment.model.message;

public enum MsgType {
    CONNECT_CONNECTED("connect.connected"),
    CONNECT_FAILED("connect.failed"),
    TRADING_QUOTE("trading.quote"),
    UNKNOWN("");

    private String textual;

    MsgType(String textual) {
        this.textual = textual;
    }

    public String getTextual() {
        return textual;
    }

    public static MsgType parse(String textual) {
        for (MsgType msgType : MsgType.values()) {
            if (msgType.textual.equals(textual)) {
                return msgType;
            }
        }
        return UNKNOWN;
    }
}
