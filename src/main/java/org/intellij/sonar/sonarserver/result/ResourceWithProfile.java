package org.intellij.sonar.sonarserver.result;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.joda.time.DateTime;

import java.util.List;

public class ResourceWithProfile {

    private final Integer id;
    private final String key;
    private final String name;
    private final String scope;
    private final String qualifier;
    private final DateTime date;
    private final DateTime creationDate;
    private final String lname;
    private final String lang;
    private final String version;
    private final String description;
    private final List<Msr> msr;

    public ResourceWithProfile(Integer id, String key, String name, String scope, String qualifier, DateTime date, DateTime creationDate, String lname, String lang, String version, String description, List<Msr> msr) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.scope = scope;
        this.qualifier = qualifier;
        this.date = date;
        this.creationDate = creationDate;
        this.lname = lname;
        this.lang = lang;
        this.version = version;
        this.description = description;
        this.msr = msr;
    }

    public static class Msr {
        private final String key;
        private final String val;
        @SerializedName("frmt_val")
        private final String frmtVal;
        private final String data;

        public Msr(String key, String val, String frmtVal, String data) {
            this.key = key;
            this.val = val;
            this.frmtVal = frmtVal;
            this.data = data;
        }

        /**
         * "key": "profile"
         *
         * @return "profile"
         */
        public String getKey() {
            return key;
        }

        /**
         * "val": 10
         *
         * @return "10"
         */
        public String getVal() {
            return val;
        }

        /**
         * "frmt_val": "10.0"
         *
         * @return "10.0"
         */
        public String getFrmtVal() {
            return frmtVal;
        }

        /**
         * "data": "AutoAct"
         *
         * @return "AutoAct"
         */
        public String getData() {
            return data;
        }
    }

    /**
     * "id": 41222
     *
     * @return 41222
     */
    public Integer getId() {
        return id;
    }

    /**
     * "key" : "autoact:autoact-b2b-api_groovy"
     *
     * @return "autoact:autoact-b2b-api_groovy"
     */
    public String getKey() {
        return key;
    }

    /**
     * "name": "autoact-b2b-api_groovy"
     *
     * @return "autoact-b2b-api_groovy"
     */
    public String getName() {
        return name;
    }

    /**
     * "scope": "PRJ"
     *
     * @return "PRJ"
     */
    public String getScope() {
        return scope;
    }

    /**
     * "qualifier": "BRC"
     *
     * @return "BRC"
     */
    public String getQualifier() {
        return qualifier;
    }

    /**
     * "date": "2014-04-08T14:36:39+0200"
     *
     * @return "2014-04-08T14:36:39+0200"
     */
    public DateTime getDate() {
        return date;
    }

    /**
     * "creationDate": "2013-11-07T11:27:49+0100"
     *
     * @return "2013-11-07T11:27:49+0100"
     */
    public DateTime getCreationDate() {
        return creationDate;
    }

    /**
     * "lname": "autoact-b2b-api_groovy"
     *
     * @return "autoact-b2b-api_groovy"
     */
    public String getLname() {
        return lname;
    }

    /**
     * "lang": "grvy"
     *
     * @return "grvy"
     */
    public String getLang() {
        return lang;
    }

    /**
     * "version": "master"
     *
     * @return "master"
     */
    public String getVersion() {
        return version;
    }

    /**
     * "description": ""
     *
     * @return ""
     */
    public String getDescription() {
        return description;
    }

    public List<Msr> getMsr() {
        return msr;
    }

    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
            .create();
}
