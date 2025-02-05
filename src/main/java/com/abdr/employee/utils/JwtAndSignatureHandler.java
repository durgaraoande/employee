package com.abdr.employee.utils;

public class JwtAndSignatureHandler extends RuntimeException {
    public JwtAndSignatureHandler(String message) {
        super(message);
    }

    public JwtAndSignatureHandler(String message, Throwable cause) {
        super(message, cause);
    }
}
