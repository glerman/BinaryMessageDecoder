package com.tectonic.input;

import com.tectonic.domain.VarInt;
import org.junit.Assert;
import org.junit.Test;

public class MemoryScannerTest {

  @Test
  public void testDecodeVarInt() throws Exception {
    byte[] bytes = {(byte)0B10101100, (byte)0B00000010};
    MemoryScanner memoryScanner = new MemoryScanner(bytes);
    VarInt varint = memoryScanner.decodeVarint(0);

    Assert.assertEquals(300, varint.value);
    Assert.assertEquals(2, varint.length);
  }
}
