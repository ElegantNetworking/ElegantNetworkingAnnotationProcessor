package test.fields;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import test.SomeEnum;
import test.SomeKey;
import test.SomeValue;

@SerializerMark(
    packetClass = test.fields.PacketWith_FinalFields.class
)
public class PacketWith_FinalFieldsSerializer implements ISerializer<PacketWith_FinalFields> {
  public void serialize(PacketWith_FinalFields value, ByteBuf acc) {
    serialize_PacketWith_FinalFields_Generic(value, acc);
  }

  public PacketWith_FinalFields unserialize(ByteBuf buf) {
    return unserialize_PacketWith_FinalFields_Generic(buf);
  }

  void serialize_SomeEnum_Generic(SomeEnum value, ByteBuf acc) {
    acc.writeByte(value.ordinal());
  }

  SomeEnum unserialize_SomeEnum_Generic(ByteBuf buf) {
    return SomeEnum.values()[buf.readByte()];
  }

  void serialize_SomeKey_Generic(SomeKey value, ByteBuf acc) {
    serialize_SomeKey_Concretic(value, acc);
  }

  SomeKey unserialize_SomeKey_Generic(ByteBuf buf) {
    return unserialize_SomeKey_Concretic(buf);
  }

  void serialize_SomeKey_Concretic(SomeKey value, ByteBuf acc) {
  }

  SomeKey unserialize_SomeKey_Concretic(ByteBuf buf) {
    SomeKey value = new SomeKey();
    return value;
  }

  void serialize_SomeValue_Generic(SomeValue value, ByteBuf acc) {
    serialize_SomeValue_Concretic(value, acc);
  }

  SomeValue unserialize_SomeValue_Generic(ByteBuf buf) {
    return unserialize_SomeValue_Concretic(buf);
  }

  void serialize_SomeValue_Concretic(SomeValue value, ByteBuf acc) {
  }

  SomeValue unserialize_SomeValue_Concretic(ByteBuf buf) {
    SomeValue value = new SomeValue();
    return value;
  }

  void serialize_PacketWith_FinalFields_Generic(PacketWith_FinalFields value, ByteBuf acc) {
    serialize_PacketWith_FinalFields_Concretic(value, acc);
  }

  PacketWith_FinalFields unserialize_PacketWith_FinalFields_Generic(ByteBuf buf) {
    return unserialize_PacketWith_FinalFields_Concretic(buf);
  }

  void serialize_PacketWith_FinalFields_Concretic(PacketWith_FinalFields value, ByteBuf acc) {
    serialize_SomeKey_Generic(value.a, acc);
    serialize_SomeValue_Generic(value.b, acc);
    serialize_SomeEnum_Generic(value.c, acc);
  }

  PacketWith_FinalFields unserialize_PacketWith_FinalFields_Concretic(ByteBuf buf) {
    PacketWith_FinalFields value = new PacketWith_FinalFields(unserialize_SomeKey_Generic(buf), unserialize_SomeValue_Generic(buf), unserialize_SomeEnum_Generic(buf));
    return value;
  }
}
