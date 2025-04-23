package com.vexsoftware.votifier.forwarding;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.vexsoftware.votifier.NuVotifierBukkit;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.support.forwarding.ForwardedVoteListener;
import com.vexsoftware.votifier.support.forwarding.ForwardingVoteSink;
import com.vexsoftware.votifier.support.forwarding.redis.RedisConstants;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.JedisPubSub;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.Locale;

public class RedisForwardingSink extends JedisPubSub implements ForwardingVoteSink, Listener {

    private final NuVotifierBukkit votifier;
    private final boolean acceptOffline;
    private final JedisPooled jedis;
    private final ForwardedVoteListener listener;
    private final Thread thread;

    public RedisForwardingSink(NuVotifierBukkit votifier, boolean acceptOffline, String redisUri, ForwardedVoteListener listener) {
        this.votifier = votifier;
        this.acceptOffline = acceptOffline;
        this.jedis = new JedisPooled(redisUri);
        this.listener = listener;
        this.thread = new Thread(() -> jedis.subscribe(this, "votifier"));

        thread.start();
        Bukkit.getPluginManager().registerEvents(this, votifier);
    }

    // TODO: Maybe add a TTL to the online players key incase the server crashes?

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        jedis.sadd(RedisConstants.ONLINE_PLAYERS_KEY, event.getName().toLowerCase(Locale.ROOT));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(
                votifier,
                () -> jedis.srem(RedisConstants.ONLINE_PLAYERS_KEY, event.getPlayer().getName().toLowerCase(Locale.ROOT))
        );
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

                    if (acceptOffline || Bukkit.getPlayer(v.getUsername()) != null) {
                        listener.onForward(v);
                    }
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
