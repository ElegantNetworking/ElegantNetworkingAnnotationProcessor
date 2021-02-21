package test.fields;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;

@SerializerMark(
    packetClass = test.fields.PacketWith_NoFields.class
)
public class PacketWith_NoFieldsSerializer implements ISerializer<PacketWith_NoFields> {
  public void serialize(PacketWith_NoFields value, ByteBuf acc) {
    serialize_PacketWith_NoFields_Generic(value, acc);
  }

  public PacketWith_NoFields unserialize(ByteBuf buf) {
    return unserialize_PacketWith_NoFields_Generic(buf);
  }

  void serialize_PacketWith_NoFields_Generic(PacketWith_NoFields value, ByteBuf acc) {
    serialize_PacketWith_NoFields_Concretic(value, acc);
  }

  PacketWith_NoFields unserialize_PacketWith_NoFields_Generic(ByteBuf buf) {
    return unserialize_PacketWith_NoFields_Concretic(buf);
  }

  void serialize_PacketWith_NoFields_Concretic(PacketWith_NoFields value, ByteBuf acc) {
  }

  PacketWith_NoFields unserialize_PacketWith_NoFields_Concretic(ByteBuf buf) {
    PacketWith_NoFields value = new PacketWith_NoFields();
    return value;
  }
}
