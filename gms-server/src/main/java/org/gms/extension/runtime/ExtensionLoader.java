package org.gms.extension.runtime;

import org.gms.extension.api.HostRuntime;
import org.gms.extension.api.ServerExtension;
import org.gms.extension.api.event.ServerReadyEvent;
import org.gms.extension.api.event.ServerShutdownEvent;
import org.gms.util.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Discovers {@link ServerExtension} implementations from the application classpath
 * and from {@code plugins/*.jar}, then drives their lifecycle.
 */
public final class ExtensionLoader {

    private static final Logger log = LoggerFactory.getLogger(ExtensionLoader.class);

    private static final ExtensionLoader INSTANCE = new ExtensionLoader();

    private final List<ServerExtension> extensions = new ArrayList<>();
    private final List<URLClassLoader> pluginLoaders = new ArrayList<>();
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    private HostRuntime runtime;

    private ExtensionLoader() {
    }

    public static ExtensionLoader getInstance() {
        return INSTANCE;
    }

    public HostRuntime getRuntime() {
        return runtime;
    }

    public synchronized void load(HostRuntime runtime, Path pluginsDir) {
        if (!loaded.compareAndSet(false, true)) {
            log.warn(I18nUtil.getLogMessage("ExtensionLoader.load.warn.alreadyLoaded"));
            return;
        }
        this.runtime = runtime;

        List<ServerExtension> discovered = new ArrayList<>();
        discovered.addAll(loadFromClassLoader(ServerExtension.class.getClassLoader(), "classpath"));
        discovered.addAll(loadFromPluginDirectory(pluginsDir));

        for (ServerExtension ext : discovered) {
            try {
                log.info(I18nUtil.getLogMessage("ExtensionLoader.load.info.loading"), ext.id(), ext.version());
                ext.onLoad(runtime);
                extensions.add(ext);
                log.info(I18nUtil.getLogMessage("ExtensionLoader.load.info.loaded"), ext.id());
            } catch (Exception e) {
                log.error(I18nUtil.getLogMessage("ExtensionLoader.load.error.onLoad"), ext.id(), e);
            }
        }

        log.info(I18nUtil.getLogMessage("ExtensionLoader.load.info.summary"), extensions.size());
    }

    public synchronized void notifyServerReady() {
        if (runtime != null) {
            runtime.events().publish(new ServerReadyEvent(System.currentTimeMillis()));
        }
        for (ServerExtension ext : extensions) {
            try {
                ext.onServerReady();
                log.info(I18nUtil.getLogMessage("ExtensionLoader.ready.info"), ext.id());
            } catch (Exception e) {
                log.error(I18nUtil.getLogMessage("ExtensionLoader.ready.error"), ext.id(), e);
            }
        }
    }

    public synchronized void unloadAll() {
        if (runtime != null) {
            runtime.events().publish(new ServerShutdownEvent(System.currentTimeMillis()));
        }
        for (ServerExtension ext : List.copyOf(extensions)) {
            try {
                ext.onUnload();
            } catch (Exception e) {
                log.error(I18nUtil.getLogMessage("ExtensionLoader.unload.error"), ext.id(), e);
            }
        }
        extensions.clear();
        for (URLClassLoader loader : pluginLoaders) {
            try {
                loader.close();
            } catch (IOException e) {
                log.warn("Failed to close plugin classloader", e);
            }
        }
        pluginLoaders.clear();
        loaded.set(false);
        runtime = null;
    }

    private List<ServerExtension> loadFromClassLoader(ClassLoader classLoader, String source) {
        List<ServerExtension> result = new ArrayList<>();
        try {
            ServiceLoader<ServerExtension> loader = ServiceLoader.load(ServerExtension.class, classLoader);
            for (ServerExtension ext : loader) {
                log.info(I18nUtil.getLogMessage("ExtensionLoader.discover.info"), ext.id(), source);
                result.add(ext);
            }
        } catch (Exception e) {
            log.error(I18nUtil.getLogMessage("ExtensionLoader.discover.error"), source, e);
        }
        return result;
    }

    private List<ServerExtension> loadFromPluginDirectory(Path pluginsDir) {
        List<ServerExtension> result = new ArrayList<>();
        if (pluginsDir == null) {
            return result;
        }
        try {
            Files.createDirectories(pluginsDir);
        } catch (IOException e) {
            log.error(I18nUtil.getLogMessage("ExtensionLoader.plugins.error.mkdir"), pluginsDir, e);
            return result;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pluginsDir, "*.jar")) {
            for (Path jar : stream) {
                try {
                    URLClassLoader pluginCl = new URLClassLoader(
                            new URL[]{jar.toUri().toURL()},
                            ServerExtension.class.getClassLoader());
                    pluginLoaders.add(pluginCl);
                    log.info(I18nUtil.getLogMessage("ExtensionLoader.plugins.info.jar"), jar.getFileName());
                    result.addAll(loadFromClassLoader(pluginCl, jar.getFileName().toString()));
                } catch (Exception e) {
                    log.error(I18nUtil.getLogMessage("ExtensionLoader.plugins.error.jar"), jar, e);
                }
            }
        } catch (IOException e) {
            log.error(I18nUtil.getLogMessage("ExtensionLoader.plugins.error.scan"), pluginsDir, e);
        }
        return result;
    }
}
