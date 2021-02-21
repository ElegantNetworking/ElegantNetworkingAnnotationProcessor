package test.set;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeValue;

import java.util.Set;

@ElegantPacket
public class PacketWith_Set implements ClientToServerPacket {
    Set<SomeValue> v;
}
