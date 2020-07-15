package com.itcast.minio.config;

import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.*;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zheng.zhang
 * @description swagger 配置类
 * @date 2020/4/27 9:38
 */
@Data
@Configuration
@EnableSwagger2
@ConfigurationProperties(prefix = "swagger")
public class SwaggerConfig {
    private String title;
    private String desc;
    private String version;
    private String termsOfServiceUrl;
    private String license;
    private String licenseUrl;
    private String basePackage;
    private String groupName;
    private String contactName;
    private String contactUrl;
    private String contactEmail;

    /**
     * 创建该API的基本信息（这些基本信息会展现在文档页面中）
     * 访问地址：http://项目实际地址/swagger-ui.html
     * @return ApiInfo
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                // swagger标题
                .title(title)
                // 描述信息
                .description(desc)
                // 版本号
                .version(version)
                // 项目地址
                .termsOfServiceUrl(termsOfServiceUrl)
                // 许可证信息
                // .licenseUrl(licenseUrl)
                // .license(license)
                // 作者相关信息
                .contact(new Contact(contactName, contactUrl, contactEmail))
                .build();
    }

    /**
     * 创建API应用
     * apiInfo() 增加API相关信息
     * 通过select()函数返回一个ApiSelectorBuilder实例,用来控制哪些接口暴露给Swagger来展现，
     * 本例采用指定扫描的包路径来定义指定要建立API的目录。
     */
    @Bean(value = "defaultApi")
    public Docket createRestApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                //组名
                .groupName(groupName)
                // 将LocalDate类型全部转为String类型
                .directModelSubstitute(LocalDate.class, String.class)
                // 通用的模板替代
                .genericModelSubstitutes(ResponseEntity.class)
                // 是否使用默认响应信息
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.POST, responseMessage())
                .globalResponseMessage(RequestMethod.GET, responseMessage())
                //通用编码
                .forCodeGeneration(true)
                // 选择那些路径和api会生成document
                .select()
                // 对指定api进行监控
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                // 对所有路径进行监控
                .paths(PathSelectors.any())
                .build()
                // securitySchemes与securityContexts作用为配置全局Authorization参数
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
        return docket;
    }

    /**
     * API授权登录
     * @return
     */
    private List<ApiKey> securitySchemes() {
        List<ApiKey> result = new ArrayList<>();
        // 添加TOKEN验证字段
        ApiKey apiKey = new ApiKey("token", "token", "header");
        result.add(apiKey);
        return result;
    }

    /**
     * 接口授权
     * @return
     */
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> result = new ArrayList<>();
        SecurityContext securityContext = SecurityContext.builder().securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("^(?!auth).*$"))
                .build();
        result.add(securityContext);
        return result;
    }

    /**
     * 默认验证
     * @return
     */
    private List<SecurityReference> defaultAuth() {
        List<SecurityReference> references = new ArrayList<>();
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        references.add(new SecurityReference("Authorization", authorizationScopes));
        return references;
    }

    /**
     * 统一响应数据
     * @return
     */
    private List<ResponseMessage> responseMessage() {
        List<ResponseMessage> list = new ArrayList<>();
        list.add(new ResponseMessageBuilder().code(200).message("请求成功").build());
        list.add(new ResponseMessageBuilder().code(201).message("资源创建成功").build());
        list.add(new ResponseMessageBuilder().code(204).message("服务器成功处理了请求，但不需要返回任何实体内容").build());
        list.add(new ResponseMessageBuilder().code(400).message("请求失败,具体查看返回业务状态码与对应消息").build());
        list.add(new ResponseMessageBuilder().code(401).message("请求失败,未经过身份认证").build());
        list.add(new ResponseMessageBuilder().code(405).message("请求方法不支持").build());
        list.add(new ResponseMessageBuilder().code(415).message("请求媒体类型不支持").build());
        list.add(new ResponseMessageBuilder().code(500).message("服务器遇到了一个未曾预料的状况,导致了它无法完成对请求的处理").build());
        list.add(new ResponseMessageBuilder().code(503).message("服务器当前无法处理请求,这个状况是临时的，并且将在一段时间以后恢复").build());
        return list;
    }

}
