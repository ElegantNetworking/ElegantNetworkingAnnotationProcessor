package test.set;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import java.util.Set;
import test.SomeValue;

@SerializerMark(
    packetClass = test.set.PacketWith_Set.class
)
public class PacketWith_SetSerializer implements ISerializer<PacketWith_Set> {
  public void serialize(PacketWith_Set value, ByteBuf acc) {
    serialize_PacketWith_Set_Generic(value, acc);
  }

  public PacketWith_Set unserialize(ByteBuf buf) {
    return unserialize_PacketWith_Set_Generic(buf);
  }

  void serialize_Set_of_SomeValue_Generic(Set<SomeValue> value, ByteBuf acc) {
    acc.writeInt(value.size());
    for (SomeValue e :value) {
      serialize_SomeValue_Generic(e,acc);
    }
  }

  Set<SomeValue> unserialize_Set_of_SomeValue_Generic(ByteBuf buf) {
    int size = buf.readInt();
    java.util.HashSet value = new java.util.HashSet();
    for (int i=0;i<size;i++) {
      SomeValue e = unserialize_SomeValue_Generic(buf);
      value.add(e);
    }
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

  void serialize_PacketWith_Set_Generic(PacketWith_Set value, ByteBuf acc) {
    serialize_PacketWith_Set_Concretic(value, acc);
  }

  PacketWith_Set unserialize_PacketWith_Set_Generic(ByteBuf buf) {
    return unserialize_PacketWith_Set_Concretic(buf);
  }

  void serialize_PacketWith_Set_Concretic(PacketWith_Set value, ByteBuf acc) {
    serialize_Set_of_SomeValue_Generic(value.v, acc);
  }

  PacketWith_Set unserialize_PacketWith_Set_Concretic(ByteBuf buf) {
    PacketWith_Set value = new PacketWith_Set();
    value.v = unserialize_Set_of_SomeValue_Generic(buf);
    return value;
  }
}
