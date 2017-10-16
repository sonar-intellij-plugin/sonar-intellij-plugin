package org.intellij.sonar.sonarserver;

import com.google.common.base.Objects;
import com.intellij.util.xmlb.annotations.Transient;
import org.apache.commons.lang.StringUtils;

public class Rule implements Comparable {

  private String key; // squid:S106
  private String name; // System.out and System.err should not be used as loggers
  private String severity; // MAJOR
  private String lan; // java
  private String langName; // Java
  private String htmlDesc; // <p>very long explanation of the rule in html</p>

  public Rule() {
  }

  public Rule(String key,String name,String severity,String lan,String langName,String htmlDesc) {
    this.key = key;
    this.name = name;
    this.severity = severity;
    this.lan = lan;
    this.langName = langName;
    this.htmlDesc = htmlDesc;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getLan() {
    return lan;
  }

  public void setLan(String lan) {
    this.lan = lan;
  }

  public String getLangName() {
    return langName;
  }

  public void setLangName(String langName) {
    this.langName = langName;
  }

  public String getHtmlDesc() {
    return htmlDesc;
  }

  public void setHtmlDesc(String htmlDesc) {
    this.htmlDesc = htmlDesc;
  }

  @Transient
  public boolean isEmpty() {
    return StringUtils.isEmpty(key) || StringUtils.isEmpty(htmlDesc);
  }

  @SuppressWarnings("RedundantIfStatement")
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Rule rule = (Rule) o;
    if (htmlDesc != null
      ? !htmlDesc.equals(rule.htmlDesc)
      : rule.htmlDesc != null)
      return false;
    if (key != null
      ? !key.equals(rule.key)
      : rule.key != null)
      return false;
    if (lan != null
      ? !lan.equals(rule.lan)
      : rule.lan != null)
      return false;
    if (langName != null
      ? !langName.equals(rule.langName)
      : rule.langName != null)
      return false;
    if (name != null
      ? !name.equals(rule.name)
      : rule.name != null)
      return false;
    if (severity != null
      ? !severity.equals(rule.severity)
      : rule.severity != null)
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = key != null
      ? key.hashCode()
      : 0;
    result = 31 * result+(name != null
      ? name.hashCode()
      : 0);
    result = 31 * result+(severity != null
      ? severity.hashCode()
      : 0);
    result = 31 * result+(lan != null
      ? lan.hashCode()
      : 0);
    result = 31 * result+(langName != null
      ? langName.hashCode()
      : 0);
    result = 31 * result+(htmlDesc != null
      ? htmlDesc.hashCode()
      : 0);
    return result;
  }

  @Override
  public int compareTo(Object that) {
    return Objects.equal(this,that)
      ? 0
      : -1;
  }
}
