package com.alexeymatveev.buxassignment.model;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class ErrorResponse {

    private String message;

    private String developerMessage;

    private String errorCode;

    public ErrorResponse() {
    }

    public ErrorResponse(String message, String developerMessage, String errorCode) {
        this.message = message;
        this.developerMessage = developerMessage;
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
