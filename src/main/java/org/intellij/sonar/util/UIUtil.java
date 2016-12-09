package org.intellij.sonar.util;

import java.util.Optional;

import javax.swing.*;

public final class UIUtil {

  private UIUtil() {
  }

  public static Object makeObj(final String item) {
    return new Object() {
      public String toString() {
        return item;
      }
    };
  }

  public static void selectComboBoxItem(JComboBox jComboBox,String name) {
    Optional itemToSelect = Optional.empty();
    for (int i = 0;i < jComboBox.getItemCount();i++) {
      final Object item = jComboBox.getItemAt(i);
      if (name.equals(item.toString())) {
        itemToSelect = Optional.of(item);
      }
    }
    if (itemToSelect.isPresent())
      jComboBox.setSelectedItem(itemToSelect.get());
  }
}
