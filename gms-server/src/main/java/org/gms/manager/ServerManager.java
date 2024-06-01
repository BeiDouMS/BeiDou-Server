package org.gms.manager;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.gms.net.server.Server;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ServerManager implements ApplicationContextAware, CommandLineRunner, DisposableBean {
    private ApplicationContext applicationContext;

    @Override
    public void destroy() throws Exception {
        Server.getInstance().shutdown(false);
    }

    @Override
    public void run(String... args) throws Exception {
        Server.getInstance().init();
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
