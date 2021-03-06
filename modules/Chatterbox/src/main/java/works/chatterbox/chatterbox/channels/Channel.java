/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package works.chatterbox.chatterbox.channels;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import works.chatterbox.chatterbox.channels.radius.Radius;
import works.chatterbox.chatterbox.channels.worlds.WorldRecipients;
import works.chatterbox.chatterbox.wrappers.CPlayer;

import java.util.Set;

/**
 * A channel, which is a group of members that can all talk amongst each other. Any member may be part of multiple
 * channels, each of which will send him messages. A player can send a message to any channel he is a part of.
 */
public interface Channel {

    /**
     * Adds a member to this channel.
     *
     * @param cp Member to add
     */
    void addMember(@NotNull final CPlayer cp);

    /**
     * Gets the format String for this channel, in valid Rythm syntax.
     *
     * @return Format String
     */
    @NotNull
    String getFormat();

    @Nullable
    String getJSONSection(@NotNull final String sectionName);

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

    @Nullable
    String getRecipientSection(@NotNull final String sectionName);

    /**
     * Gets this channel's tag.
     *
     * @return Tag
     */
    @NotNull
    String getTag();

    /**
     * Gets the worlds that should receive messages from this channel.
     *
     * @return WorldRecipients
     */
    @NotNull
    WorldRecipients getWorldRecipients();

    /**
     * Gets whether the channel is permanent or not. A permanent channel is one that is always joined by every player.
     * It cannot be left.
     *
     * @return true if permanent channel, false if not
     */
    boolean isPermanent();

    /**
     * Removes a member from this channel.
     *
     * @param cp Member to remove
     */
    void removeMember(@NotNull final CPlayer cp);

}
