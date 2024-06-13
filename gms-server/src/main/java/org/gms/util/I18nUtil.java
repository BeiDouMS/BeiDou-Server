package org.gms.util;

import org.gms.manager.ServerManager;
import org.gms.property.ServiceProperty;
import org.springframework.context.MessageSource;

import java.util.Locale;

public class I18nUtil {
    public static final Locale LANGUAGE = Locale.forLanguageTag(ServerManager.getApplicationContext().getBean(ServiceProperty.class).getLanguage());
    public static final MessageSource messageSource = ServerManager.getApplicationContext().getBean(MessageSource.class);

    /**
     * 用StringFormat格式的message，传参是通过{0} {1}
     *
     * @param code messageCode
     * @param args 传参
     * @return 组合后的message
     */
    public static String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LANGUAGE);
    }
}
