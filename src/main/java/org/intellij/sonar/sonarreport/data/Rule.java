package org.intellij.sonar.sonarreport.data;

public class Rule {

    private String key;
    private String rule;
    private String repository;
    private String name;

    public Rule(String key, String rule, String repository, String name) {
        this.key = key;
        this.rule = rule;
        this.repository = repository;
        this.name = name;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rule rule1 = (Rule) o;

        if (key != null ? !key.equals(rule1.key) : rule1.key != null)
            return false;
        if (name != null ? !name.equals(rule1.name) : rule1.name != null)
            return false;
        if (repository != null ? !repository.equals(rule1.repository) : rule1.repository != null)
            return false;
        if (rule != null ? !rule.equals(rule1.rule) : rule1.rule != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (rule != null ? rule.hashCode() : 0);
        result = 31 * result + (repository != null ? repository.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "key='" + key + '\'' +
                ", rule='" + rule + '\'' +
                ", repository='" + repository + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
