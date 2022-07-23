package net.totobirdcreations.looseendslib;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.totobirdcreations.looseendslib.util.LooseEndLang;

import java.util.ArrayList;

public class LooseEndManager {
    private static final LooseEndManager INSTANCE = new LooseEndManager();
    public  static       LooseEndManager getInstance() {return INSTANCE;}

    private ArrayList<LooseEnd>   ends     = new ArrayList<>();
    private ArrayList<Identifier> channels = new ArrayList<>();

    private LooseEndManager() {}


    public LooseEnd register(String mod_id, String mod_name, String mod_version) {
        LooseEnd end = new LooseEnd(mod_id, mod_name, mod_version);
        ends.add(end);
        return end;
    }


    public ArrayList<LooseEnd> getEnds() {
        return (ArrayList<LooseEnd>) ends.clone();
    }

    public boolean confirmActive(ArrayList<LooseEnd> ends) {
        return true;
    }


    @Environment(EnvType.CLIENT)
    /* package-private */ void putEvents() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, minecraft) -> this.onJoinServer(minecraft));
    }


    /* package-private */ void putChannel(Identifier channel) {
        this.channels.add(channel);
    }


    @Environment(EnvType.CLIENT)
    public void onJoinServer(MinecraftClient client) {
        boolean singleplayer = client.isInSingleplayer();
        if (! singleplayer) {
            Errors errors = new Errors();
            for (LooseEnd end : ends) {
                boolean canSend = ClientPlayNetworking.canSend(end.channel)
                        && channels.contains(end.channel);
                switch (end.whenJoinServer) {
                    case REQUIRED   -> {if (! canSend) { errors.addMissing(end); }}
                    case DISALLOWED -> {if (canSend)   { errors.addHas(end);     }}
                }
            }
            if (errors.hasAny()) {
                client.execute(() -> {this.onJoinServerDisconnect(client, errors);});
            }
        }
    }


    @Environment(EnvType.CLIENT)
    public void onJoinServerDisconnect(MinecraftClient client, Errors errors) {
        if (client.world != null) {
            client.world.disconnect();
        }
        client.disconnect(new DisconnectedScreen(
                new MultiplayerScreen(new TitleScreen()),
                Text.translatable("multiplayer.disconnect.generic"),
                errors.generateError(
                        LooseEndLang.getJoinServerFailText(),
                        LooseEndLang.getJoinServerFailTextMissing(),
                        LooseEndLang.getJoinServerFailTextHas()
                )
        ));
    }


    public boolean onClientJoined(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player) {
        boolean singleplayer = server.isSingleplayer();
        if (! (singleplayer && server.getCurrentPlayerCount() <= 0)) {
            ServerPlayNetworkHandler handler = new ServerPlayNetworkHandler(server, connection, player);
            Errors errors = new Errors();
            for (LooseEnd end : ends) {
                boolean canSend = ServerPlayNetworking.canSend(handler, end.channel)
                        && channels.contains(end.channel);
                switch (end.whenClientJoins) {
                    case REQUIRED   -> {if (! canSend) { errors.addMissing(end); }}
                    case NONE       -> {}
                    case DISALLOWED -> {if (canSend)   { errors.addHas(end);     }}
                }
            }
            if (errors.hasAny()) {
                connection.send(new DisconnectS2CPacket(errors.generateError(
                        LooseEndLang.getClientJoinedFailText(),
                        LooseEndLang.getClientJoinedFailTextMissing(),
                        LooseEndLang.getClientJoinedFailTextHas()
                )));
                connection.disconnect(Text.literal("Incompatible mod list."));
                return false;
            }
        }
        return true;
    }


    private static class Errors {
        private final ArrayList<LooseEnd> missing = new ArrayList<>();
        private final ArrayList<LooseEnd> has     = new ArrayList<>();
        private Errors() {}

        private void addMissing(LooseEnd end) {
            this.missing.add(end);
        }
        private void addHas(LooseEnd end) {
            this.has.add(end);
        }

        private boolean hasAny() {
            return (this.missing.size() > 0) || (this.has.size() > 0);
        }
        private Text generateError(String prefix, String missing, String has) {
            MutableText text = Text.literal("");
            text.append(Text.literal(prefix).formatted(Formatting.YELLOW));
            if (this.missing.size() > 0) {
                text = text.append("\n\n\n");
                text = text.append(Text.literal(missing).formatted(Formatting.UNDERLINE));
                text = text.append("\n");
                for (LooseEnd end : this.missing) {
                    text = text.append("\n");
                    text = text.append(this.generateError(end));
                }
            }
            if (this.has.size() > 0) {
                text = text.append("\n\n\n");
                text = text.append(Text.literal(has).formatted(Formatting.UNDERLINE));
                text = text.append("\n");
                for (LooseEnd end : this.has) {
                    text = text.append("\n");
                    text = text.append(this.generateError(end));
                }
            }
            text.append("\n");
            return text;
        }
        private Text generateError(LooseEnd end) {
            return Text.literal("")
                    .append(end.modName)
                    .append(" ")
                    .append(end.modVersion)
                    .append(" (")
                    .append(Text.literal(end.modId).formatted(Formatting.ITALIC))
                    .append(")");
        }
    }

}
