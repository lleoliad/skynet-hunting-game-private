// package org.skynet.service.provider.hunting.obsolete.config;
//
// import com.google.common.base.Predicates;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import springfox.documentation.builders.ApiInfoBuilder;
// import springfox.documentation.builders.PathSelectors;
// import springfox.documentation.service.ApiInfo;
// import springfox.documentation.spi.DocumentationType;
// import springfox.documentation.spring.web.plugins.Docket;
// import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
// /**
//  * swagger配置
//  */
// @Configuration
// @EnableSwagger2
// public class Swagger2Config {
//
//     @Bean
//     public Docket huntingrivalServerConfig() {
//         return new Docket(DocumentationType.SWAGGER_2)
//                 .apiInfo(huntingrivalServerApiInfo())
//                 .groupName("huntingrival-server")
//                 .select()
//                 .paths(Predicates.and(PathSelectors.regex("/huntingrival/.*")))
//                 .build();
//     }
//
//     private ApiInfo huntingrivalServerApiInfo() {
//         return new ApiInfoBuilder()
//                 .title("huntingrival-server后台系统Api文档")
//                 .version("0.1")
//                 .build();
//     }
// }
