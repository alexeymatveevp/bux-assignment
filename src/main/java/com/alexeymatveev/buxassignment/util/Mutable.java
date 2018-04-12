package com.alexeymatveev.buxassignment.util;

public class Mutable<T> {

    private T value;

    public Mutable(T value) {
        this.value = value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
