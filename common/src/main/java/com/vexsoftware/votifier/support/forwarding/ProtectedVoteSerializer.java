package com.vexsoftware.votifier.support.forwarding;

import com.google.gson.JsonParser;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.support.forwarding.exception.InvalidSecretKeyException;
import com.vexsoftware.votifier.support.forwarding.exception.VoteForwardingException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

public class ProtectedVoteSerializer {

    private static final JsonParser JSON_PARSER = new JsonParser();

    public static byte[] serializeVotes(String secret, Vote... votes) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        try {
            dataOutputStream.writeUTF(secret);
            dataOutputStream.writeInt(votes.length);

            for (Vote vote : votes) {
                dataOutputStream.writeUTF(vote.serialize().toString());
            }

            return outputStream.toByteArray();
        } finally {
            dataOutputStream.close();
            outputStream.close();
        }
    }

    public static Collection<Vote> deserializeVotes(
            String expectedSecret,
            byte[] data
    ) throws IOException, VoteForwardingException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        try {
            String secret = dataInputStream.readUTF();

            if (!expectedSecret.equals(secret)) {
                throw new InvalidSecretKeyException("Invalid secret key: " + secret);
            }

            Collection<Vote> votes = new ArrayList<>();

            int votesCount = dataInputStream.readInt();

            for (int i = 0; i < votesCount; i++) {
                votes.add(new Vote(JSON_PARSER.parse(dataInputStream.readUTF()).getAsJsonObject()));
            }

            return votes;
        } finally {
            dataInputStream.close();
            inputStream.close();
        }
    }
}
