package org.gms.manager;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.gms.ServerApplication;
import org.gms.constants.net.ServerConstants;
import org.gms.net.server.Server;
import org.gms.util.I18nUtil;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.InputStream;
import java.net.InetAddress;

@Component
@Slf4j
public class ServerManager implements ApplicationContextAware, ApplicationRunner, DisposableBean {
    @Getter
    private static ApplicationContext applicationContext;

    /**
     * 只为声明依赖：Spring 按「依赖者先销毁」的顺序销毁 bean，注入 DataSource 才能保证
     * destroy() 里的断线存档一定跑在连接池关闭之前。现在的 mybatis-flex FlexDataSource 恰好没有
     * close 方法所以不会被 Spring 关闭，但这是巧合，不该依赖它。
     */
    @Autowired
    @SuppressWarnings("unused")
    private DataSource dataSource;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        ServerManager.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Server.getInstance().init();

        SpringDocConfigProperties springDocConfigProperties = applicationContext.getBean(SpringDocConfigProperties.class);
        SwaggerUiConfigProperties swaggerUiConfigProperties = applicationContext.getBean(SwaggerUiConfigProperties.class);
        Environment environment = applicationContext.getBean(Environment.class);
        log.info(I18nUtil.getLogMessage("ServerManager.run.info3"), ServerConstants.BEI_DOU_VERSION, ServerConstants.BEI_DOU_BUILD_TIME);
        if (springDocConfigProperties.getApiDocs().isEnabled() && swaggerUiConfigProperties.isEnabled()) {
            log.info(I18nUtil.getLogMessage("ServerManager.run.info1"), InetAddress.getLocalHost().getHostAddress(), environment.getProperty("server.port"));
        }
        // 判断是否集成前端，集成则提示前端地址
        try(InputStream resource = ServerApplication.class.getClassLoader().getResourceAsStream("static/index.html")) {
            if (resource != null) {
                log.info(I18nUtil.getLogMessage("ServerManager.run.info2"), InetAddress.getLocalHost().getHostAddress(), environment.getProperty("server.port"));
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        Server.getInstance().shutdownInternal(false);
    }
}
