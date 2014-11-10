package org.intellij.sonar.sonarserver.result;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Profile {

    private final String name;
    private final String language;
    @SerializedName("default")
    private final Boolean isDefaultProfile;
    private final List<Rule> rules;

    public Profile(String name, String language, Boolean isDefaultProfile, List<Rule> rules) {
        this.name = name;
        this.language = language;
        this.isDefaultProfile = isDefaultProfile;
        this.rules = rules;
    }

    /**
     * "name": "mobile_relaxed"
     *
     * @return "mobile_relaxed"
     */
    public String getName() {
        return name;
    }

    /**
     * "language": "java"
     *
     * @return "java"
     */
    public String getLanguage() {
        return language;
    }

    /**
     * "default": false
     *
     * @return false
     */
    public Boolean getIsDefaultProfile() {
        return isDefaultProfile;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public static final Gson gson = new Gson();
}
