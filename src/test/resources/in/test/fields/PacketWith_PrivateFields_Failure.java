package test.fields;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;

@ElegantPacket
public class PacketWith_PrivateFields_Failure implements ClientToServerPacket {
    private int a;
}
