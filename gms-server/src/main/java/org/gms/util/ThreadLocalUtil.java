package org.gms.util;

import org.gms.client.Client;

public class ThreadLocalUtil {
    private static final ThreadLocal<Client> threadLocal = new ThreadLocal<>();

    public static void setCurrentClient(Client c) {
        threadLocal.set(c);
    }

    public static Client getCurrentClient() {
        return threadLocal.get();
    }

    public static void removeCurrentClient() {
        threadLocal.remove();
    }
}
