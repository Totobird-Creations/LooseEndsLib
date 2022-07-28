package net.totobirdcreations.looseendslib.mixin.handler;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(ServerWorld.class)
public interface ServerWorldInterface {

    @Accessor("server")
    MinecraftServer getServer();

}
