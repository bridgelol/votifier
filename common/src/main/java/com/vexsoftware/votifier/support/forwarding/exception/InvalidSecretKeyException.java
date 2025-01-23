package com.vexsoftware.votifier.support.forwarding.exception;

import com.vexsoftware.votifier.model.Vote;

public class InvalidSecretKeyException extends VoteForwardingException {
    public InvalidSecretKeyException(String message) {
        super(message);
    }

    public InvalidSecretKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSecretKeyException(Vote vote) {
        super("Invalid secret key for vote: " + vote.serialize());
    }
}
