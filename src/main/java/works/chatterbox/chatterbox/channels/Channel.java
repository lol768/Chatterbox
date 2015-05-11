package works.chatterbox.chatterbox.channels;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import works.chatterbox.chatterbox.channels.radius.Radius;
import works.chatterbox.chatterbox.wrappers.CPlayer;

import java.util.Set;

/**
 * A channel, which is a group of members that can all talk amongst each other. Any member may be part of multiple
 * channels, each of which will send him messages. A player can send a message to any channel he is a part of.
 */
public interface Channel {

    /**
     * Gets all the members of this channel.
     *
     * @return Set of members
     */
    @NotNull
    Set<CPlayer> getMembers();

    /**
     * Gets this channel's name.
     *
     * @return Name
     */
    @NotNull
    String getName();

    /**
     * Gets this channel's radius, if enabled. If there is no radius, this should return null.
     *
     * @return Radius or null
     */
    @Nullable
    Radius getRadius();

    /**
     * Gets this channel's tag.
     *
     * @return Tag
     */
    @NotNull
    String getTag();

}