package com.etebase.client.exceptions;

public class ServerErrorException extends HttpException {
    public ServerErrorException(String message) {
        super(message);
    }
}
