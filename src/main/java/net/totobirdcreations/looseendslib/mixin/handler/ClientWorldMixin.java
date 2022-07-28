package net.totobirdcreations.looseendslib.mixin.handler;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.totobirdcreations.looseendslib.manager.LooseEndManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;


@Mixin(ClientWorld.class)
abstract class ClientWorldMixin extends World {

    public ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }


    @Inject(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At("TAIL")
    )
    void tick(BooleanSupplier supplier, CallbackInfo callback) {
        LooseEndManager.getInstance().getClientManager().tick(((ClientWorldInterface)this).getClient());
    }

}
