package com.alexeymatveev.buxassignment.service;

import com.alexeymatveev.buxassignment.config.AppConfig;
import com.alexeymatveev.buxassignment.config.LoggingConfig;
import com.alexeymatveev.buxassignment.model.Result;
import com.alexeymatveev.buxassignment.model.order.BuyOrderResponse;
import com.alexeymatveev.buxassignment.model.order.DirectionType;
import com.alexeymatveev.buxassignment.model.order.SellOrderResponse;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class TestOrderService {

    private static List<String> productIds = Arrays.asList(AppConfig.getInstance().getString("sample.product.ids").split(","));

    @BeforeClass
    public static void init() {
        LoggingConfig.init();
        LoggingConfig.setHttpLoggingLevel(HttpLoggingInterceptor.Level.BODY);
    }

    @Test
    public void testBuySellOrder() {
        OrderService orderService = new OrderService();

        Result<BuyOrderResponse> buyOrderResult = orderService.buyOrder(productIds.get(0), 200f, 1, DirectionType.BUY);
        assertTrue(buyOrderResult.isSuccessful());
        assertNotNull("Buy order response is null", buyOrderResult.getData());
        String positionId = buyOrderResult.getData().getPositionId();
        assertNotNull("Position ID is null in buy order response", positionId);

        Result<SellOrderResponse> sellOrderResult = orderService.sellOrder(positionId);
        assertTrue(sellOrderResult.isSuccessful());
        assertNotNull("Sell order response is null", sellOrderResult.getData());
    }
}
