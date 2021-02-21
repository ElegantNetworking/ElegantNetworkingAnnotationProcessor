package test.general;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;

@SerializerMark(
    packetClass = test.general.PacketAs_Enum.class
)
public class PacketAs_EnumSerializer implements ISerializer<PacketAs_Enum> {
  public void serialize(PacketAs_Enum value, ByteBuf acc) {
    serialize_PacketAs_Enum_Generic(value, acc);
  }

  public PacketAs_Enum unserialize(ByteBuf buf) {
    return unserialize_PacketAs_Enum_Generic(buf);
  }

  void serialize_PacketAs_Enum_Generic(PacketAs_Enum value, ByteBuf acc) {
    acc.writeByte(value.ordinal());
  }

  PacketAs_Enum unserialize_PacketAs_Enum_Generic(ByteBuf buf) {
    return PacketAs_Enum.values()[buf.readByte()];
  }
}
