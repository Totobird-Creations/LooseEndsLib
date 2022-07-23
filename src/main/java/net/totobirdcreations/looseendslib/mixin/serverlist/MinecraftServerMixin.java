package net.totobirdcreations.looseendslib.mixin.serverlist;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.totobirdcreations.looseendslib.LooseEndManager;
import net.totobirdcreations.looseendslib.util.LooseEndLang;
import net.totobirdcreations.looseendslib.util.mixin.serverlist.ServerMetadataMixinInterface;
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
    public void runServerRedirectSetDescription(ServerMetadata metadata, Text description) {
        ServerMetadataMixinInterface imetadata = (ServerMetadataMixinInterface) metadata;
        imetadata.setRealDescription(description);
        imetadata.setEnds(LooseEndManager.getInstance().getEnds());
        metadata.setDescription(LooseEndLang.getServerListFailText());
    }

}
