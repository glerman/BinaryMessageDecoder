package com.tectonic;

import com.tectonic.decode.VarIntDecoder;
import org.junit.Assert;
import org.junit.Test;

public class HelperTest {

  @Test
  public void testRequiredVarIntBytes() throws Exception {

    Assert.assertEquals(1, Helper.requiredVarIntBytes(0));
    Assert.assertEquals(1, Helper.requiredVarIntBytes(0b0000_1000));
    Assert.assertEquals(2, Helper.requiredVarIntBytes(0b1000_0100));
    Assert.assertEquals(2, Helper.requiredVarIntBytes(0b0000_0100_1100_0100));
    Assert.assertEquals(3, Helper.requiredVarIntBytes(0b0000_0100_0000_0100_1100_0100));
  }

  @Test
  public void testEncodeDecode() throws Exception {
    
    Assert.assertEquals(57, VarIntDecoder.decodeVarint(Helper.encodeVarInt(57), 0).value);
    Assert.assertEquals(4, VarIntDecoder.decodeVarint(Helper.encodeVarInt(4), 0).value);
    Assert.assertEquals(8, VarIntDecoder.decodeVarint(Helper.encodeVarInt(8), 0).value);
    Assert.assertEquals(64, VarIntDecoder.decodeVarint(Helper.encodeVarInt(64), 0).value);
    Assert.assertEquals(13, VarIntDecoder.decodeVarint(Helper.encodeVarInt(13), 0).value);
    Assert.assertEquals(17, VarIntDecoder.decodeVarint(Helper.encodeVarInt(17), 0).value);
    Assert.assertEquals(57, VarIntDecoder.decodeVarint(Helper.encodeVarInt(57), 0).value);
    Assert.assertEquals(22, VarIntDecoder.decodeVarint(Helper.encodeVarInt(22), 0).value);
    Assert.assertEquals(300, VarIntDecoder.decodeVarint(Helper.encodeVarInt(300), 0).value);
    Assert.assertEquals(1378263, VarIntDecoder.decodeVarint(Helper.encodeVarInt(1378263), 0).value);
  }

}
