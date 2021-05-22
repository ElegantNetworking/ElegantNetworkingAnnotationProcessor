package test.fields;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeKey;
import test.SomeValue;

@ElegantPacket
public class PacketWith_FinalAndNonFinalFields_MixedOrder implements ClientToServerPacket {
    final SomeKey a1;
    SomeValue b1;
    final SomeKey a2;
    SomeValue b2;

    public PacketWith_FinalAndNonFinalFields_MixedOrder(SomeKey a1, SomeKey a2) {
        this.a1 = a1;
        this.a2 = a2;
    }
}
