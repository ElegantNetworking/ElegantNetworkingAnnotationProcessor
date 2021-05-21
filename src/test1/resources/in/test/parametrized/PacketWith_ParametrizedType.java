package test.parametrized;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;

@ElegantPacket
public class PacketWith_ParametrizedType implements ClientToServerPacket {

    Test<String, Integer, Long> v;

    public static class Test<A, B, C> {
        A a;
        B b;
        C c;
    }
}
