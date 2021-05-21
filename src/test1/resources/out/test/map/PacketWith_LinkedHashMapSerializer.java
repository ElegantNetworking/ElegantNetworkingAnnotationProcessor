package test.map;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import java.util.LinkedHashMap;
import java.util.Map;
import test.SomeKey;
import test.SomeValue;

@SerializerMark(
    packetClass = test.map.PacketWith_LinkedHashMap.class
)
public class PacketWith_LinkedHashMapSerializer implements ISerializer<PacketWith_LinkedHashMap> {
  public void serialize(PacketWith_LinkedHashMap value, ByteBuf acc) {
    serialize_PacketWith_LinkedHashMap_Generic(value, acc);
  }

  public PacketWith_LinkedHashMap unserialize(ByteBuf buf) {
    return unserialize_PacketWith_LinkedHashMap_Generic(buf);
  }

  void serialize_LinkedHashMap_of_SomeKey_SomeValue_Generic(LinkedHashMap<SomeKey, SomeValue> value,
      ByteBuf acc) {
    acc.writeInt(value.size());
    for (Map.Entry<SomeKey, SomeValue> entry :value.entrySet()) {
      SomeKey k = entry.getKey();
      SomeValue v = entry.getValue();
      serialize_SomeKey_Generic(k,acc);
      serialize_SomeValue_Generic(v,acc);
    }
  }

  LinkedHashMap<SomeKey, SomeValue> unserialize_LinkedHashMap_of_SomeKey_SomeValue_Generic(
      ByteBuf buf) {
    int size = buf.readInt();
    java.util.LinkedHashMap value = new java.util.LinkedHashMap();
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

  void serialize_PacketWith_LinkedHashMap_Generic(PacketWith_LinkedHashMap value, ByteBuf acc) {
    serialize_PacketWith_LinkedHashMap_Concretic(value, acc);
  }

  PacketWith_LinkedHashMap unserialize_PacketWith_LinkedHashMap_Generic(ByteBuf buf) {
    return unserialize_PacketWith_LinkedHashMap_Concretic(buf);
  }

  void serialize_PacketWith_LinkedHashMap_Concretic(PacketWith_LinkedHashMap value, ByteBuf acc) {
    serialize_LinkedHashMap_of_SomeKey_SomeValue_Generic(value.j2, acc);
  }

  PacketWith_LinkedHashMap unserialize_PacketWith_LinkedHashMap_Concretic(ByteBuf buf) {
    PacketWith_LinkedHashMap value = new PacketWith_LinkedHashMap();
    value.j2 = unserialize_LinkedHashMap_of_SomeKey_SomeValue_Generic(buf);
    return value;
  }
}
