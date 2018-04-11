package com.alexeymatveev.buxassignment.model.message;

import java.util.List;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class SubscribeMsg {

    List<String> subscribeTo;

    List<String> unsubscribeFrom;

    public SubscribeMsg(List<String> subscribeTo, List<String> unsubscribeFrom) {
        this.subscribeTo = subscribeTo;
        this.unsubscribeFrom = unsubscribeFrom;
    }

    public List<String> getSubscribeTo() {
        return subscribeTo;
    }

    public List<String> getUnsubscribeFrom() {
        return unsubscribeFrom;
    }
}
