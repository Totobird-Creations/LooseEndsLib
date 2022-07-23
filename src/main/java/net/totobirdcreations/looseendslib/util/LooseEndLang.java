package net.totobirdcreations.looseendslib.util;


import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LooseEndLang {

    public static String getJoinServerFailText() {
        return "Failed to connect to the server due to mod list conflicts. Please resolve them before joining the server.";
    }
    public static String getJoinServerFailTextMissing() {
        return "You can not join servers without:";
    }
    public static String getJoinServerFailTextHas() {
        return "You can not join servers that have:";
    }
    public static String getClientJoinedFailText() {
        return "Failed to connect to the the server due to mod list conflicts. Please resolve them before joining the server.";
    }
    public static String getClientJoinedFailTextMissing() {
        return "You can not join this server without:";
    }
    public static String getClientJoinedFailTextHas() {
        return "You can not join this server with:";
    }
    public static Text getServerListFailText() {
        return Text.literal("Incompatible mod list").formatted(Formatting.DARK_RED);
    }

}
