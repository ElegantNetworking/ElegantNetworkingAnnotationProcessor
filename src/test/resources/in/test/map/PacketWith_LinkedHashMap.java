package test.map;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeKey;
import test.SomeValue;

import java.util.LinkedHashMap;

@ElegantPacket
public class PacketWith_LinkedHashMap implements ClientToServerPacket {
    LinkedHashMap<SomeKey, SomeValue> j2;
}
