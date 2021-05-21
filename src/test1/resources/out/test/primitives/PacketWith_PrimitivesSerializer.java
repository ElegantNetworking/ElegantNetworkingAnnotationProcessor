package test.primitives;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;

@SerializerMark(
    packetClass = test.primitives.PacketWith_Primitives.class
)
public class PacketWith_PrimitivesSerializer implements ISerializer<PacketWith_Primitives> {
  public void serialize(PacketWith_Primitives value, ByteBuf acc) {
    serialize_PacketWith_Primitives_Generic(value, acc);
  }

  public PacketWith_Primitives unserialize(ByteBuf buf) {
    return unserialize_PacketWith_Primitives_Generic(buf);
  }

  void serialize_PacketWith_Primitives_Generic(PacketWith_Primitives value, ByteBuf acc) {
    serialize_PacketWith_Primitives_Concretic(value, acc);
  }

  PacketWith_Primitives unserialize_PacketWith_Primitives_Generic(ByteBuf buf) {
    return unserialize_PacketWith_Primitives_Concretic(buf);
  }

  void serialize_PacketWith_Primitives_Concretic(PacketWith_Primitives value, ByteBuf acc) {
    serialize_Int_Generic(value.a, acc);
    serialize_Long_Generic(value.b, acc);
    serialize_Short_Generic(value.c, acc);
    serialize_Byte_Generic(value.d, acc);
    serialize_Char_Generic(value.e, acc);
    serialize_Float_Generic(value.f, acc);
    serialize_Double_Generic(value.g, acc);
    serialize_Boolean_Generic(value.h, acc);
    serialize_Int_Generic(value.a1, acc);
    serialize_Long_Generic(value.b1, acc);
    serialize_Short_Generic(value.c1, acc);
    serialize_Byte_Generic(value.d1, acc);
    serialize_Char_Generic(value.e1, acc);
    serialize_Float_Generic(value.f1, acc);
    serialize_Double_Generic(value.g1, acc);
    serialize_Boolean_Generic(value.h1, acc);
  }

  PacketWith_Primitives unserialize_PacketWith_Primitives_Concretic(ByteBuf buf) {
    PacketWith_Primitives value = new PacketWith_Primitives();
    value.a = unserialize_Int_Generic(buf);
    value.b = unserialize_Long_Generic(buf);
    value.c = unserialize_Short_Generic(buf);
    value.d = unserialize_Byte_Generic(buf);
    value.e = unserialize_Char_Generic(buf);
    value.f = unserialize_Float_Generic(buf);
    value.g = unserialize_Double_Generic(buf);
    value.h = unserialize_Boolean_Generic(buf);
    value.a1 = unserialize_Int_Generic(buf);
    value.b1 = unserialize_Long_Generic(buf);
    value.c1 = unserialize_Short_Generic(buf);
    value.d1 = unserialize_Byte_Generic(buf);
    value.e1 = unserialize_Char_Generic(buf);
    value.f1 = unserialize_Float_Generic(buf);
    value.g1 = unserialize_Double_Generic(buf);
    value.h1 = unserialize_Boolean_Generic(buf);
    return value;
  }
}
