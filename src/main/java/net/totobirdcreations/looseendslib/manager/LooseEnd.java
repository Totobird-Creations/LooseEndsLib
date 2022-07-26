package net.totobirdcreations.looseendslib.manager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class LooseEnd {

    public final String modId;
    public final String modName;
    public final String modVersion;

    /* package-private */ Condition whenJoinServer  = Condition.NONE;
    /* package-private */ Condition whenClientJoins = Condition.NONE;

    @Nullable
    /* package-private */ String installed = null;

    /* package-private */
    public LooseEnd(String mod_id, String mod_name, String mod_version) {
        this.modId = mod_id;
        this.modName = mod_name;
        this.modVersion = mod_version;
    }


    public String escape(String text) {
        StringBuilder builder = new StringBuilder();
        List<Character> escape = List.of('\\', '/');
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (escape.contains(ch)) {
                builder.append("\\");
                builder.append(ch);
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    public static ArrayList<String> unescape(String text) {
        ArrayList<String> parts  = new ArrayList<>();
        StringBuilder     part   = new StringBuilder();
        boolean           escape = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (escape) {
                part.append(ch);
                escape = false;
            } else if (ch == '\\') {
                escape = true;
            } else if (ch == '/') {
                parts.add(part.toString());
                part = new StringBuilder();
            } else {
                part.append(ch);
            }
        }
        return parts;
    }

    public String toString() {
        return this.modName + " " + this.modVersion + " (" + this.modId + ") / JS_" + this.whenJoinServer.toString() + " CJ_" + this.whenClientJoins.toString();
    }
    public String toSendableString() {
        return (this.escape(this.modName) + "/" + this.escape(this.modId) + "/" + this.escape(this.modVersion) + "/" + this.whenJoinServer.toString() + "/" + this.whenClientJoins.toString() + "/");
    }
    @Nullable
    public static LooseEnd fromSendableString(String string) {
        ArrayList<String> parts = LooseEnd.unescape(string);
        if (parts.size() == 5) {
            return new LooseEnd(parts.get(1), parts.get(0), parts.get(2))
                    .whenJoinServer(Condition.fromString(parts.get(3)))
                    .whenClientJoins(Condition.fromString(parts.get(4)));
        }
        return null;
    }


    /**
    * What should happen when the client connects to a server.
    * (Client Side)
    *
    * otherEndCondition:
    * - REQUIRED   : Disconnect from the server unless it has the mod.
    * - NONE       : Nothing.
    * - DISALLOWED : Disconnect from the server if it has the mod.
     */
    public LooseEnd whenJoinServer(Condition otherEndCondition) {
        this.whenJoinServer = otherEndCondition;
        return this;
    }
    /**
    * What should happen when the client connects to a server.
    * (Server Side)
    *
    * otherEndCondition:
    * - REQUIRED   : Kick the client unless they have the mod.
    * - NONE       : Nothing.
    * - DISALLOWED : Kick the client if they have the mod.
     */
    public LooseEnd whenClientJoins(Condition otherEndCondition) {
        this.whenClientJoins = otherEndCondition;
        return this;
    }

    /* package-private */ LooseEnd copySetInstalled(@Nullable String installed) {
        return this.duplicate().setInstalled(installed);
    }
    private LooseEnd setInstalled(@Nullable String installed) {
        this.installed = installed;
        return this;
    }


    public Condition getWhenJoinServer() {return this.whenJoinServer;}
    public Condition getWhenClientJoins() {return this.whenClientJoins;}

    @Nullable
    public String getInstalled() {return this.installed;}


    public LooseEnd duplicate() {
        return new LooseEnd(this.modId, this.modName, this.modVersion)
                .whenJoinServer(this.whenJoinServer)
                .whenClientJoins(this.whenClientJoins)
                .setInstalled(this.installed);
    }


    public enum Condition {
        REQUIRED,
        NONE,
        DISALLOWED;

        public String toString() {
            return switch (this) {
                case REQUIRED   -> "REQUIRED";
                case NONE       -> "NONE";
                case DISALLOWED -> "DISALLOWED";
            };
        }
        public static Condition fromString(String from) {
            return switch (from) {
                case "REQUIRED"   -> REQUIRED;
                default           -> NONE;
                case "DISALLOWED" -> DISALLOWED;
            };
        }
    }

}
