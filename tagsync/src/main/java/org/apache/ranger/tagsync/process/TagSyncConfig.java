/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ranger.tagsync.process;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.SecureClientLogin;
import org.apache.ranger.credentialapi.CredentialReader;
import org.apache.ranger.plugin.util.RangerCommonConstants;
import org.apache.ranger.tagsync.ha.TagSyncHAInitializerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;

public class TagSyncConfig extends Configuration {
    private static final Logger LOG = LoggerFactory.getLogger(TagSyncConfig.class);

    public static final String TAGSYNC_ENABLED_PROP = "ranger.tagsync.enabled";
    public static final String TAGSYNC_LOGDIR_PROP = "ranger.tagsync.logdir";
    public static final String TAGSYNC_FILESOURCE_FILENAME_PROP = "ranger.tagsync.source.file.filename";
    public static final String TAGSYNC_RANGER_COOKIE_ENABLED_PROP = "ranger.tagsync.cookie.enabled";
    public static final String TAGSYNC_TAGADMIN_COOKIE_NAME_PROP  = "ranger.tagsync.dest.ranger.session.cookie.name";
    public static final  int  DEFAULT_TAGSYNC_ATLASREST_SOURCE_ENTITIES_BATCH_SIZE = 10000;
    public static final String TAGSYNC_KERBEROS_IDENTITY = "tagsync.kerberos.identity";
    public static final  String TAGSYNC_SERVER_HA_ENABLED_PARAM              = "ranger-tagsync.server.ha.enabled";
    private static final String        CONFIG_FILE = "ranger-tagsync-site.xml";
    private static final String DEFAULT_CONFIG_FILE = "ranger-tagsync-default.xml";
    private static final String CORE_SITE_FILE = "core-site.xml";
    private static final String TAGSYNC_TAGADMIN_REST_URL_PROP = "ranger.tagsync.dest.ranger.endpoint";
    private static final String TAGSYNC_TAGADMIN_REST_SSL_CONFIG_FILE_PROP = "ranger.tagsync.dest.ranger.ssl.config.filename";
    private static final String TAGSYNC_SINK_CLASS_PROP = "ranger.tagsync.dest.ranger.impl.class";
    private static final String TAGSYNC_DEST_RANGER_PASSWORD_ALIAS      = "tagadmin.user.password";
    private static final String TAGSYNC_SOURCE_ATLASREST_PASSWORD_ALIAS = "atlas.user.password";
    private static final String TAGSYNC_TAGADMIN_USERNAME_PROP  = "ranger.tagsync.dest.ranger.username";
    private static final String TAGSYNC_ATLASREST_USERNAME_PROP = "ranger.tagsync.source.atlasrest.username";
    private static final String TAGSYNC_TAGADMIN_PASSWORD_PROP  = "ranger.tagsync.dest.ranger.password";
    private static final String TAGSYNC_ATLASREST_PASSWORD_PROP = "ranger.tagsync.source.atlasrest.password";
    private static final String TAGSYNC_TAGADMIN_CONNECTION_CHECK_INTERVAL_PROP = "ranger.tagsync.dest.ranger.connection.check.interval";
    private static final String TAGSYNC_SOURCE_ATLAS_CUSTOM_RESOURCE_MAPPERS_PROP = "ranger.tagsync.atlas.custom.resource.mappers";
    private static final String TAGSYNC_ATLASSOURCE_ENDPOINT_PROP = "ranger.tagsync.source.atlasrest.endpoint";
    private static final String TAGSYNC_ATLAS_REST_SOURCE_DOWNLOAD_INTERVAL_PROP = "ranger.tagsync.source.atlasrest.download.interval.millis";
    private static final String TAGSYNC_ATLAS_REST_SSL_CONFIG_FILE_PROP = "ranger.tagsync.source.atlasrest.ssl.config.filename";
    private static final String TAGSYNC_FILESOURCE_MOD_TIME_CHECK_INTERVAL_PROP = "ranger.tagsync.source.file.check.interval.millis";
    private static final String TAGSYNC_KEYSTORE_TYPE_PROP      = "ranger.keystore.file.type";
    private static final String TAGSYNC_TAGADMIN_KEYSTORE_PROP  = "ranger.tagsync.keystore.filename";
    private static final String TAGSYNC_ATLASREST_KEYSTORE_PROP = "ranger.tagsync.source.atlasrest.keystore.filename";
    private static final String TAGSYNC_SOURCE_RETRY_INITIALIZATION_INTERVAL_PROP = "ranger.tagsync.source.retry.initialization.interval.millis";
    private static final String DEFAULT_TAGADMIN_USERNAME  = "rangertagsync";
    private static final String DEFAULT_ATLASREST_USERNAME = "admin";
    private static final String DEFAULT_ATLASREST_PASSWORD = "admin";
    private static final int  DEFAULT_TAGSYNC_TAGADMIN_CONNECTION_CHECK_INTERVAL   = 15000;
    private static final long DEFAULT_TAGSYNC_ATLASREST_SOURCE_DOWNLOAD_INTERVAL   = 900000;
    private static final long DEFAULT_TAGSYNC_FILESOURCE_MOD_TIME_CHECK_INTERVAL   = 60000;
    private static final long DEFAULT_TAGSYNC_SOURCE_RETRY_INITIALIZATION_INTERVAL = 10000;
    private static final String AUTH_TYPE                 = "hadoop.security.authentication";
    private static final String NAME_RULES                = "hadoop.security.auth_to_local";
    private static final String TAGSYNC_KERBEROS_PRICIPAL = "ranger.tagsync.kerberos.principal";
    private static final String TAGSYNC_KERBEROS_KEYTAB   = "ranger.tagsync.kerberos.keytab";
    private static final String TAGSYNC_METRICS_FILEPATH                          = "ranger.tagsync.metrics.filepath";
    private static final String DEFAULT_TAGSYNC_METRICS_FILEPATH                  = "/tmp/";
    private static final String TAGSYNC_METRICS_FILENAME                          = "ranger.tagsync.metrics.filename";
    private static final String DEFAULT_TAGSYNC_METRICS_FILENAME                  = "ranger_tagsync_metric.json";
    private static final String TAGSYNC_METRICS_FREQUENCY_TIME_IN_MILLIS_PARAM    = "ranger.tagsync.metrics.frequencytimeinmillis";
    private static final long   DEFAULT_TAGSYNC_METRICS_FREQUENCY__TIME_IN_MILLIS = 10000L;
    private static final String TAGSYNC_METRICS_ENABLED_PROP                      = "ranger.tagsync.metrics.enabled";
    private static final int    DEFAULT_TAGSYNC_SINK_MAX_BATCH_SIZE = 1;
    private static final String TAGSYNC_SINK_MAX_BATCH_SIZE_PROP    = "ranger.tagsync.dest.ranger.max.batch.size";
    private static final String TAGSYNC_ATLASREST_SOURCE_ENTITIES_BATCH_SIZE = "ranger.tagsync.source.atlasrest.entities.batch.size";

    private static TagSyncConfig instance;
    private static String        localHostname;

    // openmetadata env variables
    private static final String TAGSYNC_OPENMETADATASOURCE_ENDPOINT_PROP = "ranger.tagsync.source.openmetadatarest.endpoint";
    public static final long DEFAULT_TAGSYNC_OPENMETADATAREST_SOURCE_DOWNLOAD_INTERVAL = 230000;
    public  static final int  DEFAULT_TAGSYNC_OPENMETADATAREST_SOURCE_ENTITIES_BATCH_SIZE = 10000;
    private static final String TAGSYNC_OPENMETADATAREST_SOURCE_DOWNLOAD_INTERVAL = "ranger.tagsync.source.openmetadatarest.download.interval.millis";
    private static final String TAGSYNC_OPENMETADATAREST_SOURCE_ENTITIES_BATCH_SIZE = "ranger.tagsync.source.openmetadatarest.entities.batch.size";
    private static final String TAGSYNC_OPENMETADATAREST_TOKEN_PROP = "ranger.tagsync.source.openmetadatarest.token";
    public static final String TAGSYNC_SOURCE_OPENMETADATA_CUSTOM_RESOURCE_MAPPERS_PROP = "ranger.tagsync.openmetadatarest.custom.resource.mappers";
    private static final String TAGSYNC_OPENMETADATA_REST_SSL_CONFIG_FILENAME = "ranger.tagsync.source.openmetadatarest.ssl.config.filename";
    private static final String TAGSYNC_OPENMETADATAREST_KEYSTORE_PROP = "ranger.tagsync.source.openmetadatarest.keystore.filename";
    private static final String RANGER_OPENMETADATA_TABLE_COMPONENT_NAME = "ranger.tagsync.source.openmetadatarest.component.tabletype";
    public static final String DEFAULT_RANGER_OPENMETADATA_TABLE_COMPONENT_NAME = "trino";

    private Properties                 props;

    private TagSyncConfig() {
        super(false);

        init();
    }

    public static TagSyncConfig getInstance() {
        if (instance == null) {
            synchronized (TagSyncConfig.class) {
                if (instance == null) {
                    instance = new TagSyncConfig();
                }
            }
        }

        return instance;
    }

    public static InputStream getFileInputStream(String path) throws FileNotFoundException {
        InputStream ret;

        File f = new File(path);

        if (f.exists() && f.isFile() && f.canRead()) {
            ret = new FileInputStream(f);
        } else {
            ret = TagSyncConfig.class.getResourceAsStream(path);

            if (ret == null) {
                if (!path.startsWith("/")) {
                    ret = TagSyncConfig.class.getResourceAsStream("/" + path);
                }
            }

            if (ret == null) {
                ret = ClassLoader.getSystemClassLoader().getResourceAsStream(path);

                if (ret == null) {
                    if (!path.startsWith("/")) {
                        ret = ClassLoader.getSystemResourceAsStream("/" + path);
                    }
                }
            }
        }

        return ret;
    }

    public static String getResourceFileName(String path) {
        String ret = null;

        if (StringUtils.isNotBlank(path)) {
            File f = new File(path);

            if (f.exists() && f.isFile() && f.canRead()) {
                ret = path;
            } else {
                URL fileURL = TagSyncConfig.class.getResource(path);

                if (fileURL == null) {
                    if (!path.startsWith("/")) {
                        fileURL = TagSyncConfig.class.getResource("/" + path);
                    }
                }

                if (fileURL == null) {
                    fileURL = ClassLoader.getSystemClassLoader().getResource(path);

                    if (fileURL == null) {
                        if (!path.startsWith("/")) {
                            fileURL = ClassLoader.getSystemClassLoader().getResource("/" + path);
                        }
                    }
                }

                if (fileURL != null) {
                    try {
                        ret = fileURL.getFile();
                    } catch (Exception exception) {
                        LOG.error("{} is not a file", path, exception);
                    }
                } else {
                    LOG.warn("URL not found for {} or no privilege for reading file {}", path, path);
                }
            }
        }

        return ret;
    }

    public static synchronized boolean isTagSyncServiceActive() {
        return TagSyncHAInitializerImpl.getInstance(TagSyncConfig.getInstance()).isActive();
    }

    public static String getTagsyncKeyStoreType(Properties prop) {
        return prop.getProperty(TAGSYNC_KEYSTORE_TYPE_PROP);
    }

    public static boolean isTagSyncRangerCookieEnabled(Properties prop) {
        String val = prop.getProperty(TAGSYNC_RANGER_COOKIE_ENABLED_PROP);

        return val == null || Boolean.parseBoolean(val.trim());
    }

    public static String getRangerAdminCookieName(Properties prop) {
        String ret = RangerCommonConstants.DEFAULT_COOKIE_NAME;
        String val = prop.getProperty(TAGSYNC_TAGADMIN_COOKIE_NAME_PROP);

        if (StringUtils.isNotBlank(val)) {
            ret = val;
        }

        return ret;
    }

    public static String getTagSyncLogdir(Properties prop) {
        return prop.getProperty(TAGSYNC_LOGDIR_PROP);
    }

    public static long getTagSourceFileModTimeCheckIntervalInMillis(Properties prop) {
        String val = prop.getProperty(TAGSYNC_FILESOURCE_MOD_TIME_CHECK_INTERVAL_PROP);
        long   ret = DEFAULT_TAGSYNC_FILESOURCE_MOD_TIME_CHECK_INTERVAL;

        if (StringUtils.isNotBlank(val)) {
            try {
                ret = Long.parseLong(val);
            } catch (NumberFormatException exception) {
                // Ignore
            }
        }

        return ret;
    }

    public static long getTagSourceAtlasDownloadIntervalInMillis(Properties prop) {
        String val = prop.getProperty(TAGSYNC_ATLAS_REST_SOURCE_DOWNLOAD_INTERVAL_PROP);
        long   ret = DEFAULT_TAGSYNC_ATLASREST_SOURCE_DOWNLOAD_INTERVAL;

        if (StringUtils.isNotBlank(val)) {
            try {
                ret = Long.parseLong(val);
            } catch (NumberFormatException exception) {
                // Ignore
            }
        }

        return ret;
    }

    public static String getTagSinkClassName(Properties prop) {
        String val = prop.getProperty(TAGSYNC_SINK_CLASS_PROP);

        if (StringUtils.equalsIgnoreCase(val, "ranger")) {
            return "org.apache.ranger.tagsync.sink.tagadmin.TagAdminRESTSink";
        } else {
            return val;
        }
    }

    public static String getTagAdminRESTUrl(Properties prop) {
        return prop.getProperty(TAGSYNC_TAGADMIN_REST_URL_PROP);
    }

    public static boolean isTagSyncEnabled(Properties prop) {
        String val = prop.getProperty(TAGSYNC_ENABLED_PROP);

        return val == null || Boolean.parseBoolean(val.trim());
    }

    public static String getTagAdminPassword(Properties prop) {
        //update credential from keystore
        String password;

        if (prop != null && prop.containsKey(TAGSYNC_TAGADMIN_PASSWORD_PROP)) {
            password = prop.getProperty(TAGSYNC_TAGADMIN_PASSWORD_PROP);

            if (password != null && !password.isEmpty()) {
                return password;
            }
        }

        if (prop != null && prop.containsKey(TAGSYNC_TAGADMIN_KEYSTORE_PROP)) {
            String path = prop.getProperty(TAGSYNC_TAGADMIN_KEYSTORE_PROP);

            if (path != null) {
                if (!path.trim().isEmpty()) {
                    try {
                        password = CredentialReader.getDecryptedString(path.trim(), TAGSYNC_DEST_RANGER_PASSWORD_ALIAS, getTagsyncKeyStoreType(prop));
                    } catch (Exception ex) {
                        password = null;
                    }
                    if (password != null && !password.trim().isEmpty() && !password.trim().equalsIgnoreCase("none")) {
                        return password;
                    }
                }
            }
        }

        return null;
    }

    public static String getTagAdminUserName(Properties prop) {
        String userName = null;

        if (prop != null && prop.containsKey(TAGSYNC_TAGADMIN_USERNAME_PROP)) {
            userName = prop.getProperty(TAGSYNC_TAGADMIN_USERNAME_PROP);
        }

        if (StringUtils.isBlank(userName)) {
            userName = DEFAULT_TAGADMIN_USERNAME;
        }

        return userName;
    }

    public static String getTagAdminRESTSslConfigFile(Properties prop) {
        return prop.getProperty(TAGSYNC_TAGADMIN_REST_SSL_CONFIG_FILE_PROP);
    }

    public static String getTagSourceFileName(Properties prop) {
        return prop.getProperty(TAGSYNC_FILESOURCE_FILENAME_PROP);
    }

    public static String getAtlasRESTEndpoint(Properties prop) {
        return prop.getProperty(TAGSYNC_ATLASSOURCE_ENDPOINT_PROP);
    }

    public static String getAtlasRESTPassword(Properties prop) {
        //update credential from keystore
        String password = null;

        if (prop != null && prop.containsKey(TAGSYNC_ATLASREST_PASSWORD_PROP)) {
            password = prop.getProperty(TAGSYNC_ATLASREST_PASSWORD_PROP);

            if (password != null && !password.isEmpty()) {
                return password;
            }
        }

        if (prop != null && prop.containsKey(TAGSYNC_ATLASREST_KEYSTORE_PROP)) {
            String path = prop.getProperty(TAGSYNC_ATLASREST_KEYSTORE_PROP);

            if (path != null) {
                if (!path.trim().isEmpty()) {
                    try {
                        password = CredentialReader.getDecryptedString(path.trim(), TAGSYNC_SOURCE_ATLASREST_PASSWORD_ALIAS, getTagsyncKeyStoreType(prop));
                    } catch (Exception ex) {
                        password = null;
                    }

                    if (password != null && !password.trim().isEmpty() && !password.trim().equalsIgnoreCase("none")) {
                        return password;
                    }
                }
            }
        }

        if (StringUtils.isBlank(password)) {
            return DEFAULT_ATLASREST_PASSWORD;
        }

        return null;
    }

    public static String getAtlasRESTUserName(Properties prop) {
        String userName = null;

        if (prop != null && prop.containsKey(TAGSYNC_ATLASREST_USERNAME_PROP)) {
            userName = prop.getProperty(TAGSYNC_ATLASREST_USERNAME_PROP);
        }

        if (StringUtils.isBlank(userName)) {
            userName = DEFAULT_ATLASREST_USERNAME;
        }

        return userName;
    }

    public static String getAtlasRESTSslConfigFile(Properties prop) {
        return prop.getProperty(TAGSYNC_ATLAS_REST_SSL_CONFIG_FILE_PROP);
    }

    public static String getCustomAtlasResourceMappers(Properties prop) {
        return prop.getProperty(TAGSYNC_SOURCE_ATLAS_CUSTOM_RESOURCE_MAPPERS_PROP);
    }

    public static String getAuthenticationType(Properties prop) {
        return prop.getProperty(AUTH_TYPE, "simple");
    }

    public static String getNameRules(Properties prop) {
        return prop.getProperty(NAME_RULES, "DEFAULT");
    }

    public static String getKerberosPrincipal(Properties prop) {
// return prop.getProperty(TAGSYNC_KERBEROS_PRICIPAL);
        String principal = null;
        try {
            principal = SecureClientLogin.getPrincipal(prop.getProperty(TAGSYNC_KERBEROS_PRICIPAL, ""), localHostname);
        } catch (IOException ignored) {
            // do nothing
        }

        return principal;
    }

    public static String getKerberosKeytab(Properties prop) {
        return prop.getProperty(TAGSYNC_KERBEROS_KEYTAB, "");
    }

    public static long getTagAdminConnectionCheckInterval(Properties prop) {
        long   ret = DEFAULT_TAGSYNC_TAGADMIN_CONNECTION_CHECK_INTERVAL;
        String val = prop.getProperty(TAGSYNC_TAGADMIN_CONNECTION_CHECK_INTERVAL_PROP);

        if (StringUtils.isNotBlank(val)) {
            try {
                ret = Long.parseLong(val);
            } catch (NumberFormatException exception) {
                // Ignore
            }
        }

        return ret;
    }

    public static long getTagSourceRetryInitializationInterval(Properties prop) {
        long   ret = DEFAULT_TAGSYNC_SOURCE_RETRY_INITIALIZATION_INTERVAL;
        String val = prop.getProperty(TAGSYNC_SOURCE_RETRY_INITIALIZATION_INTERVAL_PROP);

        if (StringUtils.isNotBlank(val)) {
            try {
                ret = Long.parseLong(val);
            } catch (NumberFormatException exception) {
                // Ignore
            }
        }

        return ret;
    }

    public static String getTagsyncKerberosIdentity(Properties prop) {
        return prop.getProperty(TAGSYNC_KERBEROS_IDENTITY);
    }

    public static int getSinkMaxBatchSize(Properties prop) {
        int   ret              = DEFAULT_TAGSYNC_SINK_MAX_BATCH_SIZE;
        String maxBatchSizeStr = prop.getProperty(TAGSYNC_SINK_MAX_BATCH_SIZE_PROP);

        if (StringUtils.isNotEmpty(maxBatchSizeStr)) {
            try {
                ret = Integer.parseInt(maxBatchSizeStr);
            } catch (Exception ignored) {
            }
        }

        return ret;
    }

    public static boolean isTagSyncMetricsEnabled(Properties prop) {
        String val = prop.getProperty(TAGSYNC_METRICS_ENABLED_PROP);

        return "true".equalsIgnoreCase(StringUtils.trimToEmpty(val));
    }

    public static int getAtlasRestSourceEntitiesBatchSize(Properties prop) {
        String val = prop.getProperty(TAGSYNC_ATLASREST_SOURCE_ENTITIES_BATCH_SIZE);
        int    ret = DEFAULT_TAGSYNC_ATLASREST_SOURCE_ENTITIES_BATCH_SIZE;

        if (StringUtils.isNotBlank(val)) {
            try {
                ret = Integer.parseInt(val);
            } catch (NumberFormatException exception) {
                // Ignore
            }
        }

        return ret;
    }

    public Properties getProperties() {
        return props;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("DEFAULT_CONFIG_FILE=").append(DEFAULT_CONFIG_FILE).append(", ")
                .append("CONFIG_FILE=").append(CONFIG_FILE).append("\n\n");

        return sb + super.toString();
    }

    public String getTagSyncMetricsFileName() {
        String val = getProperties().getProperty(TAGSYNC_METRICS_FILEPATH);

        if (StringUtils.isBlank(val)) {
            if (StringUtils.isBlank(System.getProperty("logdir"))) {
                val = DEFAULT_TAGSYNC_METRICS_FILEPATH;
            } else {
                val = System.getProperty("logdir");
            }
        }

        if (Files.notExists(Paths.get(val))) {
            return null;
        }

        StringBuilder pathAndFileName = new StringBuilder(val);

        if (!val.endsWith("/")) {
            pathAndFileName.append("/");
        }

        String fileName = getProperties().getProperty(TAGSYNC_METRICS_FILENAME);

        if (StringUtils.isBlank(fileName)) {
            fileName = DEFAULT_TAGSYNC_METRICS_FILENAME;
        }

        pathAndFileName.append(fileName);

        return pathAndFileName.toString();
    }

    public long getTagSyncMetricsFrequency() {
        long   ret = DEFAULT_TAGSYNC_METRICS_FREQUENCY__TIME_IN_MILLIS;
        String val = getProperties().getProperty(TAGSYNC_METRICS_FREQUENCY_TIME_IN_MILLIS_PARAM);

        if (StringUtils.isNotBlank(val)) {
            try {
                ret = Long.parseLong(val);
            } catch (NumberFormatException exception) {
                // Ignore
            }
        }

        return ret;
    }

    private void init() {
        readConfigFile(CORE_SITE_FILE);
        readConfigFile(DEFAULT_CONFIG_FILE);
        readConfigFile(CONFIG_FILE);

        props = getProps();

        @SuppressWarnings("unchecked")
        Enumeration<String> propertyNames = (Enumeration<String>) props.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String propertyName        = propertyNames.nextElement();
            String systemPropertyValue = System.getProperty(propertyName);

            if (systemPropertyValue != null) {
                props.setProperty(propertyName, systemPropertyValue);
            }
        }
    }

    private void readConfigFile(String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            String fName = getResourceFileName(fileName);

            if (StringUtils.isBlank(fName)) {
                LOG.warn("Cannot find configuration file {} in the classpath", fileName);
            } else {
                LOG.info("Loading configuration from {}", fName);
                addResource(fileName);
            }
        } else {
            LOG.error("Configuration fileName is null");
        }
    }

    static {
        try {
            localHostname = java.net.InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            localHostname = "unknown";
        }
    }

    //openmetadata methods
    static public String getOpenmetadataRESTToken(Properties prop) {
        String token = null;
        try{
            if(prop!=null && prop.containsKey(TAGSYNC_OPENMETADATAREST_TOKEN_PROP)){
                token = prop.getProperty(TAGSYNC_OPENMETADATAREST_TOKEN_PROP);
            }
            else if (prop != null && prop.containsKey(TAGSYNC_OPENMETADATAREST_KEYSTORE_PROP)) {
                String path = prop.getProperty(TAGSYNC_OPENMETADATAREST_KEYSTORE_PROP);
                if (path != null) {
                    if (!path.trim().isEmpty()) {
                        try {
                            token = CredentialReader.getDecryptedString(path.trim(), TAGSYNC_OPENMETADATAREST_TOKEN_PROP, getTagsyncKeyStoreType(prop));
                        } catch (Exception ex) {
                            token = null;
                        }
                        if (token != null && !token.trim().isEmpty() && !token.trim().equalsIgnoreCase("none")) {
                            return token;
                        }
                    }
                }
                else{
                    LOG.info("==> Keystore Property not set for OpenMetadata token. Using the token directly from property.");
                }
            }
            else{
                token = null;
            }
        }
        catch(Exception exception){
            LOG.error("The token required to connect with Openmetadata is either null or incorrect. Expecting a valid non null token.", exception);
        }
        return token;
    }

    static public String getOpenmetadataRESTEndpoint(Properties prop) {
        return prop.getProperty(TAGSYNC_OPENMETADATASOURCE_ENDPOINT_PROP);
    }

    static public int getOpenmetadataRestTagSourceEntitiesBatchSize(Properties prop) {
        String val = prop.getProperty(TAGSYNC_OPENMETADATAREST_SOURCE_ENTITIES_BATCH_SIZE);
        int    ret = DEFAULT_TAGSYNC_ATLASREST_SOURCE_ENTITIES_BATCH_SIZE;

        if (StringUtils.isNotBlank(val)) {
            try {
                ret = Integer.valueOf(val);
            } catch (NumberFormatException exception) {
                // Ignore
            }
        }

        return ret;
    }

    static public long getOpenmetadataRESTTagSourceDownloadIntervalInMillis(Properties prop) {
        String val = prop.getProperty(TAGSYNC_OPENMETADATAREST_SOURCE_DOWNLOAD_INTERVAL);
        long    ret = DEFAULT_TAGSYNC_OPENMETADATAREST_SOURCE_DOWNLOAD_INTERVAL;

        if (StringUtils.isNotBlank(val)) {
            try {
                ret = Long.valueOf(val);
            } catch (NumberFormatException exception) {
                // Ignore
            }
        }

        return ret;
    }

    static public String getOpenmetadataRESTSslConfigFile(Properties prop) {
        return prop.getProperty(TAGSYNC_OPENMETADATA_REST_SSL_CONFIG_FILENAME);
    }

    static public String getCustomOpenmetadataRESTResourceMappers(Properties prop) {
        return prop.getProperty(TAGSYNC_SOURCE_OPENMETADATA_CUSTOM_RESOURCE_MAPPERS_PROP);
    }

    static public String getRangerOpenmetadataTableComponentName(Properties prop) {
        String tableComponentType = null;
        try{
            if(prop!=null && prop.containsKey(RANGER_OPENMETADATA_TABLE_COMPONENT_NAME)){
                tableComponentType = prop.getProperty(RANGER_OPENMETADATA_TABLE_COMPONENT_NAME);
            }
        }
        catch(Exception exception){
            LOG.warn("Error getting table component name", exception);
            LOG.warn("Setting property 'RANGER_OPENMETADATA_TABLE_COMPONENT_NAME' to default value");
            tableComponentType = DEFAULT_RANGER_OPENMETADATA_TABLE_COMPONENT_NAME;
        }
        return tableComponentType;
    }
}
