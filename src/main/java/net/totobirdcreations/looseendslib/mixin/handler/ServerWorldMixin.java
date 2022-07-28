package net.totobirdcreations.looseendslib.mixin.handler;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.world.ServerWorld;
import net.totobirdcreations.looseendslib.manager.LooseEndManager;
import net.totobirdcreations.looseendslib.manager.ServerLooseEndManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;


@Mixin(ServerWorld.class)
class ServerWorldMixin {

    @Inject(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At("TAIL")
    )
    void tick(BooleanSupplier supplier, CallbackInfo callback) {
        ImmutableList<ServerLooseEndManager> serverManagers = LooseEndManager.getInstance().getServerManagers();
        for (ServerLooseEndManager serverManager : serverManagers) {
            serverManager.tick(((ServerWorldInterface)this).getServer());
        }
    }

}
