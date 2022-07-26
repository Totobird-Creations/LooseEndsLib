package net.totobirdcreations.looseendslib.mixin.serverlist;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.text.Text;
import net.totobirdcreations.looseendslib.manager.LooseEndManager;
import net.totobirdcreations.looseendslib.util.LooseEndLang;
import net.totobirdcreations.looseendslib.util.mixin.serverlist.ServerMetadataMixinInterface;
import net.totobirdcreations.looseendslib.util.mixin.serverproperties.ServerPropertiesHandlerMixinInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(MinecraftServer.class)
class MinecraftServerMixin {

    @Redirect(
            method = "runServer()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ServerMetadata;setDescription(Lnet/minecraft/text/Text;)V"
            )
    )
    void runServerRedirectSetDescription(ServerMetadata metadata, Text description) {
        ServerMetadataMixinInterface iMetadata = (ServerMetadataMixinInterface) metadata;

        boolean replace =
                ! (((MinecraftServer) (Object) this) instanceof MinecraftDedicatedServer dedicatedServer) ||
                ((ServerPropertiesHandlerMixinInterface) (dedicatedServer.getProperties())).getWarnModsInServerList();

        if (replace) {
            iMetadata.setRealDescription(description);
            iMetadata.setEnds(LooseEndManager.getInstance().getEnds());
            metadata.setDescription(LooseEndLang.getServerList());
        } else {
            iMetadata.setRealDescription(null);
            metadata.setDescription(description);
        }
    }

}
