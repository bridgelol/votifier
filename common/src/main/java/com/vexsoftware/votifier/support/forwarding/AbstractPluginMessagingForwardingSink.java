package com.vexsoftware.votifier.support.forwarding;

import com.vexsoftware.votifier.support.forwarding.exception.VoteForwardingException;

import java.io.IOException;

public abstract class AbstractPluginMessagingForwardingSink implements ForwardingVoteSink {

    public AbstractPluginMessagingForwardingSink(String secret, ForwardedVoteListener listener) {
        this.secret = secret;
        this.listener = listener;
    }

    private final String secret;
    private final ForwardedVoteListener listener;

    public void handlePluginMessage(byte[] message) throws VoteForwardingException, IOException {
        ProtectedVoteSerializer.deserializeVotes(secret, message).forEach(listener::onForward);
    }
}
