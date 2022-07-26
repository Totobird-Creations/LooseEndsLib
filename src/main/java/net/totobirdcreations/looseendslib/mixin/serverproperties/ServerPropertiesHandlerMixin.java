package net.totobirdcreations.looseendslib.mixin.serverproperties;

import net.minecraft.server.dedicated.AbstractPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.totobirdcreations.looseendslib.util.mixin.serverproperties.ServerPropertiesHandlerMixinInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Properties;


@Mixin(ServerPropertiesHandler.class)
abstract class ServerPropertiesHandlerMixin
        extends AbstractPropertiesHandler<ServerPropertiesHandler>
        implements ServerPropertiesHandlerMixinInterface
{
    private ServerPropertiesHandlerMixin(Properties properties) {
        super(properties);
    }


    boolean warnModsInServerList = true;


    public boolean getWarnModsInServerList() {
        return this.warnModsInServerList;
    }


    @Inject(
            method = "<init>(Ljava/util/Properties;)V",
            at = @At("TAIL")
    )
    void init(Properties properties, CallbackInfo callback) {
        this.warnModsInServerList = this.parseBoolean("warn-mods-in-server-list", true);
    }

}
