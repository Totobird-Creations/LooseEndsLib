package net.totobirdcreations.looseendslib;

import net.fabricmc.api.EnvType;


public class LooseEndsExample {

    public static final String ID      = "looseendslib_example";
    public static final String NAME    = "Loose Ends Lib Example";
    public static final String VERSION = "1.0.0";

    public static void enable() {
        LooseEndsLib.LOGGER.info("Example mod enabled (" + VERSION + ").");

        LooseEndManager manager = LooseEndManager.getInstance();
        manager.register(ID, NAME, VERSION)
                .whenJoinServer(LooseEnd.Condition.DISALLOWED) // Clients will not join servers with the mod.
                .whenClientJoins(LooseEnd.Condition.REQUIRED); // Servers will not allow clients without the mod.
    }

}
