package net.totobirdcreations.looseendslib.mixin.serverlist;

import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.totobirdcreations.looseendslib.LooseEnd;
import net.totobirdcreations.looseendslib.util.mixin.serverlist.ServerMetadataMixinInterface;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;


@Mixin(ServerMetadata.class)
class ServerMetadataMixin implements ServerMetadataMixinInterface {

    @Nullable
    private Text                realDescription;
    private ArrayList<LooseEnd> ends            = new ArrayList<>();

    @Nullable
    @Override
    public Text getRealDescription() {
        return this.realDescription;
    }

    @Override
    public void setRealDescription(Text value) {
        this.realDescription = value;
    }

    @Override
    public ArrayList<LooseEnd> getEnds() {
        return this.ends;
    }

    @Override
    public void setEnds(ArrayList<LooseEnd> value) {
        this.ends = value;
    }

}
