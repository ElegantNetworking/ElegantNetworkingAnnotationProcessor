package test.general;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeEnum;

@ElegantPacket
public class PacketWith_Enum implements ClientToServerPacket {
    SomeEnum v;
}
