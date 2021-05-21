package test.list;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import test.SomeValue;

import java.util.LinkedList;

@ElegantPacket
public class PacketWith_LinkedList implements ClientToServerPacket {
    LinkedList<SomeValue> v;
}
