package net.totobirdcreations.looseendslib;

import net.fabricmc.api.EnvType;
import net.totobirdcreations.looseendslib.manager.LooseEnd;
import net.totobirdcreations.looseendslib.manager.LooseEndManager;


public class LooseEndsExample {

    public static final String ID      = "looseendslib_example";
    public static final String NAME    = "Loose Ends Lib Example";
    public static final String VERSION = "2.3.7";

    public static void enable() {
        LooseEndsLib.LOGGER.warn(NAME + " (" + VERSION + ") enabled. Make sure to disable this in release.");

        LooseEndManager manager = LooseEndManager.getInstance();
        manager.register(ID, NAME, VERSION)
                .whenJoinServer(LooseEnd.Condition.REQUIRED) // Clients will not join servers with the mod.
                .whenClientJoins(LooseEnd.Condition.REQUIRED); // Servers will not allow clients without the mod.
    }

}
