package com.tectonic.input;

import org.junit.Assert;
import org.junit.Test;

public class VarIntEncoderTest {

  @Test
  public void testRequiredVarIntBytes() throws Exception {

    System.out.println((byte)0b0000_1000);
    Assert.assertEquals(1, VarIntEncoder.requiredVarIntBytes(0));
    Assert.assertEquals(1, VarIntEncoder.requiredVarIntBytes(0b0000_1000));
    Assert.assertEquals(2, VarIntEncoder.requiredVarIntBytes(0b1000_0100));
    Assert.assertEquals(2, VarIntEncoder.requiredVarIntBytes(0b0000_0100_1100_0100));
    Assert.assertEquals(3, VarIntEncoder.requiredVarIntBytes(0b0000_0100_0000_0100_1100_0100));
  }

  @Test
  public void testEncodeDecode() throws Exception {
    Assert.assertEquals(8, new MemoryScanner(VarIntEncoder.encodeVarInt(8)).decodeVarint(0).value);
    Assert.assertEquals(64, new MemoryScanner(VarIntEncoder.encodeVarInt(64)).decodeVarint(0).value);
    Assert.assertEquals(300, new MemoryScanner(VarIntEncoder.encodeVarInt(300)).decodeVarint(0).value);
    Assert.assertEquals(1378263, new MemoryScanner(VarIntEncoder.encodeVarInt(1378263)).decodeVarint(0).value);
  }

}
