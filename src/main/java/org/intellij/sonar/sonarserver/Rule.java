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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Rule rule = (Rule) o;
    return Objects.equal(key, rule.key) &&
            Objects.equal(name, rule.name) &&
            Objects.equal(severity, rule.severity) &&
            Objects.equal(lan, rule.lan) &&
            Objects.equal(langName, rule.langName) &&
            Objects.equal(htmlDesc, rule.htmlDesc);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(key, name, severity, lan, langName, htmlDesc);
  }

  @Override
  public int compareTo(Object that) {
    return Objects.equal(this,that)
      ? 0
      : -1;
  }
}
