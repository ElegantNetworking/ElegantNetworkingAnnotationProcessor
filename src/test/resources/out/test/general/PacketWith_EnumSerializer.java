package test.general;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import test.SomeEnum;

@SerializerMark(
    packetClass = test.general.PacketWith_Enum.class
)
public class PacketWith_EnumSerializer implements ISerializer<PacketWith_Enum> {
  public void serialize(PacketWith_Enum value, ByteBuf acc) {
    serialize_PacketWith_Enum_Generic(value, acc);
  }

  public PacketWith_Enum unserialize(ByteBuf buf) {
    return unserialize_PacketWith_Enum_Generic(buf);
  }

  void serialize_SomeEnum_Generic(SomeEnum value, ByteBuf acc) {
    acc.writeByte(value.ordinal());
  }

  SomeEnum unserialize_SomeEnum_Generic(ByteBuf buf) {
    return SomeEnum.values()[buf.readByte()];
  }

  void serialize_PacketWith_Enum_Generic(PacketWith_Enum value, ByteBuf acc) {
    serialize_PacketWith_Enum_Concretic(value, acc);
  }

  PacketWith_Enum unserialize_PacketWith_Enum_Generic(ByteBuf buf) {
    return unserialize_PacketWith_Enum_Concretic(buf);
  }

  void serialize_PacketWith_Enum_Concretic(PacketWith_Enum value, ByteBuf acc) {
    serialize_SomeEnum_Generic(value.v, acc);
  }

  PacketWith_Enum unserialize_PacketWith_Enum_Concretic(ByteBuf buf) {
    PacketWith_Enum value = new PacketWith_Enum();
    value.v = unserialize_SomeEnum_Generic(buf);
    return value;
  }
}
