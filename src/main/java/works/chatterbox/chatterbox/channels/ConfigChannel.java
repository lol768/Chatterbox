package works.chatterbox.chatterbox.channels;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import ninja.leaping.configurate.ConfigurationNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import works.chatterbox.chatterbox.Chatterbox;
import works.chatterbox.chatterbox.channels.files.FormatFiles;
import works.chatterbox.chatterbox.channels.radius.Radius;
import works.chatterbox.chatterbox.channels.worlds.WorldRecipients;
import works.chatterbox.chatterbox.wrappers.CPlayer;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigChannel implements Channel {

    private final static FormatFiles formatFiles = new FormatFiles();
    private final ConfigurationNode node;
    private final Set<CPlayer> members = Sets.newHashSet();
    private final Chatterbox chatterbox;

    public ConfigChannel(@NotNull final Chatterbox chatterbox, @NotNull final String name) {
        Preconditions.checkNotNull(chatterbox, "chatterbox was null");
        Preconditions.checkNotNull(name, "name was null");
        this.chatterbox = chatterbox;
        this.node = chatterbox.getConfiguration().getNode("channels").getChildrenList().stream()
            .filter(node -> name.equalsIgnoreCase(node.getNode(ChannelConfiguration.NAME.getKey()).getString()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No channel by the name " + name));
    }

    public ConfigChannel(@NotNull final Chatterbox chatterbox, @NotNull final ConfigurationNode node) {
        Preconditions.checkNotNull(chatterbox, "chatterbox was null");
        Preconditions.checkNotNull(node, "node was null");
        this.chatterbox = chatterbox;
        this.node = node;
    }

    /**
     * Determines the correct node under {@code format} to use for the format. If a {@code file} node is specified, this
     * will load the file and return its contents. If not, this will return the contents of the {@code text} node. If
     * both are missing, this returns null.
     *
     * @return Format or null
     */
    @Nullable
    private String determineFormat() {
        final ConfigurationNode format = this.getConfiguration(ChannelConfiguration.FORMAT, node -> node);
        Preconditions.checkState(format != null, "No format specified for " + this.getName());
        if (!format.getNode(ChannelConfiguration.FORMAT_FILE.getKey()).isVirtual()) {
            return this.getFileFormat(format);
        }
        if (!format.getNode(ChannelConfiguration.FORMAT_TEXT.getKey()).isVirtual()) {
            return this.getTextFormat(format);
        }
        return null;
    }

    /**
     * Gets a configuration value from the given {@link ChannelConfiguration}.
     * <p>This gets all parent nodes, then the node corresponding to {@code configuration}, which is then checked to see
     * if it is virtual. If it is virtual, null is returned. If not, {@code function} is applied to the node. If
     * {@code function} returns null for the channel, it is applied to the master node. If it still returns null, null
     * is returned. If either the channel or master value is not null, that value is returned.
     *
     * @param configuration ChannelConfiguration that the value is desired of
     * @param function      Function to apply on the node represented by {@code configuration}, on channel and possibly master
     * @param <T>           Type that {@code function} returns, which this method returns
     * @return Desired value, from channel or master, or null
     */
    @Nullable
    private <T> T getConfiguration(@NotNull final ChannelConfiguration configuration, @NotNull final Function<ConfigurationNode, T> function) {
        Preconditions.checkNotNull(configuration, "configuration was null");
        Preconditions.checkNotNull(function, "function was null");
        return this.localOrMaster(node -> {
            ConfigurationNode child = node;
            for (final String parent : configuration.getParents()) {
                child = child.getNode(parent);
            }
            child = child.getNode(configuration.getKey());
            return child.isVirtual() ? null : function.apply(child);
        });
    }

    /**
     * Gets the cached contents of the {@code file} node in the given node. If there is any error, null will be
     * returned.
     *
     * @param formatNode Format node
     * @return File contents or null
     */
    @Nullable
    private String getFileFormat(@NotNull final ConfigurationNode formatNode) {
        Preconditions.checkNotNull(formatNode, "formatNode was null");
        return ConfigChannel.formatFiles.getFileContents(
            new File(this.chatterbox.getDataFolder(), formatNode.getNode(ChannelConfiguration.FORMAT_FILE.getKey()).getString())
        );
    }

    /**
     * Returns the value of the {@code text} node inside of the given node. If such a node is not present, null will be
     * returned.
     *
     * @param formatNode Format node
     * @return Node contents or null
     */
    @Nullable
    private String getTextFormat(@NotNull final ConfigurationNode formatNode) {
        Preconditions.checkNotNull(formatNode, "formatNode was null");
        return formatNode.getNode(ChannelConfiguration.FORMAT_TEXT.getKey()).getString();
    }

    @Nullable
    private <T> T localOrMaster(@NotNull final Function<ConfigurationNode, T> function) {
        Preconditions.checkNotNull(function, "function was null");
        final T local = function.apply(this.node);
        return local == null ? function.apply(this.chatterbox.getAPI().getChannelAPI().getMaster()) : local;
    }

    @Override
    public void addMember(@NotNull final CPlayer cp) {
        Preconditions.checkNotNull(cp, "cp was null");
        if (this.members.contains(cp)) return;
        this.members.add(cp);
        cp.joinChannel(this);
    }

    @NotNull
    @Override
    public String getFormat() {
        final String format = this.determineFormat();
        Preconditions.checkState(format != null, "No format specified for " + this.getName());
        return format;
    }

    @Override
    @Nullable
    public String getJSONSection(@NotNull final String sectionName) {
        Preconditions.checkNotNull(sectionName, "sectionName was null");
        final String section = this.getConfiguration(ChannelConfiguration.FORMAT_JSON, node -> node.getNode(sectionName).getString());
        if (section == null) return null;
        return section;
    }

    /**
     * {@inheritDoc}
     * <p>Note: the set returned is immutable.
     */
    @NotNull
    @Override
    public Set<CPlayer> getMembers() {
        return ImmutableSet.copyOf(this.members);
    }

    @NotNull
    @Override
    public String getName() {
        // May not use master
        final String name = this.node.getNode(ChannelConfiguration.NAME.getKey()).getString();
        Preconditions.checkState(name != null, "No name specified for channel");
        return name;
    }

    @Nullable
    @Override
    public Radius getRadius() {
        return this.getConfiguration(ChannelConfiguration.RADIUS, node -> node.getValue(input -> {
            if (!(input instanceof Map)) return null;
            @SuppressWarnings("unchecked")
            final Map<String, Object> internal = (Map<String, Object>) input;
            // TODO: Better way to handle this
            if (!Boolean.parseBoolean(internal.get(ChannelConfiguration.RADIUS_ENABLED.getKey()).toString())) {
                return null;
            }
            return new Radius(
                Double.parseDouble(internal.get(ChannelConfiguration.RADIUS_HORIZONTAL.getKey()).toString()),
                Double.parseDouble(internal.get(ChannelConfiguration.RADIUS_VERTICAL.getKey()).toString())
            );
        }));
    }

    @Nullable
    @Override
    public String getRecipientSection(@NotNull final String sectionName) {
        Preconditions.checkNotNull(sectionName, "sectionName was null");
        final String section = this.getConfiguration(ChannelConfiguration.FORMAT_RECIPIENT, node -> node.getNode(sectionName).getString());
        if (section == null) return null;
        return section;
    }

    @NotNull
    @Override
    public String getTag() {
        // May not use master
        final String tag = this.node.getNode(ChannelConfiguration.TAG.getKey()).getString();
        Preconditions.checkState(tag != null, "No tag specified for channel");
        return tag;
    }

    @NotNull
    @Override
    public WorldRecipients getWorldRecipients() {
        final Boolean toAll = this.getConfiguration(ChannelConfiguration.WORLDS_ALL, ConfigurationNode::getBoolean);
        final Boolean toSelf = this.getConfiguration(ChannelConfiguration.WORLDS_SELF, ConfigurationNode::getBoolean);
        final Map<String, Boolean> individual = this.getConfiguration(
            ChannelConfiguration.WORLDS_INDIVIDUAL,
            node -> node.getChildrenMap().entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().toString(),
                    entry -> entry.getValue().getBoolean()
                ))
        );
        Preconditions.checkState(toAll != null, "No all worlds option specified for " + this.getName());
        Preconditions.checkState(toSelf != null, "No self world option specified for " + this.getName());
        Preconditions.checkArgument(individual != null, "No individual worlds option specified for " + this.getName());
        return new WorldRecipients(individual, toSelf, toAll);
    }

    @Override
    public boolean isPermanent() {
        final Boolean permanent = this.getConfiguration(ChannelConfiguration.PERMANENT, ConfigurationNode::getBoolean);
        Preconditions.checkState(permanent != null, "No permanent setting was specified for " + this.getName());
        return permanent;
    }

    @Override
    public void removeMember(@NotNull final CPlayer cp) {
        Preconditions.checkNotNull(cp, "cp was null");
        if (!this.members.contains(cp)) return;
        this.members.remove(cp);
        cp.leaveChannel(this);
    }

    /**
     * Returns the {@link ConfigurationNode} that this ConfigChannel is based upon. This represents one element of the
     * channel list in the {@code config.yml}.
     * <p>This may be useful for developers to access custom config options.
     *
     * @return Node
     */
    @NotNull
    public ConfigurationNode getNode() {
        return this.node;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("name", this.getName())
            .add("tag", this.getTag())
            .add("members", this.members)
            .toString();
    }
}
