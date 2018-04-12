package com.alexeymatveev.buxassignment.model.message;

public class ErrorMsg extends BodyMsg {

    private String developerMessage;

    private String errorCode;

    public ErrorMsg() {
    }

    public ErrorMsg(String developerMessage, String errorCode) {
        this.developerMessage = developerMessage;
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "ErrorMsg{" +
                "developerMessage='" + developerMessage + '\'' +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
