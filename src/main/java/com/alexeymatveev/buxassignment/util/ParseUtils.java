package com.alexeymatveev.buxassignment.util;

public class ParseUtils {

    public static Float parseFloatOrDefault(String s, Float defaultValue) {
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
