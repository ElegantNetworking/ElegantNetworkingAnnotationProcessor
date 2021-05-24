package test.map;

import com.google.common.collect.ImmutableMap;
import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeKey;
import test.SomeValue;

@ElegantPacket
public class PacketWith_ImmutableMap implements ClientToServerPacket {
    ImmutableMap<SomeKey, SomeValue> j5;
}
