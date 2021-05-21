package test.set;

import com.google.common.collect.ImmutableSet;
import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import test.SomeValue;

@SerializerMark(
    packetClass = test.set.PacketWith_ImmutableSet.class
)
public class PacketWith_ImmutableSetSerializer implements ISerializer<PacketWith_ImmutableSet> {
  public void serialize(PacketWith_ImmutableSet value, ByteBuf acc) {
    serialize_PacketWith_ImmutableSet_Generic(value, acc);
  }

  public PacketWith_ImmutableSet unserialize(ByteBuf buf) {
    return unserialize_PacketWith_ImmutableSet_Generic(buf);
  }

  void serialize_ImmutableSet_of_SomeValue_Generic(ImmutableSet<SomeValue> value, ByteBuf acc) {
    acc.writeInt(value.size());
    for (SomeValue e :value) {
      serialize_SomeValue_Generic(e,acc);
    }
  }

  ImmutableSet<SomeValue> unserialize_ImmutableSet_of_SomeValue_Generic(ByteBuf buf) {
    int size = buf.readInt();
    com.google.common.collect.ImmutableSet.Builder value = com.google.common.collect.ImmutableSet.builder();
    for (int i=0;i<size;i++) {
      SomeValue e = unserialize_SomeValue_Generic(buf);
      value.add(e);
    }
    return value.build();
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

  void serialize_PacketWith_ImmutableSet_Generic(PacketWith_ImmutableSet value, ByteBuf acc) {
    serialize_PacketWith_ImmutableSet_Concretic(value, acc);
  }

  PacketWith_ImmutableSet unserialize_PacketWith_ImmutableSet_Generic(ByteBuf buf) {
    return unserialize_PacketWith_ImmutableSet_Concretic(buf);
  }

  void serialize_PacketWith_ImmutableSet_Concretic(PacketWith_ImmutableSet value, ByteBuf acc) {
    serialize_ImmutableSet_of_SomeValue_Generic(value.v, acc);
  }

  PacketWith_ImmutableSet unserialize_PacketWith_ImmutableSet_Concretic(ByteBuf buf) {
    PacketWith_ImmutableSet value = new PacketWith_ImmutableSet();
    value.v = unserialize_ImmutableSet_of_SomeValue_Generic(buf);
    return value;
  }
}
