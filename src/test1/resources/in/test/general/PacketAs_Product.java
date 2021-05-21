package test.general;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeEnum;
import test.SomeKey;
import test.SomeValue;

@ElegantPacket
public class PacketAs_Product implements ClientToServerPacket {
    SomeKey a;
    SomeValue b;
    SomeEnum c;
}
