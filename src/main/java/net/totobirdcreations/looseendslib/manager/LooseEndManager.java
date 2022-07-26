package net.totobirdcreations.looseendslib.manager;


import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.totobirdcreations.looseendslib.LooseEndsLib;
import net.totobirdcreations.looseendslib.util.LooseEndLang;

import java.util.ArrayList;
import java.util.HashMap;


public class LooseEndManager {
    private static final LooseEndManager INSTANCE = new LooseEndManager();
    public  static       LooseEndManager getInstance() {return INSTANCE;}

    @Environment(EnvType.CLIENT)
    private final ClientLooseEndManager                                    client;
    private final HashMap<ServerPlayNetworkHandler, ServerLooseEndManager> servers = new HashMap<>();

    /* package-private */ final Identifier channel = new Identifier(LooseEndsLib.ID, "register_ends");

    /* package-private */ final ArrayList<LooseEnd> ends = new ArrayList<>();

    private LooseEndManager() {
        ServerPlayConnectionEvents.JOIN       .register(this::serverManagerStart );
        ServerPlayConnectionEvents.DISCONNECT .register(this::serverManagerStop  );
        ServerPlayNetworking.registerGlobalReceiver(this.channel, this::serverManagerSignal);
        if (LooseEndsLib.ENVIRONMENT == EnvType.CLIENT) {
            this.client = new ClientLooseEndManager(this);
            this.initClient();
        } else {
            this.client = null;
        }
    }
    @Environment(EnvType.CLIENT)
    private void initClient() {
        ClientPlayConnectionEvents.JOIN       .register(this.client::start );
        ClientPlayConnectionEvents.DISCONNECT .register(this.client::stop  );
        ClientPlayNetworking.registerGlobalReceiver(this.channel, this.client::signal);
    }


    @Environment(EnvType.CLIENT)
    public ClientLooseEndManager getClientManager() {
        return this.client;
    }
    public ImmutableList<ServerLooseEndManager> getServerManagers() {
        return ImmutableList.copyOf(this.servers.values());
    }


    private void serverManagerStart(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ServerLooseEndManager serverManager = new ServerLooseEndManager(this);
        this.servers.put(handler, serverManager);
        serverManager.start(handler, sender, server);
    }
    private void serverManagerStop(ServerPlayNetworkHandler handler, MinecraftServer server) {
        this.servers.get(handler).stop(handler, server);
        this.servers.remove(handler);
    }
    private void serverManagerSignal(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        this.servers.get(handler).signal(server, player, handler, buf, sender);
    }


    public LooseEnd register(String mod_id, String mod_name, String mod_version) {
        LooseEnd end = new LooseEnd(mod_id, mod_name, mod_version);
        this.ends.add(end);
        return end;
    }


    public ArrayList<LooseEnd> getEnds() {
        return this.ends;
    }



    public Errors getErrors(ArrayList<LooseEnd> clientEnds, ArrayList<LooseEnd> serverEnds) {
        Errors errors = new Errors();

        for (LooseEnd clientEnd : clientEnds) {
            if (
                    clientEnd == null ||
                    clientEnd.getWhenJoinServer() == null ||
                    clientEnd.getWhenJoinServer() == LooseEnd.Condition.NONE
            ) {continue;}
            LooseEnd remoteEnd = null;
            for (LooseEnd serverEnd : serverEnds) {
                if (clientEnd.modId.equals(serverEnd.modId)) {
                    remoteEnd = serverEnd;
                    break;
                }
            }
            switch (clientEnd.getWhenJoinServer()) {
                case REQUIRED -> {if (remoteEnd == null) {
                    errors.addServerMissing(clientEnd);
                } else if (! remoteEnd.modVersion.equals(clientEnd.modVersion)) {
                    errors.addServerMissing(clientEnd.copySetInstalled(remoteEnd.modVersion));
                }}
                case DISALLOWED -> {if (remoteEnd != null) {
                    errors.addServerDisallowed(remoteEnd);
                }}
            }
        }

        for (LooseEnd serverEnd : serverEnds) {
            if (
                    serverEnd == null ||
                    serverEnd.getWhenClientJoins() == null ||
                    serverEnd.getWhenClientJoins() == LooseEnd.Condition.NONE
            ) {continue;}
            LooseEnd remoteEnd = null;
            for (LooseEnd clientEnd : clientEnds) {
                if (serverEnd.modId.equals(clientEnd.modId)) {
                    remoteEnd = clientEnd;
                    break;
                }
            }
            switch (serverEnd.getWhenClientJoins()) {
                case REQUIRED -> {if (remoteEnd == null) {
                    errors.addClientMissing(serverEnd);
                } else if (! remoteEnd.modVersion.equals(serverEnd.modVersion)) {
                    errors.addClientMissing(serverEnd.copySetInstalled(remoteEnd.modVersion));
                }}
                case DISALLOWED -> {if (remoteEnd != null) {
                    errors.addClientDisallowed(remoteEnd);
                }}
            }
        }

        return errors;
    }



    public static class Errors {
        private final ArrayList<LooseEnd> clientMissing    = new ArrayList<>();
        private final ArrayList<LooseEnd> clientDisallowed = new ArrayList<>();
        private final ArrayList<LooseEnd> serverMissing    = new ArrayList<>();
        private final ArrayList<LooseEnd> serverDisallowed = new ArrayList<>();

        /* package-private */ Errors() {}

        private void addClientMissing(LooseEnd end) {
            this.clientMissing.add(end);
        }
        private void addClientDisallowed(LooseEnd end) {
            this.clientDisallowed.add(end);
        }
        private void addServerMissing(LooseEnd end) {
            this.serverMissing.add(end);
        }
        private void addServerDisallowed(LooseEnd end) {
            this.serverDisallowed.add(end);
        }

        public boolean hasAny() {
            return (this.clientMissing.size() > 0) || (this.clientDisallowed.size() > 0) || (this.serverMissing.size() > 0) || (this.serverDisallowed.size() > 0);
        }

        /* package-private */ Text generateError(String suffix) {
            MutableText text = Text.literal("")
                    .append(Text.literal(LooseEndLang.getTitle()).formatted(Formatting.YELLOW));
            if (this.clientMissing.size() > 0) {text = text.append(this.generateErrorSection(
                    LooseEndLang.getClientMissing(),
                    this.clientMissing,
                    LooseEndLang.getClientInstalledPrefix(),
                    true
            ));}
            if (this.clientDisallowed.size() > 0) {text = text.append(this.generateErrorSection(
                    LooseEndLang.getClientDisallowed(),
                    this.clientDisallowed,
                    "",
                    false
            ));}
            if (this.serverMissing.size() > 0) {text = text.append(this.generateErrorSection(
                    LooseEndLang.getServerMissing(),
                    this.serverMissing,
                    LooseEndLang.getServerInstalledPrefix(),
                    true
            ));}
            if (this.serverDisallowed.size() > 0) {text = text.append(this.generateErrorSection(
                    LooseEndLang.getServerDisallowed(),
                    this.serverDisallowed,
                    "",
                    false
            ));}
            if (LooseEndsLib.IS_DEVELOPMENT) {
                text = text.append("\n\n\n")
                        .append(Text.literal(suffix).formatted(Formatting.DARK_GRAY).formatted(Formatting.ITALIC));
            }
            text.append("\n");
            return text;
        }

        private Text generateErrorSection(String title, ArrayList<LooseEnd> ends, String installedPrefix, boolean showVersion) {
            MutableText text = Text.literal("\n\n\n")
                    .append(Text.literal(title).formatted(Formatting.UNDERLINE))
                    .append("\n");
            for (LooseEnd end : ends) {
                text = text.append("\n")
                        .append(this.generateErrorLine(end, installedPrefix, showVersion));
            }
            return text;
        }

        private Text generateErrorLine(LooseEnd end, String installedPrefix, boolean showVersion) {
            MutableText text = Text.literal("")
                    .append(end.modName);
            if (showVersion) {
                text = text.append(" ")
                        .append(end.modVersion);
                if (end.getInstalled() != null) {
                    text = text.append(Text.literal(" (")
                            .append(
                                    Text.literal(installedPrefix)
                                            .append(Text.literal(end.getInstalled()))
                            ).append(")")
                            .formatted(Formatting.GRAY)
                            .formatted(Formatting.ITALIC)
                    );
                }
            }
            return text;
        }
    }

}
