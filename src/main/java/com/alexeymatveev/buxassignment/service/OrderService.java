package com.alexeymatveev.buxassignment.service;

import com.alexeymatveev.buxassignment.config.AppConfig;
import com.alexeymatveev.buxassignment.config.CommonObjectMapper;
import com.alexeymatveev.buxassignment.config.LoggingConfig;
import com.alexeymatveev.buxassignment.model.ErrorResponse;
import com.alexeymatveev.buxassignment.model.Result;
import com.alexeymatveev.buxassignment.model.order.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private String accessToken = AppConfig.getInstance().getString("authorization.token");

    private String buyOrderUrl = AppConfig.getInstance().getString("buy.order.url");

    private String sellOrderUrl = AppConfig.getInstance().getString("sell.order.url");

    private ObjectMapper objectMapper = new CommonObjectMapper();

    public static final MediaType JSON_MT = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;

    public OrderService() {
        // send headers with every order request
        // add logging interceptor
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(LoggingConfig.getHttpLoggingLevel());
        client = new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                            .addHeader("Authorization", accessToken)
                            .addHeader("Accept-Language", "nl-NL,en;q=0.8")
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Accept", "application/json")
                            .build()))
                .addInterceptor(logging)
                .build();
    }

    public Result<BuyOrderResponse> buyOrder(String productId, Float amount, Integer leverage, DirectionType directionType) {
        BuyOrderRequest order = new BuyOrderRequest(productId, new Price(CurrencyType.BUX, 2, amount), leverage, directionType);
        try {
            Request request = new Request.Builder()
                    .url(buyOrderUrl)
                    .post(RequestBody.create(JSON_MT, objectMapper.writeValueAsBytes(order)))
                    .build();
            Response response = client.newCall(request).execute();
            return tryParseResponse(response, BuyOrderResponse.class);
        } catch (IOException e) {
            LOGGER.error("Error sending request", e);
        }
        return Result.fail();
    }

    public Result<SellOrderResponse> sellOrder(String positionId) {
        try {
            Request request = new Request.Builder()
                    .url(sellOrderUrl + positionId)
                    .delete()
                    .build();
            Response response = client.newCall(request).execute();
            return tryParseResponse(response, SellOrderResponse.class);
        } catch (IOException e) {
            LOGGER.error("Error sending request", e);
        }
        return null;
    }

    private <T> Result<T> tryParseResponse(Response response, Class<T> responseType) {
        ResponseBody body = response.body();
        if (body != null) {
            String bodyString;
            try {
                bodyString = body.string();
                try {
                    JsonNode jsonNode = objectMapper.readTree(bodyString);
                    if (jsonNode.get("id") != null) {
                        return Result.ok(objectMapper.readValue(bodyString, responseType));
                    } else {
                        ErrorResponse errorResponse = objectMapper.readValue(bodyString, ErrorResponse.class);
                        return Result.fail(errorResponse.getMessage());
                    }
                } catch (Exception e) {
                    LOGGER.info("Error during json parsing", e);
                }
            } catch (IOException e) {
                LOGGER.error("Cannot decode response body", e);
            }
        } else {
            LOGGER.error("Response body empty, expected valid json response");
        }
        return Result.fail();
    }

}
