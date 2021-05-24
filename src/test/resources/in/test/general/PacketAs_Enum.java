package test.general;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;

@ElegantPacket
public enum PacketAs_Enum implements ClientToServerPacket {
    a, b, c
}
