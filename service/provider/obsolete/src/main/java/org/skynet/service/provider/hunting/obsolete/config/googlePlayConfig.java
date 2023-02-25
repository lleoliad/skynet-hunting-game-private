package org.skynet.service.provider.hunting.obsolete.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "google.config")
public class googlePlayConfig implements InitializingBean {

    private String type;
    private String projectId;
    private String privateKeyId;
    private String privateKey;
    private String clientEmail;
    private String clientId;
    private String authUrl;
    private String tokenUrl;
    private String authProviderX509CertUrl;
    private String clientX509CertUrl;

    public static String TYPE;
    public static String PROJECT_ID;
    public static String private_key_id;
    public static String private_key;
    public static String client_email;
    public static String client_id;
    public static String auth_uri;
    public static String token_uri;
    public static String auth_provider_x509_cert_url;
    public static String client_x509_cert_url;


    @Override
    public void afterPropertiesSet() throws Exception {
        TYPE = type;
        PROJECT_ID = projectId;
        private_key_id = privateKeyId;
        private_key = privateKey;
        client_email = clientEmail;
        client_id = clientId;
        auth_uri = authUrl;
        token_uri = tokenUrl;
        auth_provider_x509_cert_url = authProviderX509CertUrl;
        client_x509_cert_url = clientX509CertUrl;

    }

}
