package net.totobirdcreations.looseendslib;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.totobirdcreations.looseendslib.handler.ClientChannelHandler;
import net.totobirdcreations.looseendslib.handler.ServerChannelHandler;

import javax.annotation.Nullable;

public class LooseEnd {

    public                final String               modId;
    public                final String               modName;
    public                final String               modVersion;
    /* package-private */ final ServerChannelHandler serverHandler;
    @Nullable
    /* package-private */       ClientChannelHandler clientHandler;
    /* package-private */ final Identifier           channel;

    @Nullable
    /* package-private */ Condition whenJoinServer;
    @Nullable
    /* package-private */ Condition whenClientJoins;


    /* package-private */
    public LooseEnd(String mod_id, String mod_name, String mod_version) {
        this.modId = mod_id;
        this.modName = mod_name;
        this.modVersion = mod_version;

        this.serverHandler = new ServerChannelHandler();
        this.channel       = new Identifier(LooseEndsLib.ID, "sync_" + this.modId + "_" + this.modVersion);
        ServerPlayNetworking.registerGlobalReceiver(this.channel, this.serverHandler);
        if (LooseEndsLib.ENVIRONMENT == EnvType.CLIENT) {
            this.registerClient();
        }
        LooseEndManager.getInstance().putChannel(this.channel);
    }
    @Environment(EnvType.CLIENT)
    private void registerClient() {
        this.clientHandler = new ClientChannelHandler();
        ClientPlayNetworking.registerGlobalReceiver(this.channel, this.clientHandler);
    }


    public String toString() {
        return this.modName + " " + this.modVersion + " (" + this.modId + ") / JS_" + this.whenJoinServer.toString() + " CJ_" + this.whenClientJoins.toString();
    }


    @Nullable
    public Condition getWhenJoinServer() {return this.whenJoinServer;}
    @Nullable
    public Condition getWhenClientJoins() {return this.whenClientJoins;}


    /**
    What should happen when the client connects to a server.
    (Client Side)

    otherEndCondition:
    - REQUIRED   : Disconnect from the server unless it has the mod.
    - NONE       : Nothing.
    - DISALLOWED : Disconnect from the server if it has the mod.
     */
    public LooseEnd whenJoinServer(Condition otherEndCondition) {
        this.whenJoinServer = otherEndCondition;
        return this;
    }

    /**
    What should happen when the client connects to a server.
    (Server Side)

    otherEndCondition:
    - REQUIRED   : Kick the client unless they have the mod.
    - NONE       : Nothing.
    - DISALLOWED : Kick the client if they have the mod.
     */
    public LooseEnd whenClientJoins(Condition otherEndCondition) {
        this.whenClientJoins = otherEndCondition;
        return this;
    }


    public enum Condition {
        REQUIRED,
        NONE,
        DISALLOWED;

        public String toString() {
            return switch (this) {
                case REQUIRED   -> "REQUIRED";
                case NONE       -> "NONE";
                case DISALLOWED -> "DISALLOWED";
            };
        }

        @Nullable
        public static Condition fromString(String from) {
            return switch (from) {
                case "REQUIRED"   -> REQUIRED;
                default           -> NONE;
                case "DISALLOWED" -> DISALLOWED;
            };
        }
    }

}
