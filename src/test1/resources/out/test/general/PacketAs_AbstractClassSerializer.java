package test.general;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;

@SerializerMark(
    packetClass = test.general.PacketAs_AbstractClass.class
)
public class PacketAs_AbstractClassSerializer implements ISerializer<PacketAs_AbstractClass> {
  public void serialize(PacketAs_AbstractClass value, ByteBuf acc) {
    serialize_PacketAs_AbstractClass_Generic(value, acc);
  }

  public PacketAs_AbstractClass unserialize(ByteBuf buf) {
    return unserialize_PacketAs_AbstractClass_Generic(buf);
  }

  void serialize_PacketAs_AbstractClass_Generic(PacketAs_AbstractClass value, ByteBuf acc) {
    if (false)  {
      return;
    } else if (value instanceof PacketAs_AbstractClass.Impl1) {
      acc.writeByte(0);
      serialize_Impl1_Concretic((PacketAs_AbstractClass.Impl1)value, acc);
    } else if (value instanceof PacketAs_AbstractClass.Impl2) {
      acc.writeByte(1);
      serialize_Impl2_Concretic((PacketAs_AbstractClass.Impl2)value, acc);
    } else {
      throw new IllegalStateException("Unexpected implementation of test.general.PacketAs_AbstractClass: "+value.getClass().getName());
    }
  }

  PacketAs_AbstractClass unserialize_PacketAs_AbstractClass_Generic(ByteBuf buf) {
    byte concreteIndex = buf.readByte();
    if (false)  {
      return null;
    } else if (concreteIndex == 0) {
      return unserialize_Impl1_Concretic(buf);
    } else if (concreteIndex == 1) {
      return unserialize_Impl2_Concretic(buf);
    } else {
      throw new IllegalStateException("Unexpected implementation of test.general.PacketAs_AbstractClass: concreteIndex = "+concreteIndex);
    }
  }

  void serialize_Impl1_Generic(PacketAs_AbstractClass.Impl1 value, ByteBuf acc) {
    serialize_Impl1_Concretic(value, acc);
  }

  PacketAs_AbstractClass.Impl1 unserialize_Impl1_Generic(ByteBuf buf) {
    return unserialize_Impl1_Concretic(buf);
  }

  void serialize_Impl1_Concretic(PacketAs_AbstractClass.Impl1 value, ByteBuf acc) {
  }

  PacketAs_AbstractClass.Impl1 unserialize_Impl1_Concretic(ByteBuf buf) {
    PacketAs_AbstractClass.Impl1 value = new PacketAs_AbstractClass.Impl1();
    return value;
  }

  void serialize_Impl2_Generic(PacketAs_AbstractClass.Impl2 value, ByteBuf acc) {
    serialize_Impl2_Concretic(value, acc);
  }

  PacketAs_AbstractClass.Impl2 unserialize_Impl2_Generic(ByteBuf buf) {
    return unserialize_Impl2_Concretic(buf);
  }

  void serialize_Impl2_Concretic(PacketAs_AbstractClass.Impl2 value, ByteBuf acc) {
  }

  PacketAs_AbstractClass.Impl2 unserialize_Impl2_Concretic(ByteBuf buf) {
    PacketAs_AbstractClass.Impl2 value = new PacketAs_AbstractClass.Impl2();
    return value;
  }
}
