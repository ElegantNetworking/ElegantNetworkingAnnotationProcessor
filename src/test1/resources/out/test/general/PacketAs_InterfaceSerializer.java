package test.general;

import hohserg.elegant.networking.impl.ISerializer;
import hohserg.elegant.networking.impl.SerializerMark;
import io.netty.buffer.ByteBuf;

@SerializerMark(
    packetClass = test.general.PacketAs_Interface.class
)
public class PacketAs_InterfaceSerializer implements ISerializer<PacketAs_Interface> {
  public void serialize(PacketAs_Interface value, ByteBuf acc) {
    serialize_PacketAs_Interface_Generic(value, acc);
  }

  public PacketAs_Interface unserialize(ByteBuf buf) {
    return unserialize_PacketAs_Interface_Generic(buf);
  }

  void serialize_PacketAs_Interface_Generic(PacketAs_Interface value, ByteBuf acc) {
    if (false)  {
      return;
    } else if (value instanceof PacketAs_Interface.Impl1) {
      acc.writeByte(0);
      serialize_Impl1_Concretic((PacketAs_Interface.Impl1)value, acc);
    } else if (value instanceof PacketAs_Interface.Impl2) {
      acc.writeByte(1);
      serialize_Impl2_Concretic((PacketAs_Interface.Impl2)value, acc);
    } else {
      throw new IllegalStateException("Unexpected implementation of test.general.PacketAs_Interface: "+value.getClass().getName());
    }
  }

  PacketAs_Interface unserialize_PacketAs_Interface_Generic(ByteBuf buf) {
    byte concreteIndex = buf.readByte();
    if (false)  {
      return null;
    } else if (concreteIndex == 0) {
      return unserialize_Impl1_Concretic(buf);
    } else if (concreteIndex == 1) {
      return unserialize_Impl2_Concretic(buf);
    } else {
      throw new IllegalStateException("Unexpected implementation of test.general.PacketAs_Interface: concreteIndex = "+concreteIndex);
    }
  }

  void serialize_Impl1_Generic(PacketAs_Interface.Impl1 value, ByteBuf acc) {
    serialize_Impl1_Concretic(value, acc);
  }

  PacketAs_Interface.Impl1 unserialize_Impl1_Generic(ByteBuf buf) {
    return unserialize_Impl1_Concretic(buf);
  }

  void serialize_Impl1_Concretic(PacketAs_Interface.Impl1 value, ByteBuf acc) {
  }

  PacketAs_Interface.Impl1 unserialize_Impl1_Concretic(ByteBuf buf) {
    PacketAs_Interface.Impl1 value = new PacketAs_Interface.Impl1();
    return value;
  }

  void serialize_Impl2_Generic(PacketAs_Interface.Impl2 value, ByteBuf acc) {
    serialize_Impl2_Concretic(value, acc);
  }

  PacketAs_Interface.Impl2 unserialize_Impl2_Generic(ByteBuf buf) {
    return unserialize_Impl2_Concretic(buf);
  }

  void serialize_Impl2_Concretic(PacketAs_Interface.Impl2 value, ByteBuf acc) {
  }

  PacketAs_Interface.Impl2 unserialize_Impl2_Concretic(ByteBuf buf) {
    PacketAs_Interface.Impl2 value = new PacketAs_Interface.Impl2();
    return value;
  }
}
