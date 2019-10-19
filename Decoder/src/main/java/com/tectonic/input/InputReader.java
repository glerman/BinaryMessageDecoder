package com.tectonic.input;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class InputReader {

  private String filePath;

  public InputReader(final String filePath) {
    this.filePath = filePath;
  }

  public byte[] read() throws IOException {

    Path path = FileSystems.getDefault().getPath(filePath);
    System.out.println("Reading file: " + path.toString());

    byte[] bytes = Files.readAllBytes(path);

    System.out.println("Bytes in file: " + bytes.length);
    return bytes;
  }

}
