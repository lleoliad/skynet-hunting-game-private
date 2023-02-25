package org.skynet.service.provider.hunting.obsolete.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "server.config")
public class SystemPropertiesConfig implements InitializingBean {

    private String[] clientGameVersion;
    private Boolean production;
    private Boolean supportRecordModeClient;
    private String[] groups;
    private String googleWebClientId;
    private String facebookAppId;
    private String facebookAppSecret;
    private String cloudUrl;
    private String fightUrl;
    private String aiUrl;
    private String rankUrl;

    public static String[] CLIENT_GAME_VERSION;
    public static Boolean PRODUCTION;
    public static Boolean SUPPORT_RECORD_MODE_CLIENT;
    public static String[] GROUPS;
    public static String GOOGLE_WEB_CLIENT_ID;
    public static String FACEBOOK_APP_ID;
    public static String FACEBOOK_APP_SECRET;
    private static String CLOUD_URL;
    private static String FIGHT_URL;
    private static String AI_URL;
    private static String RANK_URL;

    @Override
    public void afterPropertiesSet() throws Exception {
        CLIENT_GAME_VERSION = clientGameVersion;
        PRODUCTION = production;
        SUPPORT_RECORD_MODE_CLIENT = supportRecordModeClient;
        GROUPS = groups;
        GOOGLE_WEB_CLIENT_ID = googleWebClientId;
        FACEBOOK_APP_ID = facebookAppId;
        FACEBOOK_APP_SECRET = facebookAppSecret;
        CLOUD_URL = cloudUrl;
        FIGHT_URL = fightUrl;
        AI_URL = aiUrl;
        RANK_URL = rankUrl;
    }
}
