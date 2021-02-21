package test.set;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeEnum;

import java.util.EnumSet;

@ElegantPacket
public class PacketWith_EnumSet implements ClientToServerPacket {
    EnumSet<SomeEnum> v;
}
