package net.totobirdcreations.looseendslib.manager;

import io.netty.util.AsciiString;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.totobirdcreations.looseendslib.LooseEndsLib;
import net.totobirdcreations.looseendslib.util.LooseEndLang;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


@Environment(EnvType.CLIENT)
public class ClientLooseEndManager {

    private final LooseEndManager owner;

    @Nullable
    private ClientPlayNetworkHandler handler;
    @Nullable
    private MinecraftClient          client;


    private ArrayList<LooseEnd> remoteEnds;
    private int                 ticksPassed;


    /* package-private */ ClientLooseEndManager(LooseEndManager owner) {
        this.owner = owner;
        this.reset();
    }


    /* package-private */ void start(ClientPlayNetworkHandler handler, PacketSender ignoredSender, MinecraftClient client) {
        this.reset();
        this.handler = handler;
        this.client  = client;
        if (! this.shouldRun()) {return;}

        LooseEndManager manager = LooseEndManager.getInstance();

        for (LooseEnd end : manager.ends) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBytes(end.toSendableString().getBytes(StandardCharsets.US_ASCII));
            Packet<?> packet = ClientPlayNetworking.createC2SPacket(owner.channel, buf);
            handler.sendPacket(packet);
        }
    }


    public void tick() {
        if (! this.shouldRun()) {return;}

        if (this.ticksPassed < LooseEndsLib.TIMEOUT) {
            this.ticksPassed += 1;
            if (this.ticksPassed == LooseEndsLib.TIMEOUT) {
                this.verify();
            }
        }
    }


    private void verify() {
        if (! this.shouldRun()) {return;}

        LooseEndManager manager = LooseEndManager.getInstance();
        LooseEndManager.Errors errors = manager.getErrors(manager.getEnds(), this.remoteEnds);

        if (errors.hasAny()) {
            Text error = errors.generateError(LooseEndLang.getClientSuffix());
            assert this.client != null;
            this.client.execute(() -> {
                MinecraftClient client = this.client;
                if (client.world != null) {
                    client.world.disconnect();
                }
                client.disconnect(new DisconnectedScreen(
                        new MultiplayerScreen(new TitleScreen()),
                        Text.translatable("multiplayer.disconnect.generic"),
                        error
                ));
            });
        }
    }


    /* package-private */ void stop(ClientPlayNetworkHandler ignoredHandler, MinecraftClient ignoredClient) {
        this.reset();
    }


    /* package-private */ void signal(MinecraftClient ignoredClient, ClientPlayNetworkHandler ignoredHandler, PacketByteBuf buf, PacketSender ignoredSender) {
        if (! this.shouldRun()) {return;}

        StringBuilder data = new StringBuilder();
        while (buf.isReadable()) {
            data.append(AsciiString.b2c(buf.readByte()));
        }
        remoteEnds.add(LooseEnd.fromSendableString(data.toString()));

    }


    private boolean shouldRun() {
        assert this.client != null;
        MinecraftServer server = this.client.getServer();
        return server == null || ! server.isSingleplayer();
    }


    private void reset() {
        this.handler = null;
        this.client  = null;

        this.remoteEnds  = new ArrayList<>();
        this.ticksPassed = 0;
    }

}
