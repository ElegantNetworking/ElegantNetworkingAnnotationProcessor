package test.parametrized;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;

@ElegantPacket
public class PacketWith_ParametrizedType_Multiple implements ClientToServerPacket {

    Test<String, Integer, Long> v1;
    Test<String, Integer, Long> v2;

    public static class Test<A, B, C> {
        A a;
        B b;
        C c;
    }
}
