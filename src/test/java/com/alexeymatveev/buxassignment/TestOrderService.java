package com.alexeymatveev.buxassignment;

import com.alexeymatveev.buxassignment.config.LoggingConfig;
import com.alexeymatveev.buxassignment.model.Result;
import com.alexeymatveev.buxassignment.model.order.BuyOrderResponse;
import com.alexeymatveev.buxassignment.model.order.SellOrderResponse;
import com.alexeymatveev.buxassignment.service.OrderService;
import com.alexeymatveev.buxassignment.util.SomeData;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class TestOrderService {

    @BeforeClass
    public static void init() {
        LoggingConfig.init();
        LoggingConfig.setHttpLoggingLevel(HttpLoggingInterceptor.Level.BODY);
    }

    @Test
    public void testBuySellOrder() {
        OrderService orderService = new OrderService();

        Result<BuyOrderResponse> buyOrderResult = orderService.buyOrder(SomeData.SOME_PRODUCT_IDS.get(0), 200f, 1);
        assertTrue(buyOrderResult.isSuccessful());
        assertNotNull(buyOrderResult.getData());
        String positionId = buyOrderResult.getData().getPositionId();
        assertNotNull(positionId);

        Result<SellOrderResponse> sellOrderResult = orderService.sellOrder(positionId);
        assertTrue(sellOrderResult.isSuccessful());
        assertNotNull(sellOrderResult.getData());
    }
}
