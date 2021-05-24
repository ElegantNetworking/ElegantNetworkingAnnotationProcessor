package test.list;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeValue;

import java.util.Collection;

@ElegantPacket
public class PacketWith_Collection implements ClientToServerPacket {
    Collection<SomeValue> v;
}
