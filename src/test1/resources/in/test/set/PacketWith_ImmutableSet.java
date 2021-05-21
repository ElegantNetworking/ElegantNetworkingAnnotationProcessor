package test.set;

import com.google.common.collect.ImmutableSet;
import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeValue;

@ElegantPacket
public class PacketWith_ImmutableSet implements ClientToServerPacket {
    ImmutableSet<SomeValue> v;
}
