package test.list;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeValue;

import java.util.ArrayList;

@ElegantPacket
public class PacketWith_ArrayList implements ClientToServerPacket {
    ArrayList<SomeValue> v;
}
