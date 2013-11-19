package org.intellij.sonar.util;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
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
   * Copy and close all streams
   */
  public static void copy(final InputStream inputStream, final OutputStream outputStream) throws IOException {
    ByteStreams.copy(toInputSupplier(inputStream), toOutputSupplier(outputStream));
  }

  /**
   * Copy and close stream
   */
  public static void toStream(final CharSequence from, final OutputStream outputStream) throws IOException {
    toStream(from, outputStream, DEFAULT_CHARSET);
  }

  /**
   * Copy and close stream
   */
  public static void toStream(final CharSequence from, final OutputStream outputStream, final Charset charset) throws IOException {
    CharStreams.write(from, CharStreams.newWriterSupplier(toOutputSupplier(outputStream), charset));
  }

  /**
   * Copy and close stream
   */
  public static void toStream(final byte[] from, final OutputStream outputStream) throws IOException {
    ByteStreams.write(from, toOutputSupplier(outputStream));
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
