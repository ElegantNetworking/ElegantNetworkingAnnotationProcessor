package test.fields;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;

@SerializerMark(
    packetClass = test.fields.PacketWith_PrivateFields.class
)
public class PacketWith_PrivateFieldsSerializer implements ISerializer<PacketWith_PrivateFields> {
  public void serialize(PacketWith_PrivateFields value, ByteBuf acc) {
    serialize_PacketWith_PrivateFields_Generic(value, acc);
  }

  public PacketWith_PrivateFields unserialize(ByteBuf buf) {
    return unserialize_PacketWith_PrivateFields_Generic(buf);
  }

  void serialize_PacketWith_PrivateFields_Generic(PacketWith_PrivateFields value, ByteBuf acc) {
    serialize_PacketWith_PrivateFields_Concretic(value, acc);
  }

  PacketWith_PrivateFields unserialize_PacketWith_PrivateFields_Generic(ByteBuf buf) {
    return unserialize_PacketWith_PrivateFields_Concretic(buf);
  }

  void serialize_PacketWith_PrivateFields_Concretic(PacketWith_PrivateFields value, ByteBuf acc) {
    serialize_Int_Generic(value.getA(), acc);
  }

  PacketWith_PrivateFields unserialize_PacketWith_PrivateFields_Concretic(ByteBuf buf) {
    PacketWith_PrivateFields value = new PacketWith_PrivateFields();
    value.setA(unserialize_Int_Generic(buf));
    return value;
  }
}
