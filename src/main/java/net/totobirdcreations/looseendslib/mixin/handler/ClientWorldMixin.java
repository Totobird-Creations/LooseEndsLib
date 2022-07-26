package net.totobirdcreations.looseendslib.mixin.handler;

import net.minecraft.client.world.ClientWorld;
import net.totobirdcreations.looseendslib.manager.LooseEndManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;


@Mixin(ClientWorld.class)
class ClientWorldMixin {

    @Inject(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At("TAIL")
    )
    void tick(BooleanSupplier supplier, CallbackInfo callback) {
        LooseEndManager.getInstance().getClientManager().tick();
    }

}
