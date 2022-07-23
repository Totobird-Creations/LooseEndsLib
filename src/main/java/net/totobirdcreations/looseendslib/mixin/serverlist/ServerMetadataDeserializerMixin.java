package net.totobirdcreations.looseendslib.mixin.serverlist;

import com.google.gson.*;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import net.totobirdcreations.looseendslib.LooseEnd;
import net.totobirdcreations.looseendslib.LooseEndManager;
import net.totobirdcreations.looseendslib.util.LooseEndLang;
import net.totobirdcreations.looseendslib.util.mixin.serverlist.ServerMetadataMixinInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;
import java.util.ArrayList;


@Mixin(ServerMetadata.Deserializer.class)
abstract class ServerMetadataDeserializerMixin implements JsonDeserializer<ServerMetadata>, JsonSerializer<ServerMetadata> {

    @Inject(
            method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/server/ServerMetadata;",
            at = @At("RETURN")
    )
    public void deserialize(JsonElement element, Type type, JsonDeserializationContext context, CallbackInfoReturnable<ServerMetadata> callback) {
        ServerMetadata               metadata  = callback.getReturnValue();
        JsonObject                   json      = JsonHelper.asObject(element, "status");
        ServerMetadataMixinInterface imetadata = (ServerMetadataMixinInterface) metadata;
        if (json.has("looseEndsLibRealDescription")) {
            ArrayList<LooseEnd> ends = new ArrayList<>();
            int i = 0;
            while (true) {
                if (
                        json.has("looseEndsLibEndList." + i + ".modId"      ) &&
                        json.has("looseEndsLibEndList." + i + ".modName"    ) &&
                        json.has("looseEndsLibEndList." + i + ".modVersion" )
                ) {
                    LooseEnd end = new LooseEnd(
                            JsonHelper.getString(json, "looseEndsLibEndList." + i + ".modId"      ),
                            JsonHelper.getString(json, "looseEndsLibEndList." + i + ".modName"    ),
                            JsonHelper.getString(json, "looseEndsLibEndList." + i + ".modVersion" )
                    )
                            .whenJoinServer(LooseEnd.Condition.fromString(
                                    JsonHelper.getString(json, "looseEndsLibEndList." + i + ".whenJoinServer")
                            ))
                            .whenClientJoins(LooseEnd.Condition.fromString(
                                    JsonHelper.getString(json, "looseEndsLibEndList." + i + ".whenClientJoins")
                            ));
                    ends.add(end);
                } else {
                    break;
                }
                i += 1;
            }
            imetadata.setEnds(ends);
            if (LooseEndManager.getInstance().confirmActive(ends)) {
                metadata.setDescription(context.deserialize(json.get("looseEndsLibRealDescription"), Text.class));
            }
        }
    }


    @Inject(
            method = "serialize(Lnet/minecraft/server/ServerMetadata;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
            at = @At("RETURN")
    )
    public void serialize(ServerMetadata metadata, Type type, JsonSerializationContext context, CallbackInfoReturnable<JsonElement> callback) {
        JsonObject                   json      = (JsonObject)(callback.getReturnValue());
        ServerMetadataMixinInterface imetadata = (ServerMetadataMixinInterface) metadata;

        if (imetadata.getRealDescription() != null) {
            json.add("looseEndsLibRealDescription", context.serialize(imetadata.getRealDescription()));
        }
        ArrayList<LooseEnd> ends = imetadata.getEnds();
        for (int i = 0; i < ends.size(); i++) {
            LooseEnd end = ends.get(i);
            String prefix = "looseEndsLibEndList." + i + ".";
            json.addProperty(prefix + "modId"           , end.modId);
            json.addProperty(prefix + "modName"         , end.modName);
            json.addProperty(prefix + "modVersion"      , end.modVersion);
            json.addProperty(prefix + "whenJoinServer"  , end.getWhenJoinServer().toString()  );
            json.addProperty(prefix + "whenClientJoins" , end.getWhenClientJoins().toString() );
        }

    }


    @Redirect(
            method = "serialize(Lnet/minecraft/server/ServerMetadata;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/ServerMetadata;getDescription()Lnet/minecraft/text/Text;"
            )
    )
    public Text serializeGetDescription(ServerMetadata metadata) {
        return LooseEndLang.getServerListFailText();
    }

}
