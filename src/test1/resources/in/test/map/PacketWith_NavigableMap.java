package test.map;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeKey;
import test.SomeValue;

import java.util.NavigableMap;

@ElegantPacket
public class PacketWith_NavigableMap implements ClientToServerPacket {
    NavigableMap<SomeKey, SomeValue> j7;
}
