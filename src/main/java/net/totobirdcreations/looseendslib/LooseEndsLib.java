package net.totobirdcreations.looseendslib;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.totobirdcreations.looseendslib.handler.ClientChannelHandler;
import net.totobirdcreations.looseendslib.handler.ServerChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LooseEndsLib implements ModInitializer {
	public static final String     ID      = "looseendslib";
	public static final Logger     LOGGER  = LoggerFactory.getLogger(ID);

	public static EnvType ENVIRONMENT;


	@Override
	public void onInitialize() {
		ENVIRONMENT = FabricLoader.getInstance().getEnvironmentType();
		if (LooseEndsLib.ENVIRONMENT == EnvType.CLIENT) {
			LooseEndManager.getInstance().putEvents();
		}

		LooseEndsExample.enable();
	}

}
