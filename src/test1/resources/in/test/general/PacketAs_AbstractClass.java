package test.general;

import hohserg.elegant.networking.api.ElegantPacket;
import hohserg.elegant.networking.api.ServerToClientPacket;

@ElegantPacket
public abstract class PacketAs_AbstractClass implements ServerToClientPacket {
    public static class Impl1 extends PacketAs_AbstractClass {

    }

    public static class Impl2 extends PacketAs_AbstractClass {

    }
}
