package test.list;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import test.SomeValue;

@SerializerMark(
    packetClass = test.list.PacketWith_ArrayList.class
)
public class PacketWith_ArrayListSerializer implements ISerializer<PacketWith_ArrayList> {
  public void serialize(PacketWith_ArrayList value, ByteBuf acc) {
    serialize_PacketWith_ArrayList_Generic(value, acc);
  }

  public PacketWith_ArrayList unserialize(ByteBuf buf) {
    return unserialize_PacketWith_ArrayList_Generic(buf);
  }

  void serialize_ArrayList_of_SomeValue_Generic(ArrayList<SomeValue> value, ByteBuf acc) {
    acc.writeInt(value.size());
    for (SomeValue e :value) {
      serialize_SomeValue_Generic(e,acc);
    }
  }

  ArrayList<SomeValue> unserialize_ArrayList_of_SomeValue_Generic(ByteBuf buf) {
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

  void serialize_PacketWith_ArrayList_Generic(PacketWith_ArrayList value, ByteBuf acc) {
    serialize_PacketWith_ArrayList_Concretic(value, acc);
  }

  PacketWith_ArrayList unserialize_PacketWith_ArrayList_Generic(ByteBuf buf) {
    return unserialize_PacketWith_ArrayList_Concretic(buf);
  }

  void serialize_PacketWith_ArrayList_Concretic(PacketWith_ArrayList value, ByteBuf acc) {
    serialize_ArrayList_of_SomeValue_Generic(value.v, acc);
  }

  PacketWith_ArrayList unserialize_PacketWith_ArrayList_Concretic(ByteBuf buf) {
    PacketWith_ArrayList value = new PacketWith_ArrayList();
    value.v = unserialize_ArrayList_of_SomeValue_Generic(buf);
    return value;
  }
}
