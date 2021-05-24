package test.list;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import java.util.List;
import test.SomeValue;

@SerializerMark(
    packetClass = test.list.PacketWith_List.class
)
public class PacketWith_ListSerializer implements ISerializer<PacketWith_List> {
  public void serialize(PacketWith_List value, ByteBuf acc) {
    serialize_PacketWith_List_Generic(value, acc);
  }

  public PacketWith_List unserialize(ByteBuf buf) {
    return unserialize_PacketWith_List_Generic(buf);
  }

  void serialize_List_of_SomeValue_Generic(List<SomeValue> value, ByteBuf acc) {
    acc.writeInt(value.size());
    for (SomeValue e :value) {
      serialize_SomeValue_Generic(e,acc);
    }
  }

  List<SomeValue> unserialize_List_of_SomeValue_Generic(ByteBuf buf) {
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

  void serialize_PacketWith_List_Generic(PacketWith_List value, ByteBuf acc) {
    serialize_PacketWith_List_Concretic(value, acc);
  }

  PacketWith_List unserialize_PacketWith_List_Generic(ByteBuf buf) {
    return unserialize_PacketWith_List_Concretic(buf);
  }

  void serialize_PacketWith_List_Concretic(PacketWith_List value, ByteBuf acc) {
    serialize_List_of_SomeValue_Generic(value.v, acc);
  }

  PacketWith_List unserialize_PacketWith_List_Concretic(ByteBuf buf) {
    PacketWith_List value = new PacketWith_List();
    value.v = unserialize_List_of_SomeValue_Generic(buf);
    return value;
  }
}
