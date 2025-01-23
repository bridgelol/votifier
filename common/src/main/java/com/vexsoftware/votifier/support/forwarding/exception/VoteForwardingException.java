package com.vexsoftware.votifier.support.forwarding.exception;

public abstract class VoteForwardingException extends RuntimeException {
    public VoteForwardingException(String message) {
        super(message);
    }

    public VoteForwardingException(String message, Throwable cause) {
        super(message, cause);
    }
}
