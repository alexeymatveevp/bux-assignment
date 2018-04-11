package com.alexeymatveev.buxassignment.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class AppConfig {

    public static Config getInstance() {
        return AppConfigHolder.instance;
    }

    private static class AppConfigHolder {
        static final Config instance = ConfigFactory.load();

    }

}
