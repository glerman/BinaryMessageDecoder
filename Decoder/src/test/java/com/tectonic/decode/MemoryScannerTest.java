package com.tectonic.decode;

import com.google.common.collect.Lists;
import com.tectonic.input.Helper;
import com.tectonic.domain.Memory;
import com.tectonic.domain.Block;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MemoryScannerTest {

  @Test
  public void testTrivialBlock() throws Exception {
    byte[] rootBlock = Helper.encodeBlock(null, null, false);
    Memory memory = new MemoryScanner(rootBlock).scan();

    Assert.assertEquals(1, memory.getReachableBlocks().size());
    Assert.assertEquals(1, memory.root.length);
    Assert.assertEquals(0, memory.root.offset);
    Assert.assertNull(memory.root.payloadOffset);
    Assert.assertTrue(memory.root.getPointers().isEmpty());
  }

  @Test
  public void testTrivialBlockWithZeroByte() throws Exception {
    byte[] rootBlock = Helper.encodeBlock(null, null, true);
    Memory memory = new MemoryScanner(rootBlock).scan();

    Assert.assertEquals(1, memory.getReachableBlocks().size());
    Assert.assertEquals(2, memory.root.length);
    Assert.assertEquals(0, memory.root.offset);
    Assert.assertNull(memory.root.payloadOffset);
    Assert.assertTrue(memory.root.getPointers().isEmpty());
  }

  @Test
  public void testPayloadBock() throws Exception {
    byte[] payload = "hello".getBytes();
    byte[] rootBlock = Helper.encodeBlock(null, payload, false);
    Memory memory = new MemoryScanner(rootBlock).scan();

    Assert.assertEquals(memory.data, rootBlock);
    Assert.assertEquals(memory.root.length, rootBlock.length);
    Assert.assertEquals(Collections.emptyList(), memory.root.getPointers());
    Assert.assertEquals(2, memory.root.payloadOffset.intValue());

    byte[] actualPayload = Arrays.copyOfRange(rootBlock, memory.root.payloadOffset, rootBlock.length);
    Assert.assertTrue(Arrays.equals(payload, actualPayload));
  }

  @Test
  public void testTwoBlocksNoPayload() throws Exception {
    List<Integer> pointers = Lists.newArrayList(2);
    byte[] rootBlock = Helper.encodeBlock(pointers, null, false);
    byte[] secondBlock = Helper.encodeBlock(null, null, false);
    byte[] data = Helper.encodeMemory(Lists.newArrayList(rootBlock, secondBlock));

    Memory memory = new MemoryScanner(data).scan();

    Assert.assertEquals(memory.data.length, rootBlock.length + secondBlock.length);
    Assert.assertEquals(memory.root.length, rootBlock.length);
    Assert.assertEquals(pointers, memory.root.getPointerIntegers());
    Assert.assertEquals(2, memory.blockCount());

    Assert.assertEquals(2, memory.getReachableBlocks().size());
  }

  @Test
  public void testTwoBlocksWithPayload() throws Exception {
    byte[] payload = new byte[5];
    List<Integer> pointers = Lists.newArrayList(8);
    byte[] rootBlock = Helper.encodeBlock(pointers, payload, false);
    byte[] secondBlock = Helper.encodeBlock(pointers, payload, false);
    byte[] data = Helper.encodeMemory(Lists.newArrayList(rootBlock, secondBlock));

    Memory memory = new MemoryScanner(data).scan();

    Assert.assertEquals(memory.data.length, rootBlock.length + secondBlock.length);
    Assert.assertEquals(memory.root.length, rootBlock.length);
    Assert.assertEquals(pointers, memory.root.getPointerIntegers());
    Assert.assertEquals(2, memory.blockCount());

    Assert.assertEquals(2, memory.getReachableBlocks().size());

    Block actualSecondBlock = memory.getReachableBlocks().get(1);
    Assert.assertEquals(pointers, actualSecondBlock.getPointerIntegers());
    Assert.assertEquals(8, actualSecondBlock.offset);
  }

  @Test
  public void testMiddleBlockUnreachableWithPayloads() throws Exception {
    byte[] payload = new byte[5];
    List<Integer> pointers = Lists.newArrayList(9, 21);
    byte[] rootBlock = Helper.encodeBlock(pointers, payload, false);
    byte[] reachable1 = Helper.encodeBlock(null, payload, false);
    byte[] reachable2 = Helper.encodeBlock(null, payload, false);

    byte[] data = Helper.encodeMemory(Lists.newArrayList(rootBlock, reachable1, payload, reachable2));

    Memory memory = new MemoryScanner(data).scan();

    Assert.assertEquals(3, memory.getReachableBlocks().size());
    Assert.assertEquals(rootBlock.length, memory.getReachableBlocks().get(0).length);

    Block actualReachable1 = memory.getReachableBlocks().get(1);
    Assert.assertEquals(reachable1.length, actualReachable1.length);
    Assert.assertEquals(9, actualReachable1.offset);

    Block actualReachable2 = memory.getReachableBlocks().get(2);
    Assert.assertEquals(reachable2.length, actualReachable2.length);
    Assert.assertEquals(21, actualReachable2.offset);
  }

  @Test
  public void testDecodeMiddleMessage() throws Exception {

    String expectedMessage = "hello";
    List<Integer> pointers = Lists.newArrayList(3, 10);
    byte[] rootBlock = Helper.encodeBlock(pointers, null, false);
    byte[] reachable1 = Helper.encodeBlock(null, null, true);
    byte[] unreachable = expectedMessage.getBytes();
    byte[] reachable2 = Helper.encodeBlock(null, null, true);

    byte[] data = Helper.encodeMemory(Lists.newArrayList(rootBlock, reachable1, unreachable, reachable2));
    Memory memory = new MemoryScanner(data).scan();

    Assert.assertEquals(3, memory.getReachableBlocks().size());
    Assert.assertEquals(pointers, memory.root.getPointerIntegers());
    Assert.assertEquals(expectedMessage, memory.getMessage());
  }

  @Test
  public void testSelfReferencingBlock() throws Exception {

    String expectedMessage = "hello";
    byte[] rootBlock = Helper.encodeBlock(Lists.newArrayList(6, 15), new byte[2], false);
    byte[] reachable1 = Helper.encodeBlock(Lists.newArrayList(6, 15), null, true);
    byte[] unreachable = expectedMessage.getBytes();
    byte[] reachable2 = Helper.encodeBlock(Lists.newArrayList(6, 15, 15), null, false);

    byte[] data = Helper.encodeMemory(Lists.newArrayList(rootBlock, reachable1, unreachable, reachable2));

    Memory memory = new MemoryScanner(data).scan();
    Assert.assertEquals(19, memory.data.length);
    Assert.assertEquals(3, memory.getReachableBlocks().size());
    Assert.assertEquals(Lists.newArrayList(6, 15), memory.root.getPointerIntegers());
    Assert.assertEquals(Lists.newArrayList(6, 15), memory.getReachableBlocks().get(1).getPointerIntegers());
    Assert.assertEquals(Lists.newArrayList(6, 15, 15), memory.getReachableBlocks().get(2).getPointerIntegers());

    Assert.assertEquals(expectedMessage, memory.getMessage());
  }

  @Test
  public void testDecodeFragmentedMessage() throws Exception {

    List<Integer> pointers = Lists.newArrayList(4, 8, 11);
    byte[] rootBlock = Helper.encodeBlock(pointers, null, false);
    byte[] reachable1 = Helper.encodeBlock(null, null, true);
    byte[] unreachable1 = "he".getBytes();
    byte[] reachable2 = Helper.encodeBlock(null, null, true);
    byte[] unreachabl2 = "l".getBytes();
    byte[] reachable3 = Helper.encodeBlock(null, null, true);
    byte[] unreachabl3 = "lo".getBytes();

    byte[] data = Helper.encodeMemory(Lists.newArrayList(rootBlock, reachable1, unreachable1, reachable2, unreachabl2, reachable3, unreachabl3));
    Assert.assertEquals(15, data.length);

    Memory memory = new MemoryScanner(data).scan();

    Assert.assertEquals(4, memory.getReachableBlocks().size());
    Assert.assertEquals(pointers, memory.root.getPointerIntegers());
    Assert.assertEquals("hello", memory.getMessage());
  }

}
