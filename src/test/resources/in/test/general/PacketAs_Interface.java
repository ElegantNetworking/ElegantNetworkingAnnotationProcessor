package test.general;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;

@ElegantPacket
public interface PacketAs_Interface extends ClientToServerPacket {
    public class Impl1 implements PacketAs_Interface {

    }
    public class Impl2 implements PacketAs_Interface {

    }
}
