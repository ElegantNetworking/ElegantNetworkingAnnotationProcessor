package test.set;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import java.util.LinkedHashSet;
import test.SomeValue;

@SerializerMark(
    packetClass = test.set.PacketWith_LinkedHashSet.class
)
public class PacketWith_LinkedHashSetSerializer implements ISerializer<PacketWith_LinkedHashSet> {
  public void serialize(PacketWith_LinkedHashSet value, ByteBuf acc) {
    serialize_PacketWith_LinkedHashSet_Generic(value, acc);
  }

  public PacketWith_LinkedHashSet unserialize(ByteBuf buf) {
    return unserialize_PacketWith_LinkedHashSet_Generic(buf);
  }

  void serialize_LinkedHashSet_of_SomeValue_Generic(LinkedHashSet<SomeValue> value, ByteBuf acc) {
    acc.writeInt(value.size());
    for (SomeValue e :value) {
      serialize_SomeValue_Generic(e,acc);
    }
  }

  LinkedHashSet<SomeValue> unserialize_LinkedHashSet_of_SomeValue_Generic(ByteBuf buf) {
    int size = buf.readInt();
    java.util.LinkedHashSet value = new java.util.LinkedHashSet();
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

  void serialize_PacketWith_LinkedHashSet_Generic(PacketWith_LinkedHashSet value, ByteBuf acc) {
    serialize_PacketWith_LinkedHashSet_Concretic(value, acc);
  }

  PacketWith_LinkedHashSet unserialize_PacketWith_LinkedHashSet_Generic(ByteBuf buf) {
    return unserialize_PacketWith_LinkedHashSet_Concretic(buf);
  }

  void serialize_PacketWith_LinkedHashSet_Concretic(PacketWith_LinkedHashSet value, ByteBuf acc) {
    serialize_LinkedHashSet_of_SomeValue_Generic(value.v, acc);
  }

  PacketWith_LinkedHashSet unserialize_PacketWith_LinkedHashSet_Concretic(ByteBuf buf) {
    PacketWith_LinkedHashSet value = new PacketWith_LinkedHashSet();
    value.v = unserialize_LinkedHashSet_of_SomeValue_Generic(buf);
    return value;
  }
}
