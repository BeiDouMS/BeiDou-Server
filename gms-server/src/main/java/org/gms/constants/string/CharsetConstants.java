/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gms.constants.string;

/*
 * Thanks to GabrielSin (EllinMS) - gabrielsin@playellin.net
 * Ellin
 * MapleStory Server
 * CharsetConstants
 */

import org.gms.manager.ServerManager;
import org.gms.property.ServiceProperty;

import java.nio.charset.Charset;

public class CharsetConstants {
    // 保证只加载一次
    private static final Language SERViCE_LANGUAGE = loadServiceLanguage();

    public static Charset getCharset(int language) {
        return Charset.forName(Language.fromLang(language).getCharset());
    }

    private static Language loadServiceLanguage() {
        ServiceProperty serviceProperty = ServerManager.getApplicationContext().getBean(ServiceProperty.class);
        String language = serviceProperty.getLanguage();
        if (language.equals("zh-CN")) {
            return Language.LANGUAGE_CN;
        } else {
            return Language.LANGUAGE_US;
        }
    }

    /**
     * @see LanguageConstants
     */
    private enum Language {
        LANGUAGE_US(2, "US-ASCII"),
        LANGUAGE_CN(3, "GBK"),
        LANGUAGE_PT_BR(-1, "ISO-8859-1"),
        LANGUAGE_THAI(-1, "TIS620"),
        LANGUAGE_KOREAN(-1, "MS949");

        private final int lang;
        private final String charset;

        Language(int lang, String charset) {
            this.lang = lang;
            this.charset = charset;
        }

        public int getLang() {
            return lang;
        }

        public String getCharset() {
            return charset;
        }

        public static Language fromLang(int lang) {
            for (Language value : values()) {
                if (value.getLang() == lang) {
                    return value;
                }
            }
            return SERViCE_LANGUAGE;
        }
    }
}