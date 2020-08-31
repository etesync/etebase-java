package com.etebase.client.exceptions;

public class NotFoundException extends HttpException {
    public NotFoundException(String message) {
        super(message);
    }
}
