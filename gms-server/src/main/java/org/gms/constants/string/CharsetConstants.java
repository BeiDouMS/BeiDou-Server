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

import org.gms.config.YamlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

public class CharsetConstants {
    private static final Logger log = LoggerFactory.getLogger(CharsetConstants.class);
    public static final Charset CHARSET = loadCharset();

    public static Charset getCharset(int language) {
        return Charset.forName(Language.fromLang(language).getCharset());
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
            return LANGUAGE_CN;
        }

        public static Language fromCharset(String charset) {
            Optional<Language> language = Arrays.stream(values())
                    .filter(l -> l.charset.equals(charset))
                    .findAny();
            if (language.isEmpty()) {
                log.warn("Charset {} was not found, defaulting to US-ASCII", charset);
                return LANGUAGE_CN;
            }

            return language.get();
        }
    }

    private static String loadCharsetFromConfig() {
        try {
            return YamlConfig.config.server.CHARSET;
        } catch (Exception e) {
            throw new RuntimeException("Could not successfully parse charset from config file: " + e.getMessage());
        }
    }

    private static Charset loadCharset() {
        String configCharset = loadCharsetFromConfig();
        if (configCharset != null) {
            Language language = Language.fromCharset(configCharset);
            return Charset.forName(language.getCharset());
        }

        return Charset.forName(Language.LANGUAGE_CN.getCharset());
    }

    private static class StrippedYamlConfig {
        public StrippedServerConfig server;

        private static class StrippedServerConfig {
            public String CHARSET;
        }
    }
}