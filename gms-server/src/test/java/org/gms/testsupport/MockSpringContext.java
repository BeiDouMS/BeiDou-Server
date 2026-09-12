package org.gms.testsupport;

import org.gms.manager.ServerManager;
import org.gms.property.ServiceProperty;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 单测用的假 Spring 上下文。
 * <p>
 * 遗留代码（Character / Server / I18nUtil 等）在类静态初始化时就通过
 * {@code ServerManager.getApplicationContext().getBean(...)} 拉 Spring bean，
 * 没有上下文连 mock 都建不出来。这里装一个「要什么 bean 就给什么 mock」的上下文，
 * 让不需要数据库的纯逻辑测试可以跑起来。i18n 的 MessageSource 直接把 code 原样返回。
 */
public final class MockSpringContext {
    private static boolean installed = false;

    private MockSpringContext() {
    }

    public static synchronized void install() {
        if (installed) {
            return;
        }
        ApplicationContext ctx = mock(ApplicationContext.class);

        ServiceProperty serviceProperty = new ServiceProperty();
        serviceProperty.setLanguage("zh-CN");

        MessageSource passthrough = mock(MessageSource.class);
        when(passthrough.getMessage(anyString(), any(), any(Locale.class)))
                .thenAnswer(inv -> inv.getArgument(0, String.class));

        when(ctx.getBean(any(Class.class))).thenAnswer(inv -> {
            Class<?> type = inv.getArgument(0);
            if (type == ServiceProperty.class) {
                return serviceProperty;
            }
            if (MessageSource.class.isAssignableFrom(type)) {
                return passthrough;
            }
            return Mockito.mock(type);
        });
        when(ctx.getBean(anyString(), any(Class.class))).thenAnswer(inv -> {
            Class<?> type = inv.getArgument(1);
            if (MessageSource.class.isAssignableFrom(type)) {
                return passthrough;
            }
            return Mockito.mock(type);
        });

        new ServerManager().setApplicationContext(ctx);
        installed = true;
    }
}
