package test.list;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import test.SomeValue;

@SerializerMark(
    packetClass = test.list.PacketWith_Collection.class
)
public class PacketWith_CollectionSerializer implements ISerializer<PacketWith_Collection> {
  public void serialize(PacketWith_Collection value, ByteBuf acc) {
    serialize_PacketWith_Collection_Generic(value, acc);
  }

  public PacketWith_Collection unserialize(ByteBuf buf) {
    return unserialize_PacketWith_Collection_Generic(buf);
  }

  void serialize_Collection_of_SomeValue_Generic(Collection<SomeValue> value, ByteBuf acc) {
    acc.writeInt(value.size());
    for (SomeValue e :value) {
      serialize_SomeValue_Generic(e,acc);
    }
  }

  Collection<SomeValue> unserialize_Collection_of_SomeValue_Generic(ByteBuf buf) {
    int size = buf.readInt();
    java.util.ArrayList value = new java.util.ArrayList();
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

  void serialize_PacketWith_Collection_Generic(PacketWith_Collection value, ByteBuf acc) {
    serialize_PacketWith_Collection_Concretic(value, acc);
  }

  PacketWith_Collection unserialize_PacketWith_Collection_Generic(ByteBuf buf) {
    return unserialize_PacketWith_Collection_Concretic(buf);
  }

  void serialize_PacketWith_Collection_Concretic(PacketWith_Collection value, ByteBuf acc) {
    serialize_Collection_of_SomeValue_Generic(value.v, acc);
  }

  PacketWith_Collection unserialize_PacketWith_Collection_Concretic(ByteBuf buf) {
    PacketWith_Collection value = new PacketWith_Collection();
    value.v = unserialize_Collection_of_SomeValue_Generic(buf);
    return value;
  }
}
