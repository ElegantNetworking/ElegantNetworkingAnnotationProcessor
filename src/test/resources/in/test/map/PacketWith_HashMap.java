package test.map;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeKey;
import test.SomeValue;

import java.util.HashMap;

@ElegantPacket
public class PacketWith_HashMap implements ClientToServerPacket {
    HashMap<SomeKey, SomeValue> j1;
}
