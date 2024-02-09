package com.vexsoftware.votifier.forwarding;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.support.forwarding.ForwardedVoteListener;
import com.vexsoftware.votifier.support.forwarding.ForwardingVoteSink;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.CharArrayReader;
import java.io.IOException;

public class RedisForwardingSink extends JedisPubSub implements ForwardingVoteSink {

    private final Jedis jedis;
    private final ForwardedVoteListener listener;
    private final Thread thread;

    public RedisForwardingSink(String redisUri, ForwardedVoteListener listener) {
        this.jedis = new Jedis(redisUri);
        this.listener = listener;
        this.thread = new Thread(() -> jedis.subscribe(this, "votifier"));

        thread.start();
    }

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals("votifier")) {
            try (CharArrayReader reader = new CharArrayReader(message.toCharArray())) {
                JsonReader r = new JsonReader(reader);
                r.setLenient(true);
                while (r.peek() != JsonToken.END_DOCUMENT) {
                    r.beginObject();
                    JsonObject o = new JsonObject();

                    while (r.hasNext()) {
                        String name = r.nextName();
                        if (r.peek() == JsonToken.NUMBER)
                            o.add(name, new JsonPrimitive(r.nextLong()));
                        else
                            o.add(name, new JsonPrimitive(r.nextString()));
                    }
                    r.endObject();

                    Vote v = new Vote(o);
                    listener.onForward(v);
                }
            } catch (IOException e) {
                e.printStackTrace(); // Should never happen.
            }
        }
    }

    @Override
    public void halt() {
        thread.interrupt();
    }
}
