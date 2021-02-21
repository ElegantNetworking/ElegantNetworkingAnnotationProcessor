package test.map;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.NavigableMap;
import test.SomeKey;
import test.SomeValue;

@SerializerMark(
    packetClass = test.map.PacketWith_NavigableMap.class
)
public class PacketWith_NavigableMapSerializer implements ISerializer<PacketWith_NavigableMap> {
  public void serialize(PacketWith_NavigableMap value, ByteBuf acc) {
    serialize_PacketWith_NavigableMap_Generic(value, acc);
  }

  public PacketWith_NavigableMap unserialize(ByteBuf buf) {
    return unserialize_PacketWith_NavigableMap_Generic(buf);
  }

  void serialize_NavigableMap_of_SomeKey_SomeValue_Generic(NavigableMap<SomeKey, SomeValue> value,
      ByteBuf acc) {
    acc.writeInt(value.size());
    for (Map.Entry<SomeKey, SomeValue> entry :value.entrySet()) {
      SomeKey k = entry.getKey();
      SomeValue v = entry.getValue();
      serialize_SomeKey_Generic(k,acc);
      serialize_SomeValue_Generic(v,acc);
    }
  }

  NavigableMap<SomeKey, SomeValue> unserialize_NavigableMap_of_SomeKey_SomeValue_Generic(
      ByteBuf buf) {
    int size = buf.readInt();
    java.util.TreeMap value = new java.util.TreeMap();
    for (int i=0;i<size;i++) {
      SomeKey k = unserialize_SomeKey_Generic(buf);
      SomeValue v = unserialize_SomeValue_Generic(buf);
      value.put(k,v);
    }
    return value;
  }

  void serialize_SomeKey_Generic(SomeKey value, ByteBuf acc) {
    serialize_SomeKey_Concretic(value, acc);
  }

  SomeKey unserialize_SomeKey_Generic(ByteBuf buf) {
    return unserialize_SomeKey_Concretic(buf);
  }

  void serialize_SomeKey_Concretic(SomeKey value, ByteBuf acc) {
  }

  SomeKey unserialize_SomeKey_Concretic(ByteBuf buf) {
    SomeKey value = new SomeKey();
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

  void serialize_PacketWith_NavigableMap_Generic(PacketWith_NavigableMap value, ByteBuf acc) {
    serialize_PacketWith_NavigableMap_Concretic(value, acc);
  }

  PacketWith_NavigableMap unserialize_PacketWith_NavigableMap_Generic(ByteBuf buf) {
    return unserialize_PacketWith_NavigableMap_Concretic(buf);
  }

  void serialize_PacketWith_NavigableMap_Concretic(PacketWith_NavigableMap value, ByteBuf acc) {
    serialize_NavigableMap_of_SomeKey_SomeValue_Generic(value.j7, acc);
  }

  PacketWith_NavigableMap unserialize_PacketWith_NavigableMap_Concretic(ByteBuf buf) {
    PacketWith_NavigableMap value = new PacketWith_NavigableMap();
    value.j7 = unserialize_NavigableMap_of_SomeKey_SomeValue_Generic(buf);
    return value;
  }
}
