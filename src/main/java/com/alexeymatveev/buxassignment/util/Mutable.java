package com.alexeymatveev.buxassignment.util;

/**
 * Mutable wrapper over immutable objects like primitives or strings.
 * @param <T> object type
 */
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
