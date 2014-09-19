package org.intellij.sonar.util;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class GuaveStreamUtil {

  private static final Charset DEFAULT_CHARSET = Charsets.UTF_8;

  private GuaveStreamUtil() {
  }


  /**
   * Copy and close stream
   */
  public static String toString(final InputStream inputStream) throws IOException {
    return toString(inputStream, DEFAULT_CHARSET);
  }

  /**
   * Copy and close stream
   */
  public static String toString(final InputStream inputStream, final Charset charset) throws IOException {
    return CharStreams.toString(CharStreams.newReaderSupplier(toInputSupplier(inputStream), charset));
  }

  private static InputSupplier<InputStream> toInputSupplier(final InputStream is) {
    return new InputSupplier<InputStream>() {
      @Override
      public InputStream getInput() {
        return is;
      }
    };
  }

  private static OutputSupplier<OutputStream> toOutputSupplier(final OutputStream os) {
    return new OutputSupplier<OutputStream>() {
      @Override
      public OutputStream getOutput() {
        return os;
      }
    };

  }
}
