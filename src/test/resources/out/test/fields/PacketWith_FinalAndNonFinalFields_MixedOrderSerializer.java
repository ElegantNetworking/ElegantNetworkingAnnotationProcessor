package test.fields;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import test.SomeKey;
import test.SomeValue;

@SerializerMark(
    packetClass = test.fields.PacketWith_FinalAndNonFinalFields_MixedOrder.class
)
public class PacketWith_FinalAndNonFinalFields_MixedOrderSerializer implements ISerializer<PacketWith_FinalAndNonFinalFields_MixedOrder> {
  public void serialize(PacketWith_FinalAndNonFinalFields_MixedOrder value, ByteBuf acc) {
    serialize_PacketWith_FinalAndNonFinalFields_MixedOrder_Generic(value, acc);
  }

  public PacketWith_FinalAndNonFinalFields_MixedOrder unserialize(ByteBuf buf) {
    return unserialize_PacketWith_FinalAndNonFinalFields_MixedOrder_Generic(buf);
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

  void serialize_PacketWith_FinalAndNonFinalFields_MixedOrder_Generic(
      PacketWith_FinalAndNonFinalFields_MixedOrder value, ByteBuf acc) {
    serialize_PacketWith_FinalAndNonFinalFields_MixedOrder_Concretic(value, acc);
  }

  PacketWith_FinalAndNonFinalFields_MixedOrder unserialize_PacketWith_FinalAndNonFinalFields_MixedOrder_Generic(
      ByteBuf buf) {
    return unserialize_PacketWith_FinalAndNonFinalFields_MixedOrder_Concretic(buf);
  }

  void serialize_PacketWith_FinalAndNonFinalFields_MixedOrder_Concretic(
      PacketWith_FinalAndNonFinalFields_MixedOrder value, ByteBuf acc) {
    serialize_SomeKey_Generic(value.a1, acc);
    serialize_SomeKey_Generic(value.a2, acc);
    serialize_SomeValue_Generic(value.b1, acc);
    serialize_SomeValue_Generic(value.b2, acc);
  }

  PacketWith_FinalAndNonFinalFields_MixedOrder unserialize_PacketWith_FinalAndNonFinalFields_MixedOrder_Concretic(
      ByteBuf buf) {
    PacketWith_FinalAndNonFinalFields_MixedOrder value = new PacketWith_FinalAndNonFinalFields_MixedOrder(unserialize_SomeKey_Generic(buf), unserialize_SomeKey_Generic(buf));
    value.b1 = unserialize_SomeValue_Generic(buf);
    value.b2 = unserialize_SomeValue_Generic(buf);
    return value;
  }
}
