package org.intellij.sonar.util;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressWrapper;
import org.jetbrains.annotations.Nullable;

public class ProgressIndicatorUtil {

  public static void setText(@Nullable ProgressIndicator progressIndicator,String message) {
    if (progressIndicator != null) {
      ProgressWrapper.unwrap(progressIndicator).setText(message);
    }
  }

  public static void setText2(@Nullable ProgressIndicator progressIndicator,String message) {
    if (progressIndicator != null) {
      ProgressWrapper.unwrap(progressIndicator).setText2(message);
    }
  }

  public static void setFraction(@Nullable ProgressIndicator progressIndicator,double fraction) {
    if (progressIndicator != null) {
      ProgressWrapper.unwrap(progressIndicator).setFraction(fraction);
    }
  }

  public static void setIndeterminate(@Nullable ProgressIndicator progressIndicator,boolean isIndeterminate) {
    if (progressIndicator != null) {
      ProgressWrapper.unwrap(progressIndicator).setIndeterminate(isIndeterminate);
    }
  }
}
