package net.totobirdcreations.looseendslib;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LooseEndsLib implements ModInitializer {
	public static final String ID     = "looseendslib";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static EnvType ENVIRONMENT;
	public static boolean IS_DEVELOPMENT;

	public static int TIMEOUT = 20; // 1.0s


	@Override
	public void onInitialize() {
		ENVIRONMENT    = FabricLoader.getInstance().getEnvironmentType();
		IS_DEVELOPMENT = FabricLoader.getInstance().isDevelopmentEnvironment();

		LooseEndsExample.enable();
	}

}
