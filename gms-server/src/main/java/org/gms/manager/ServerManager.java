package org.gms.manager;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.gms.ServerApplication;
import org.gms.constants.net.ServerConstants;
import org.gms.extension.api.HostRuntime;
import org.gms.extension.runtime.BeiDouHostCommandRegistry;
import org.gms.extension.runtime.BeiDouHostConfig;
import org.gms.extension.runtime.BeiDouHostRuntime;
import org.gms.extension.runtime.ExtensionLoader;
import org.gms.extension.runtime.InMemoryHostEventBus;
import org.gms.net.server.Server;
import org.gms.util.I18nUtil;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Path;

@Component
@Slf4j
public class ServerManager implements ApplicationContextAware, ApplicationRunner, DisposableBean {
    @Getter
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        ServerManager.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Environment environment = applicationContext.getBean(Environment.class);
        boolean pluginsEnabled = environment.getProperty(BeiDouHostConfig.PLUGINS_ENABLED, Boolean.class, true);
        if (pluginsEnabled) {
            HostRuntime runtime = new BeiDouHostRuntime(
                    new BeiDouHostConfig(environment),
                    new InMemoryHostEventBus(),
                    new BeiDouHostCommandRegistry());
            Path pluginsDir = Path.of(environment.getProperty(BeiDouHostConfig.PLUGINS_DIR, "plugins"));
            ExtensionLoader.getInstance().load(runtime, pluginsDir);
        }

        Server.getInstance().init();

        if (pluginsEnabled) {
            ExtensionLoader.getInstance().notifyServerReady();
        }

        SpringDocConfigProperties springDocConfigProperties = applicationContext.getBean(SpringDocConfigProperties.class);
        SwaggerUiConfigProperties swaggerUiConfigProperties = applicationContext.getBean(SwaggerUiConfigProperties.class);
        log.info(I18nUtil.getLogMessage("ServerManager.run.info3"), ServerConstants.BEI_DOU_VERSION, ServerConstants.BEI_DOU_BUILD_TIME);
        if (springDocConfigProperties.getApiDocs().isEnabled() && swaggerUiConfigProperties.isEnabled()) {
            log.info(I18nUtil.getLogMessage("ServerManager.run.info1"), InetAddress.getLocalHost().getHostAddress(), environment.getProperty("server.port"));
        }
        // 判断是否集成前端，集成则提示前端地址
        try (InputStream resource = ServerApplication.class.getClassLoader().getResourceAsStream("static/index.html")) {
            if (resource != null) {
                log.info(I18nUtil.getLogMessage("ServerManager.run.info2"), InetAddress.getLocalHost().getHostAddress(), environment.getProperty("server.port"));
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        ExtensionLoader.getInstance().unloadAll();
        Server.getInstance().shutdownInternal(false);
    }
}
