package test.set;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeValue;

import java.util.HashSet;

@ElegantPacket
public class PacketWith_HashSet implements ClientToServerPacket {
    HashSet<SomeValue> v;
}
