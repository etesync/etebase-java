package com.etebase.client.exceptions;

public class TemporaryServerErrorException extends HttpException {
    public TemporaryServerErrorException(String message) {
        super(message);
    }
}
