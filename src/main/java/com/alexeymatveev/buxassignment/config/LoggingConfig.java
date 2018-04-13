package com.alexeymatveev.buxassignment.config;

import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Init SLF4J logging bridge. Required to be called at app startup.
 *
 * Created by Alexey Matveev on 4/10/2018.
 */
public class LoggingConfig {

    private static HttpLoggingInterceptor.Level httpLoggingLevel = HttpLoggingInterceptor.Level.BASIC;

    public static void init() {
        try {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void setHttpLoggingLevel(HttpLoggingInterceptor.Level level) {
        httpLoggingLevel = level;
    }

    public static HttpLoggingInterceptor.Level getHttpLoggingLevel() {
        return httpLoggingLevel;
    }

}
