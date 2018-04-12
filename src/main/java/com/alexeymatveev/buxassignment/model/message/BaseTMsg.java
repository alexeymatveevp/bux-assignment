package com.alexeymatveev.buxassignment.model.message;

public class BaseTMsg<T extends BodyMsg> {

    private MsgType type;

    private T body;

    public BaseTMsg() {
    }

    public BaseTMsg(MsgType type, T body) {
        this.type = type;
        this.body = body;
    }

    public MsgType getT() {
        return type;
    }

    public T getBody() {
        return body;
    }
}
