package test.set;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import test.SomeValue;

@SerializerMark(
    packetClass = test.set.PacketWith_HashSet.class
)
public class PacketWith_HashSetSerializer implements ISerializer<PacketWith_HashSet> {
  public void serialize(PacketWith_HashSet value, ByteBuf acc) {
    serialize_PacketWith_HashSet_Generic(value, acc);
  }

  public PacketWith_HashSet unserialize(ByteBuf buf) {
    return unserialize_PacketWith_HashSet_Generic(buf);
  }

  void serialize_HashSet_of_SomeValue_Generic(HashSet<SomeValue> value, ByteBuf acc) {
    acc.writeInt(value.size());
    for (SomeValue e :value) {
      serialize_SomeValue_Generic(e,acc);
    }
  }

  HashSet<SomeValue> unserialize_HashSet_of_SomeValue_Generic(ByteBuf buf) {
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

  void serialize_PacketWith_HashSet_Generic(PacketWith_HashSet value, ByteBuf acc) {
    serialize_PacketWith_HashSet_Concretic(value, acc);
  }

  PacketWith_HashSet unserialize_PacketWith_HashSet_Generic(ByteBuf buf) {
    return unserialize_PacketWith_HashSet_Concretic(buf);
  }

  void serialize_PacketWith_HashSet_Concretic(PacketWith_HashSet value, ByteBuf acc) {
    serialize_HashSet_of_SomeValue_Generic(value.v, acc);
  }

  PacketWith_HashSet unserialize_PacketWith_HashSet_Concretic(ByteBuf buf) {
    PacketWith_HashSet value = new PacketWith_HashSet();
    value.v = unserialize_HashSet_of_SomeValue_Generic(buf);
    return value;
  }
}
