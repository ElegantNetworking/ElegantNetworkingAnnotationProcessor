package test.list;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeValue;

import java.util.List;

@ElegantPacket
public class PacketWith_List implements ClientToServerPacket {
    List<SomeValue> v;
}
