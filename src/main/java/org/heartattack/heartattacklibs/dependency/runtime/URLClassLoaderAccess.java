package org.heartattack.heartattacklibs.dependency.runtime;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public final class URLClassLoaderAccess {
    private final ClassLoader loader;
    private final Method addUrlMethod;

    private URLClassLoaderAccess(ClassLoader loader, Method addUrlMethod) {
        this.loader = loader;
        this.addUrlMethod = addUrlMethod;
    }

    public static URLClassLoaderAccess create(ClassLoader loader) {
        if (!(loader instanceof URLClassLoader urlClassLoader)) {
            return null;
        }
        try {
            Method addUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addUrl.setAccessible(true);
            return new URLClassLoaderAccess(urlClassLoader, addUrl);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    public void addUrl(URL url) throws ReflectiveOperationException {
        addUrlMethod.invoke(loader, url);
    }
}
