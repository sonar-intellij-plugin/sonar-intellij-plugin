package org.intellij.sonar.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DurationUtil {

  /**
   Convert a millisecond duration to a string format

   @param durationInMillis
   A duration to convert to a string form

   @return A string of the form "25:36:259".
   */
  public static String getDurationBreakdown(long durationInMillis) {
    if (durationInMillis < 0) {
      throw new IllegalArgumentException("Duration must be greater than zero!");
    }
    return (new SimpleDateFormat("mm:ss:SSS")).format(new Date(durationInMillis));
  }
}
