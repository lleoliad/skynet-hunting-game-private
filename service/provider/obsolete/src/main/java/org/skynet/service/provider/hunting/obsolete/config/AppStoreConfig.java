package org.skynet.service.provider.hunting.obsolete.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "appstore.config")
public class AppStoreConfig implements InitializingBean {

    private String[] bundleIds;

    public static String[] BUNDLE_IDS;

    @Override
    public void afterPropertiesSet() throws Exception {
        BUNDLE_IDS = bundleIds;
    }

}
