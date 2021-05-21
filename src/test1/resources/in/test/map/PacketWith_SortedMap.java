package test.map;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeKey;
import test.SomeValue;

import java.util.SortedMap;

@ElegantPacket
public class PacketWith_SortedMap implements ClientToServerPacket {
    SortedMap<SomeKey, SomeValue> j6;
}
