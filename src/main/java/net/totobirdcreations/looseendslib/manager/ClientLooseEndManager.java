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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


@Environment(EnvType.CLIENT)
public class ClientLooseEndManager {

    private final LooseEndManager owner;


    private ArrayList<LooseEnd> remoteEnds;
    private int                 ticksPassed;


    /* package-private */ ClientLooseEndManager(LooseEndManager owner) {
        this.owner = owner;
        this.reset();
    }


    /* package-private */ void start(ClientPlayNetworkHandler handler, PacketSender ignoredSender, MinecraftClient client) {
        this.reset();
        if (! this.shouldRun(client)) {return;}

        LooseEndManager manager = LooseEndManager.getInstance();

        for (LooseEnd end : manager.ends) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBytes(end.toSendableString().getBytes(StandardCharsets.US_ASCII));
            Packet<?> packet = ClientPlayNetworking.createC2SPacket(owner.channel, buf);
            handler.sendPacket(packet);
        }
    }


    public void tick(MinecraftClient client) {
        if (! this.shouldRun(client)) {return;}

        if (this.ticksPassed < LooseEndsLib.TIMEOUT) {
            this.ticksPassed += 1;
            if (this.ticksPassed == LooseEndsLib.TIMEOUT) {
                this.verify(client);
            }
        }
    }


    private void verify(MinecraftClient client) {
        if (! this.shouldRun(client)) {return;}

        LooseEndManager manager = LooseEndManager.getInstance();
        LooseEndManager.Errors errors = manager.getErrors(manager.getEnds(), this.remoteEnds);

        if (errors.hasAny()) {
            Text error = errors.generateError(LooseEndLang.getClientSuffix());
            client.execute(() -> {
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


    /* package-private */ void signal(MinecraftClient client, ClientPlayNetworkHandler ignoredHandler, PacketByteBuf buf, PacketSender ignoredSender) {
        if (! this.shouldRun(client)) {return;}

        StringBuilder data = new StringBuilder();
        while (buf.isReadable()) {
            data.append(AsciiString.b2c(buf.readByte()));
        }
        remoteEnds.add(LooseEnd.fromSendableString(data.toString()));

    }


    private boolean shouldRun(MinecraftClient client) {
        MinecraftServer server = client.getServer();
        return server == null || ! server.isSingleplayer();
    }


    private void reset() {
        this.remoteEnds  = new ArrayList<>();
        this.ticksPassed = 0;
    }

}
