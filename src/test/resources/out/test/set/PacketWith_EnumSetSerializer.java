package test.set;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import test.SomeEnum;

@SerializerMark(
    packetClass = test.set.PacketWith_EnumSet.class
)
public class PacketWith_EnumSetSerializer implements ISerializer<PacketWith_EnumSet> {
  public void serialize(PacketWith_EnumSet value, ByteBuf acc) {
    serialize_PacketWith_EnumSet_Generic(value, acc);
  }

  public PacketWith_EnumSet unserialize(ByteBuf buf) {
    return unserialize_PacketWith_EnumSet_Generic(buf);
  }

  void serialize_EnumSet_of_SomeEnum_Generic(EnumSet<SomeEnum> value, ByteBuf acc) {
    acc.writeInt(value.size());
    for (SomeEnum e :value) {
      serialize_SomeEnum_Generic(e,acc);
    }
  }

  EnumSet<SomeEnum> unserialize_EnumSet_of_SomeEnum_Generic(ByteBuf buf) {
    int size = buf.readInt();
    java.util.EnumSet value = java.util.EnumSet.noneOf(test.SomeEnum.class);
    for (int i=0;i<size;i++) {
      SomeEnum e = unserialize_SomeEnum_Generic(buf);
      value.add(e);
    }
    return value;
  }

  void serialize_SomeEnum_Generic(SomeEnum value, ByteBuf acc) {
    acc.writeByte(value.ordinal());
  }

  SomeEnum unserialize_SomeEnum_Generic(ByteBuf buf) {
    return SomeEnum.values()[buf.readByte()];
  }

  void serialize_PacketWith_EnumSet_Generic(PacketWith_EnumSet value, ByteBuf acc) {
    serialize_PacketWith_EnumSet_Concretic(value, acc);
  }

  PacketWith_EnumSet unserialize_PacketWith_EnumSet_Generic(ByteBuf buf) {
    return unserialize_PacketWith_EnumSet_Concretic(buf);
  }

  void serialize_PacketWith_EnumSet_Concretic(PacketWith_EnumSet value, ByteBuf acc) {
    serialize_EnumSet_of_SomeEnum_Generic(value.v, acc);
  }

  PacketWith_EnumSet unserialize_PacketWith_EnumSet_Concretic(ByteBuf buf) {
    PacketWith_EnumSet value = new PacketWith_EnumSet();
    value.v = unserialize_EnumSet_of_SomeEnum_Generic(buf);
    return value;
  }
}
