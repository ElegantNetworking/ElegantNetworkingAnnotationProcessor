package test.primitives;

import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;

@ElegantPacket
public class PacketWith_Primitives implements ClientToServerPacket {
    public int a;
    public long b;
    public short c;
    public byte d;
    public char e;
    public float f;
    public double g;
    public boolean h;

    public Integer a1;
    public Long b1;
    public Short c1;
    public Byte d1;
    public Character e1;
    public Float f1;
    public Double g1;
    public Boolean h1;
}
