package com.tectonic.decode;

import com.tectonic.domain.Block;
import com.tectonic.input.InputReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Decoder {
  public static void main(String[] args) throws IOException {

    String file = Decoder.class.getClassLoader().getResource("hello.bin").getFile();
    byte[] data = new InputReader().read(file);

  }

  public List<Block> findUnreachableBlocks(final Block root) {
    final List<Block> reachables = findReachableBlocks(root);
    Collections.sort(reachables);

    List<Block> unreachables = new ArrayList<>();
    Block curr;
    Block next;
    for(int i = 0; i < reachables.size() - 1; i++) {
      curr = reachables.get(i);
      next = reachables.get(i + 1);
      if (curr.getOffset() + curr.getLength() < next.getOffset()) {
        //there are unreachable block(s) in between
        //add them to unreachable
      }
    }
    throw new UnsupportedOperationException();
  }

  private List<Block> findReachableBlocks(final Block root) {
    throw new UnsupportedOperationException();
  }
}
