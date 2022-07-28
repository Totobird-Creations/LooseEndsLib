package net.totobirdcreations.looseendslib.mixin.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(ClientWorld.class)
public interface ClientWorldInterface {

    @Accessor("client")
    MinecraftClient getClient();

}
