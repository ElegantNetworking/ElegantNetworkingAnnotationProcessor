package test.fields;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeKey;
import test.SomeValue;

@ElegantPacket
public class PacketWith_FinalAndNonFinalFields implements ClientToServerPacket {
    final SomeKey a1;
    final SomeValue b1;
    SomeKey a2;
    SomeValue b2;

    public PacketWith_FinalAndNonFinalFields(SomeKey a1, SomeValue b1) {
        this.a1 = a1;
        this.b1 = b1;
    }
}
