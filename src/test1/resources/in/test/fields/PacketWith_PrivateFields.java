package test.fields;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;

@ElegantPacket
public class PacketWith_PrivateFields implements ClientToServerPacket {
    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    private int a;
}
