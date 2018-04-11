package com.alexeymatveev.buxassignment.model;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class Result<T> {

    private boolean successful;

    private T data;

    private String errorMsg;

    public Result() {
    }

    private Result(boolean successful, T data, String errorMsg) {
        this.successful = successful;
        this.data = data;
        this.errorMsg = errorMsg;
    }

    public static <T> Result<T> ok() {
        return Result.ok(null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(true, data, null);
    }

    public static <T> Result<T> fail() {
        return Result.fail(null);
    }

    public static <T> Result<T> fail(String errorMsg) {
        return new Result<>(false, null, errorMsg);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public T getData() {
        return data;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
