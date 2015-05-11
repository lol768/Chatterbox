package org.royaldev.chatterbox.wrappers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CPlayers {

    private final static LoadingCache<UUID, CPlayer> players = CacheBuilder.newBuilder()
        .softValues()
        .build(new CacheLoader<UUID, CPlayer>() {
            @Override
            @NotNull
            public CPlayer load(@NotNull final UUID key) throws Exception {
                return new UUIDCPlayer(key);
            }
        });

    public static CPlayer getCPlayer(@NotNull final UUID uuid) {
        return CPlayers.players.getUnchecked(uuid);
    }

    public static CPlayer getCPlayer(@NotNull final OfflinePlayer player) {
        return CPlayers.getCPlayer(player.getUniqueId());
    }

}
