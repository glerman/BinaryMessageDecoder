package com.tectonic.decode;

import com.tectonic.domain.VarInt;

import java.util.ArrayList;
import java.util.List;

public class VarIntDecoder {

  public static VarInt decodeVarint(final byte[] data, final int offset) {
    //find varint bytes
    byte moreMask = (byte) 0B1000_0000;
    byte varIntValueMask = (byte) 0B0111_1111;
    List<Byte> varIntValueBytes = new ArrayList<>();
    boolean hasMore = true;
    int currIdx = 0;
    while (hasMore) {
      byte currByte = data[offset + currIdx];
      hasMore = (currByte & moreMask) == moreMask;
      varIntValueBytes.add((byte)(currByte & varIntValueMask));
      currIdx++;
    }
    assert varIntValueBytes.size() > 0 && varIntValueBytes.size() <= 8 : "A varint has between 0 and 8 bytes";
    assert currIdx == varIntValueBytes.size();
    //extract number from bytes
    int value = 0;
    for (int i = 0; i < varIntValueBytes.size(); i++) {
      byte byteVal = varIntValueBytes.get(i);
      assert byteVal >=0;

      long shifted = ((long) byteVal) << 7 * i;
      assert shifted >=0;

      value += shifted;
    }
    return new VarInt(value, varIntValueBytes.size());
  }
}
