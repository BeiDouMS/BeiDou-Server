package org.gms.client.fake;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * YAML 属性源工厂
 * <p>
 * 用于在 Spring Boot 中加载 YAML 配置文件
 * </p>
 *
 * @author BeiDou
 * @since 1.0.0
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());
        factory.afterPropertiesSet();

        Properties properties = factory.getObject();
        if (properties == null) {
            throw new IOException("Failed to load YAML properties from " + resource.getResource());
        }

        String sourceName = (name != null) ? name : resource.getResource().getFilename();
        return new PropertiesPropertySource(sourceName, properties);
    }
}
