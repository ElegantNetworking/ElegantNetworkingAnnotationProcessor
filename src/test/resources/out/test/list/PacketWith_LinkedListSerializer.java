package test.list;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import java.util.LinkedList;
import test.SomeValue;

@SerializerMark(
    packetClass = test.list.PacketWith_LinkedList.class
)
public class PacketWith_LinkedListSerializer implements ISerializer<PacketWith_LinkedList> {
  public void serialize(PacketWith_LinkedList value, ByteBuf acc) {
    serialize_PacketWith_LinkedList_Generic(value, acc);
  }

  public PacketWith_LinkedList unserialize(ByteBuf buf) {
    return unserialize_PacketWith_LinkedList_Generic(buf);
  }

  void serialize_LinkedList_of_SomeValue_Generic(LinkedList<SomeValue> value, ByteBuf acc) {
    acc.writeInt(value.size());
    for (SomeValue e :value) {
      serialize_SomeValue_Generic(e,acc);
    }
  }

  LinkedList<SomeValue> unserialize_LinkedList_of_SomeValue_Generic(ByteBuf buf) {
    int size = buf.readInt();
    java.util.LinkedList value = new java.util.LinkedList();
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

  void serialize_PacketWith_LinkedList_Generic(PacketWith_LinkedList value, ByteBuf acc) {
    serialize_PacketWith_LinkedList_Concretic(value, acc);
  }

  PacketWith_LinkedList unserialize_PacketWith_LinkedList_Generic(ByteBuf buf) {
    return unserialize_PacketWith_LinkedList_Concretic(buf);
  }

  void serialize_PacketWith_LinkedList_Concretic(PacketWith_LinkedList value, ByteBuf acc) {
    serialize_LinkedList_of_SomeValue_Generic(value.v, acc);
  }

  PacketWith_LinkedList unserialize_PacketWith_LinkedList_Concretic(ByteBuf buf) {
    PacketWith_LinkedList value = new PacketWith_LinkedList();
    value.v = unserialize_LinkedList_of_SomeValue_Generic(buf);
    return value;
  }
}
