package test.general;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import test.SomeEnum;
import test.SomeKey;
import test.SomeValue;

@SerializerMark(
    packetClass = test.general.PacketAs_Product.class
)
public class PacketAs_ProductSerializer implements ISerializer<PacketAs_Product> {
  public void serialize(PacketAs_Product value, ByteBuf acc) {
    serialize_PacketAs_Product_Generic(value, acc);
  }

  public PacketAs_Product unserialize(ByteBuf buf) {
    return unserialize_PacketAs_Product_Generic(buf);
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

  void serialize_PacketAs_Product_Generic(PacketAs_Product value, ByteBuf acc) {
    serialize_PacketAs_Product_Concretic(value, acc);
  }

  PacketAs_Product unserialize_PacketAs_Product_Generic(ByteBuf buf) {
    return unserialize_PacketAs_Product_Concretic(buf);
  }

  void serialize_PacketAs_Product_Concretic(PacketAs_Product value, ByteBuf acc) {
    serialize_SomeKey_Generic(value.a, acc);
    serialize_SomeValue_Generic(value.b, acc);
    serialize_SomeEnum_Generic(value.c, acc);
  }

  PacketAs_Product unserialize_PacketAs_Product_Concretic(ByteBuf buf) {
    PacketAs_Product value = new PacketAs_Product();
    value.a = unserialize_SomeKey_Generic(buf);
    value.b = unserialize_SomeValue_Generic(buf);
    value.c = unserialize_SomeEnum_Generic(buf);
    return value;
  }
}
