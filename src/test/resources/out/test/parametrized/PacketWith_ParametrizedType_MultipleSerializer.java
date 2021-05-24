package test.parametrized;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;

@SerializerMark(
    packetClass = test.parametrized.PacketWith_ParametrizedType_Multiple.class
)
public class PacketWith_ParametrizedType_MultipleSerializer implements ISerializer<PacketWith_ParametrizedType_Multiple> {
  public void serialize(PacketWith_ParametrizedType_Multiple value, ByteBuf acc) {
    serialize_PacketWith_ParametrizedType_Multiple_Generic(value, acc);
  }

  public PacketWith_ParametrizedType_Multiple unserialize(ByteBuf buf) {
    return unserialize_PacketWith_ParametrizedType_Multiple_Generic(buf);
  }

  void serialize_PacketWith_ParametrizedType_Multiple_Generic(
      PacketWith_ParametrizedType_Multiple value, ByteBuf acc) {
    serialize_PacketWith_ParametrizedType_Multiple_Concretic(value, acc);
  }

  PacketWith_ParametrizedType_Multiple unserialize_PacketWith_ParametrizedType_Multiple_Generic(
      ByteBuf buf) {
    return unserialize_PacketWith_ParametrizedType_Multiple_Concretic(buf);
  }

  void serialize_PacketWith_ParametrizedType_Multiple_Concretic(
      PacketWith_ParametrizedType_Multiple value, ByteBuf acc) {
    serialize_Test_of_String_Int_Long_Generic(value.v1, acc);
    serialize_Test_of_String_Int_Long_Generic(value.v2, acc);
  }

  PacketWith_ParametrizedType_Multiple unserialize_PacketWith_ParametrizedType_Multiple_Concretic(
      ByteBuf buf) {
    PacketWith_ParametrizedType_Multiple value = new PacketWith_ParametrizedType_Multiple();
    value.v1 = unserialize_Test_of_String_Int_Long_Generic(buf);
    value.v2 = unserialize_Test_of_String_Int_Long_Generic(buf);
    return value;
  }

  void serialize_Test_of_String_Int_Long_Generic(
      PacketWith_ParametrizedType_Multiple.Test<String, Integer, Long> value, ByteBuf acc) {
    serialize_Test_of_String_Int_Long_Concretic(value, acc);
  }

  PacketWith_ParametrizedType_Multiple.Test<String, Integer, Long> unserialize_Test_of_String_Int_Long_Generic(
      ByteBuf buf) {
    return unserialize_Test_of_String_Int_Long_Concretic(buf);
  }

  void serialize_Test_of_String_Int_Long_Concretic(
      PacketWith_ParametrizedType_Multiple.Test<String, Integer, Long> value, ByteBuf acc) {
    serialize_String_Generic(value.a, acc);
    serialize_Int_Generic(value.b, acc);
    serialize_Long_Generic(value.c, acc);
  }

  PacketWith_ParametrizedType_Multiple.Test<String, Integer, Long> unserialize_Test_of_String_Int_Long_Concretic(
      ByteBuf buf) {
    PacketWith_ParametrizedType_Multiple.Test<String, Integer, Long> value = new PacketWith_ParametrizedType_Multiple.Test<String, Integer, Long>();
    value.a = unserialize_String_Generic(buf);
    value.b = unserialize_Int_Generic(buf);
    value.c = unserialize_Long_Generic(buf);
    return value;
  }
}
