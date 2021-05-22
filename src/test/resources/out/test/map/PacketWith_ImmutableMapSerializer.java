package test.map;

import com.google.common.collect.ImmutableMap;
import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import test.SomeKey;
import test.SomeValue;

@SerializerMark(
    packetClass = test.map.PacketWith_ImmutableMap.class
)
public class PacketWith_ImmutableMapSerializer implements ISerializer<PacketWith_ImmutableMap> {
  public void serialize(PacketWith_ImmutableMap value, ByteBuf acc) {
    serialize_PacketWith_ImmutableMap_Generic(value, acc);
  }

  public PacketWith_ImmutableMap unserialize(ByteBuf buf) {
    return unserialize_PacketWith_ImmutableMap_Generic(buf);
  }

  void serialize_ImmutableMap_of_SomeKey_SomeValue_Generic(ImmutableMap<SomeKey, SomeValue> value,
      ByteBuf acc) {
    acc.writeInt(value.size());
    for (Map.Entry<SomeKey, SomeValue> entry :value.entrySet()) {
      SomeKey k = entry.getKey();
      SomeValue v = entry.getValue();
      serialize_SomeKey_Generic(k,acc);
      serialize_SomeValue_Generic(v,acc);
    }
  }

  ImmutableMap<SomeKey, SomeValue> unserialize_ImmutableMap_of_SomeKey_SomeValue_Generic(
      ByteBuf buf) {
    int size = buf.readInt();
    com.google.common.collect.ImmutableMap.Builder value = com.google.common.collect.ImmutableMap.builder();
    for (int i=0;i<size;i++) {
      SomeKey k = unserialize_SomeKey_Generic(buf);
      SomeValue v = unserialize_SomeValue_Generic(buf);
      value.put(k,v);
    }
    return value.build();
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

  void serialize_PacketWith_ImmutableMap_Generic(PacketWith_ImmutableMap value, ByteBuf acc) {
    serialize_PacketWith_ImmutableMap_Concretic(value, acc);
  }

  PacketWith_ImmutableMap unserialize_PacketWith_ImmutableMap_Generic(ByteBuf buf) {
    return unserialize_PacketWith_ImmutableMap_Concretic(buf);
  }

  void serialize_PacketWith_ImmutableMap_Concretic(PacketWith_ImmutableMap value, ByteBuf acc) {
    serialize_ImmutableMap_of_SomeKey_SomeValue_Generic(value.j5, acc);
  }

  PacketWith_ImmutableMap unserialize_PacketWith_ImmutableMap_Concretic(ByteBuf buf) {
    PacketWith_ImmutableMap value = new PacketWith_ImmutableMap();
    value.j5 = unserialize_ImmutableMap_of_SomeKey_SomeValue_Generic(buf);
    return value;
  }
}
