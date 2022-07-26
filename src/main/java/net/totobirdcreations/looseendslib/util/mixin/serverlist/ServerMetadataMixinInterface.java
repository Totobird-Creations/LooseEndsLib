package net.totobirdcreations.looseendslib.util.mixin.serverlist;

import net.minecraft.text.Text;
import net.totobirdcreations.looseendslib.manager.LooseEnd;

import java.util.ArrayList;


public interface ServerMetadataMixinInterface {

    Text getRealDescription();

    void setRealDescription(Text value);

    ArrayList<LooseEnd> getEnds();

    void setEnds(ArrayList<LooseEnd> value);

}
