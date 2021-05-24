package test.map;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeKey;
import test.SomeValue;

import java.util.TreeMap;

@ElegantPacket
public class PacketWith_TreeMap implements ClientToServerPacket {
    TreeMap<SomeKey, SomeValue> j3;
}
