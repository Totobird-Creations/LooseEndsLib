package net.totobirdcreations.looseendslib.mixin.serverlist;

import com.google.gson.*;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;
import net.totobirdcreations.looseendslib.LooseEndsLib;
import net.totobirdcreations.looseendslib.manager.LooseEnd;
import net.totobirdcreations.looseendslib.manager.LooseEndManager;
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
    void deserialize(JsonElement element, Type type, JsonDeserializationContext context, CallbackInfoReturnable<ServerMetadata> callback) {
        ServerMetadata               metadata  = callback.getReturnValue();
        JsonObject                   json      = JsonHelper.asObject(element, "status");
        ServerMetadataMixinInterface iMetadata = (ServerMetadataMixinInterface) metadata;
        ArrayList<LooseEnd> ends = new ArrayList<>();
        if (json.has("looseEndsLibRealDescription")) {
            int i = 0;
            while (true) {
                if (
                        json.has("looseEndsLibEndList." + i)
                ) {
                    LooseEnd end = LooseEnd.fromSendableString(
                            JsonHelper.getString(json, "looseEndsLibEndList." + i)
                    );
                    if (end != null) {
                        ends.add(end);
                    }
                } else {
                    break;
                }
                i += 1;
            }
        }
        iMetadata.setEnds(ends);
        if (! LooseEndManager.getInstance().getErrors(LooseEndManager.getInstance().getEnds(), ends).hasAny()) {
            metadata.setDescription(context.deserialize(json.get("looseEndsLibRealDescription"), Text.class));
        } else {
            metadata.setDescription(LooseEndLang.getServerList());
        }
    }


    @Inject(
            method = "serialize(Lnet/minecraft/server/ServerMetadata;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
            at = @At("RETURN")
    )
    void serialize(ServerMetadata metadata, Type type, JsonSerializationContext context, CallbackInfoReturnable<JsonElement> callback) {
        JsonObject                   json      = (JsonObject)(callback.getReturnValue());
        ServerMetadataMixinInterface iMetadata = (ServerMetadataMixinInterface) metadata;

        if (iMetadata.getRealDescription() != null) {
            json.add("looseEndsLibRealDescription", context.serialize(iMetadata.getRealDescription()));
        }
        ArrayList<LooseEnd> ends = iMetadata.getEnds();
        for (int i = 0; i < ends.size(); i++) {
            LooseEnd end = ends.get(i);
            json.addProperty("looseEndsLibEndList." + i, end.toSendableString());
        }

    }

}
