package com.tectonic;

import com.tectonic.domain.Memory;
import com.tectonic.input.InputReader;
import com.tectonic.input.MemoryScanner;

import java.io.IOException;

public class Decode {

  public static void main(String[] args) throws IOException {

    String file = Decode.class.getClassLoader().getResource(args[0]).getFile();
    byte[] data = new InputReader(file).read();
    MemoryScanner memoryScanner = new MemoryScanner(data);
    Memory memory = memoryScanner.scan();

    System.out.println("And the message is:");
    System.out.println(memory.getMessage());
  }
}
