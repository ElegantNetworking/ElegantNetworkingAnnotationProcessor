package test.list;

import com.google.common.collect.ImmutableList;
import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeValue;

@ElegantPacket
public class PacketWith_ImmutableList implements ClientToServerPacket {
    ImmutableList<SomeValue> v;
}
