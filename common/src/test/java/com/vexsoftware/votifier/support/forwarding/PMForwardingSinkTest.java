package com.vexsoftware.votifier.support.forwarding;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.util.TokenUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PMForwardingSinkTest {

    @Test
    public void testSuccessfulMultiDecode() throws Exception {
        List<Vote> receivedVotes = new ArrayList<>();
        ForwardedVoteListener vl = receivedVotes::add;

        List<Vote> sentVotes = new ArrayList<>(Arrays.asList(
                new Vote("serviceA", "usernameA", "1.1.1.1", "1546300800"),
                new Vote("serviceB", "usernameBBBBBBB", "1.2.23.4", "1514764800")
        ));

        String secret = TokenUtil.newToken();

        AbstractPluginMessagingForwardingSink sink = new AbstractPluginMessagingForwardingSink(secret, vl) {
            @Override
            public void halt() {

            }
        };

        sink.handlePluginMessage(ProtectedVoteSerializer.serializeVotes(secret, sentVotes.toArray(new Vote[0])));

        assertEquals(sentVotes.size(), receivedVotes.size());

        for (int i = 0; i < receivedVotes.size(); i++) {
            assertEquals(sentVotes.get(i), receivedVotes.get(i));
        }
    }
}
