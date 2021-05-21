package test.list;

import com.google.common.collect.ImmutableList;
import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import test.SomeValue;

@SerializerMark(
    packetClass = test.list.PacketWith_ImmutableList.class
)
public class PacketWith_ImmutableListSerializer implements ISerializer<PacketWith_ImmutableList> {
  public void serialize(PacketWith_ImmutableList value, ByteBuf acc) {
    serialize_PacketWith_ImmutableList_Generic(value, acc);
  }

  public PacketWith_ImmutableList unserialize(ByteBuf buf) {
    return unserialize_PacketWith_ImmutableList_Generic(buf);
  }

  void serialize_ImmutableList_of_SomeValue_Generic(ImmutableList<SomeValue> value, ByteBuf acc) {
    acc.writeInt(value.size());
    for (SomeValue e :value) {
      serialize_SomeValue_Generic(e,acc);
    }
  }

  ImmutableList<SomeValue> unserialize_ImmutableList_of_SomeValue_Generic(ByteBuf buf) {
    int size = buf.readInt();
    com.google.common.collect.ImmutableList.Builder value = com.google.common.collect.ImmutableList.builder();
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

  void serialize_PacketWith_ImmutableList_Generic(PacketWith_ImmutableList value, ByteBuf acc) {
    serialize_PacketWith_ImmutableList_Concretic(value, acc);
  }

  PacketWith_ImmutableList unserialize_PacketWith_ImmutableList_Generic(ByteBuf buf) {
    return unserialize_PacketWith_ImmutableList_Concretic(buf);
  }

  void serialize_PacketWith_ImmutableList_Concretic(PacketWith_ImmutableList value, ByteBuf acc) {
    serialize_ImmutableList_of_SomeValue_Generic(value.v, acc);
  }

  PacketWith_ImmutableList unserialize_PacketWith_ImmutableList_Concretic(ByteBuf buf) {
    PacketWith_ImmutableList value = new PacketWith_ImmutableList();
    value.v = unserialize_ImmutableList_of_SomeValue_Generic(buf);
    return value;
  }
}
