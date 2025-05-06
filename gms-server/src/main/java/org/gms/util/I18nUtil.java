package org.gms.util;

import org.gms.client.Client;
import org.gms.constants.string.CharsetConstants;
import org.gms.manager.ServerManager;
import org.gms.property.ServiceProperty;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * messageSource.getMessage底层是通过循环遍历文件名去读取的
 * 所以将不同文件名定义不同的bean，这样扫描的时候可以少扫描其他文件，直接找到想要对应的文件，节约时间
 */
public class I18nUtil {
    public static final Locale LANGUAGE = Locale.forLanguageTag(ServerManager.getApplicationContext().getBean(ServiceProperty.class).getLanguage());
    public static final MessageSource messageSource = ServerManager.getApplicationContext().getBean("messageSource", MessageSource.class);
    public static final MessageSource logSource = ServerManager.getApplicationContext().getBean("logSource", MessageSource.class);
    public static final MessageSource exceptionSource = ServerManager.getApplicationContext().getBean("exceptionSource", MessageSource.class);

    public static String getMessage(String code, Object... args) {
        // 如果当前存在客户端请求，则以客户端的语言为准。如果当前非客户端请求，是服务端主动发给客户端的，则以服务端语言为准
        Locale clientLang = CharsetConstants.getLanguageLocale(ThreadLocalUtil.getClientLang());
        // 确保所有参数转为字符串，包括数字类型（避免千分符问题）
        String[] stringArgs = Arrays.stream(args)
                .map(String::valueOf)
                .toArray(String[]::new);
        return messageSource.getMessage(code, stringArgs, clientLang);
    }

    public static String getMessage(Locale locale, String code, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }

    /**
     * 用StringFormat格式的message，传参是通过{0} {1}
     *
     * @param code messageCode
     * @param args 传参
     * @return 组合后的message
     */
    public static String getLogMessage(String code, Object... args) {
        return logSource.getMessage(code, args, LANGUAGE);
    }

    /**
     * 根据传入的语言获取message
     *
     * @param locale 语言
     * @param code   messageCode
     * @param args   传参
     * @return 组合后的message
     */
    public static String getLogMessage(Locale locale, String code, Object... args) {
        return logSource.getMessage(code, args, locale);
    }

    public static String getExceptionMessage(String code, Object... args) {
        return exceptionSource.getMessage(code, args, LANGUAGE);
    }

    public static String getExceptionMessage(Locale locale, String code, Object... args) {
        return exceptionSource.getMessage(code, args, locale);
    }
}
