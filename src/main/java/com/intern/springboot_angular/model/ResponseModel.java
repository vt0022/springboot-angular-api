package com.intern.springboot_angular.model;

public class ResponseModel {
    boolean isSuccessful;
    String message;
    ProductModel data;

    public ResponseModel() {
    }

    public ResponseModel(boolean isSuccessful, String message, ProductModel data) {
        this.isSuccessful = isSuccessful;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessfull(boolean successfull) {
        isSuccessful = successfull;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ProductModel getData() {
        return data;
    }

    public void setData(ProductModel data) {
        this.data = data;
    }
}
