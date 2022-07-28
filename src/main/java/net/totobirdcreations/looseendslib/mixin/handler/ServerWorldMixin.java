package net.totobirdcreations.looseendslib.mixin.handler;

import com.google.common.collect.ImmutableList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.totobirdcreations.looseendslib.manager.LooseEndManager;
import net.totobirdcreations.looseendslib.manager.ServerLooseEndManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;


@Mixin(ServerWorld.class)
abstract class ServerWorldMixin extends World {

    private ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }


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
