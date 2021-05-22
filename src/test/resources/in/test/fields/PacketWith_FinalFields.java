package test.fields;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeEnum;
import test.SomeKey;
import test.SomeValue;

@ElegantPacket
public class PacketWith_FinalFields implements ClientToServerPacket {
    final SomeKey a;
    final SomeValue b;
    final SomeEnum c;

    public PacketWith_FinalFields(SomeKey a, SomeValue b, SomeEnum c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}
