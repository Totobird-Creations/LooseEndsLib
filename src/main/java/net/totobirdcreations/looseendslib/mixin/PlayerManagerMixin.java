package net.totobirdcreations.looseendslib.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.totobirdcreations.looseendslib.LooseEndManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(PlayerManager.class)
class PlayerManagerMixin {

    @Inject(
            method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo callback) {
        if (! LooseEndManager.getInstance()
                .onClientJoined(player.getServer(), connection, player)
        ) {
            callback.cancel();
        }
    }

}
