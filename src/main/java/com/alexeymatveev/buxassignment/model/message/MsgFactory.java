package com.alexeymatveev.buxassignment.model.message;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class MsgFactory {

    public static SubscribeMsg createSubscribeMsg(String... productIds) {
        return new SubscribeMsg(getPrefixedSubscriptionProductIds(productIds), null);
    }

    public static SubscribeMsg createUnsubscribeMsg(String... productIds) {
        return new SubscribeMsg(null, getPrefixedSubscriptionProductIds(productIds));
    }

    private static List<String> getPrefixedSubscriptionProductIds(String... productIds) {
        List<String> result = null;
        if (productIds != null) {
            result = Arrays.stream(productIds).map(id -> "trading.product." + id).collect(Collectors.toList());
        }
        return result;
    }
}
