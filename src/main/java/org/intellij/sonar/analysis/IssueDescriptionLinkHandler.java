package org.intellij.sonar.analysis;

import com.intellij.codeInsight.highlighting.TooltipLinkHandler;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IssueDescriptionLinkHandler extends TooltipLinkHandler {

  @Nullable
  @Override
  public String getDescription(@NotNull String refSuffix, @NotNull Editor editor) {
    return "my desc "+refSuffix;
  }
}
