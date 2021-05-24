package test.map;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeKey;
import test.SomeValue;

import java.util.Map;

@ElegantPacket
public class PacketWith_Map implements ClientToServerPacket {
    Map<SomeKey, SomeValue> j;
}
