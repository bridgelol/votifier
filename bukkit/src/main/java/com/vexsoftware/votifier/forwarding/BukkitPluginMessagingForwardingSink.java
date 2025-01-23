package com.vexsoftware.votifier.forwarding;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vexsoftware.votifier.support.forwarding.AbstractPluginMessagingForwardingSink;
import com.vexsoftware.votifier.support.forwarding.ForwardedVoteListener;
import com.vexsoftware.votifier.support.forwarding.exception.InvalidSecretKeyException;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by Joe Hirschfeld on 10/20/2015.
 */
public class BukkitPluginMessagingForwardingSink extends AbstractPluginMessagingForwardingSink implements PluginMessageListener {

    private static final int VOTE_BRUTEFORCE_THRESHOLD = 25;
    private final Plugin p;
    private final String channel;
    private final Cache<UUID, Integer> voteCounter = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.SECONDS).build();

    public BukkitPluginMessagingForwardingSink(String secret, Plugin p, String channel, ForwardedVoteListener listener) {
        super(secret, listener);
        Validate.notNull(channel, "Channel cannot be null.");
        this.channel = channel;
        Bukkit.getMessenger().registerIncomingPluginChannel(p, channel, this);
        this.p = p;
    }

    @Override
    public void halt() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(p, channel, this);
    }

    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        try {
            this.handlePluginMessage(bytes);

            int count = voteCounter.getIfPresent(player.getUniqueId()) == null ? 1 : voteCounter.get(player.getUniqueId(), () -> 0) + 1;

            if (count > VOTE_BRUTEFORCE_THRESHOLD) {
                p.getLogger().log(Level.SEVERE, "Votifier exploit attempt (bruteforce) by " + player.getName());
                return;
            }

            voteCounter.put(player.getUniqueId(), count);
        } catch (InvalidSecretKeyException e) {
            p.getLogger().log(Level.SEVERE, "Votifier exploit attempt (invalid key) by " + player.getName());
        } catch (Exception e) {
            p.getLogger().log(Level.SEVERE, "There was an unknown error when processing a forwarded vote.", e);
        }
    }
}
