/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package works.chatterbox.chatterbox.api.player;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import works.chatterbox.chatterbox.Chatterbox;
import works.chatterbox.chatterbox.wrappers.CPlayer;
import works.chatterbox.chatterbox.wrappers.UUIDCPlayer;

import java.util.UUID;

/**
 * The player API has methods relating to players in Chatterbox.
 */
public class PlayerAPI {

    private final Chatterbox chatterbox;

    private final LoadingCache<UUID, CPlayer> players = CacheBuilder.newBuilder()
        .softValues()
        .removalListener(new RemovalListener<UUID, CPlayer>() {
            @Override
            public void onRemoval(@NotNull final RemovalNotification<UUID, CPlayer> notification) {
                final UUID uuid = notification.getKey();
                if (uuid == null) return;
                // Invalidate the titles being kept with this player
                PlayerAPI.this.chatterbox.getAPI().getTitleAPI().invalidate(uuid);
            }
        })
        .build(new CacheLoader<UUID, CPlayer>() {
            @Override
            @NotNull
            public CPlayer load(@NotNull final UUID key) throws Exception {
                return new UUIDCPlayer(key, PlayerAPI.this.chatterbox);
            }
        });

    public PlayerAPI(@NotNull final Chatterbox chatterbox) {
        Preconditions.checkNotNull(chatterbox, "chatterbox was null");
        this.chatterbox = chatterbox;
    }

    /**
     * Gets a CPlayer (Chatterbox Player) wrapper for the given UUID, representative of a Minecraft account's UUID. This
     * wrapper is used to get Chatterbox-specific information from one easy class. These are cached using soft
     * references.
     *
     * @param uuid UUID of the player to get the CPlayer of
     * @return CPlayer
     */
    @NotNull
    public CPlayer getCPlayer(@NotNull final UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid was null");
        return this.players.getUnchecked(uuid);
    }

    /**
     * Calls {@link #getCPlayer(UUID)} with {@link OfflinePlayer#getUniqueId()}.
     *
     * @param player OfflinePlayer to get CPlayer of
     * @return CPlayer
     * @see #getCPlayer(UUID)
     */
    @NotNull
    public CPlayer getCPlayer(@NotNull final OfflinePlayer player) {
        Preconditions.checkNotNull(player, "player was null");
        return this.getCPlayer(player.getUniqueId());
    }

}
