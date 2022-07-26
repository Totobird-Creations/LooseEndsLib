package net.totobirdcreations.looseendslib.manager;

import io.netty.util.AsciiString;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.totobirdcreations.looseendslib.LooseEndsLib;
import net.totobirdcreations.looseendslib.util.LooseEndLang;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


/* package-private */ public class ServerLooseEndManager {

    private final LooseEndManager owner;

    @Nullable
    private ServerPlayNetworkHandler handler;
    @Nullable
    private MinecraftServer          server;

    private ArrayList<LooseEnd> remoteEnds;
    private int                 ticksPassed;


    /* package-private */ ServerLooseEndManager(LooseEndManager owner) {
        this.owner = owner;
        this.reset();
    }


    /* package-private */ void start(ServerPlayNetworkHandler handler, PacketSender ignoredSender, MinecraftServer server) {
        this.reset();
        this.handler = handler;
        this.server  = server;
        if (! this.shouldRun()) {return;}

        LooseEndManager manager = LooseEndManager.getInstance();

        for (LooseEnd end : manager.ends) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBytes(end.toSendableString().getBytes(StandardCharsets.US_ASCII));
            Packet<?> packet = ServerPlayNetworking.createS2CPacket(owner.channel, buf);
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
        LooseEndManager.Errors errors = manager.getErrors(this.remoteEnds, manager.getEnds());

        if (errors.hasAny()) {
            Text error = errors.generateError(LooseEndLang.getServerSuffix());
            assert this.server != null;
            assert this.handler != null;
            this.server.execute(() -> {
                this.handler.connection.send(new DisconnectS2CPacket(error));
                this.handler.disconnect(Text.literal("Unresolved mod list conflicts."));
            });
        }
    }


    /* package-private */ void stop(ServerPlayNetworkHandler ignoredHandler, MinecraftServer ignoredServer) {}


    /* package-private */ void signal(MinecraftServer ignoredServer, ServerPlayerEntity ignoredPlayer, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender ignoredSender) {
        if (! this.shouldRun()) {return;}

        StringBuilder data = new StringBuilder();
        while (buf.isReadable()) {
            data.append(AsciiString.b2c(buf.readByte()));
        }
        remoteEnds.add(LooseEnd.fromSendableString(data.toString()));
        LooseEndsLib.LOGGER.info(remoteEnds.get(remoteEnds.size() - 1).toString());
    }


    private boolean shouldRun() {
        assert this.server != null;
        return (
                this.server.getHostProfile() == null ||
                ! this.server.getHostProfile().equals(this.handler.getPlayer().getGameProfile())
        );
    }


    private void reset() {
        this.handler = null;
        this.server  = null;

        this.remoteEnds  = new ArrayList<>();
        this.ticksPassed = 0;
    }

}
