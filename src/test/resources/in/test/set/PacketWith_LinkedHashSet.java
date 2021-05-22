package test.set;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeValue;

import java.util.LinkedHashSet;

@ElegantPacket
public class PacketWith_LinkedHashSet implements ClientToServerPacket {
    LinkedHashSet<SomeValue> v;
}
