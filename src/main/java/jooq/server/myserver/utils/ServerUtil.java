package jooq.server.myserver.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jooq.Condition;
import org.jooq.DSLContext;

import jooq.server.myserver.dto.MethodKey;
import jooq.server.myserver.exceptions.DslMethodLoadException;

public class ServerUtil {

    private final Map<String, MethodKey> methodRegistry = new HashMap<>();
    private final URLClassLoader loader;

    public URLClassLoader getLoader() {
        return loader;
    }

    public Map<String, MethodKey> getMethodRegistry() {
        return methodRegistry;
    }

    public ServerUtil(String jarPath) throws Exception {
        this.loader = new URLClassLoader(
                new URL[] { new File(jarPath).toURI().toURL() },
                ServerUtil.class.getClassLoader());
    }

    public Set<String> loadJar(String jarPath) throws Exception {
        try (JarFile jar = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace("/", ".").replace(".class", "");
                    Class<?> clazz = loader.loadClass(className);

                    for (Method m : clazz.getDeclaredMethods()) {
                        if (matchesSignature(m)) {
                            String key = clazz.getSimpleName() + ":" + m.getName();
                            methodRegistry.put(key, new MethodKey(className, m.getName(),
                                    Arrays.stream(m.getParameterTypes())
                                            .map(Class::getName)
                                            .toList()));
                        }
                    }
                }
            }
        }
        return methodRegistry.keySet();
    }

    private static boolean matchesSignature(Method method) {
        Class<?>[] params = method.getParameterTypes();
        return params.length == 2
                && params[0] == DSLContext.class
                && params[1] == Condition.class;
    }

    public Object invoke(String key, ServerUtil instance, Object... args) throws Exception {

        MethodKey methodKey = instance.getMethodRegistry().get(key);
        if (methodKey == null)
            throw new RuntimeException("Method not found: " + key);
        Method m = resolveMethod(methodKey, instance.getLoader());
        return m.invoke(null, args);
    }

    public void unload() throws Exception {
        try (loader) {
            methodRegistry.clear();
        }
    }

    public Method resolveMethod(MethodKey key, ClassLoader loader) throws Exception {

        try {
            Class<?> clazz = Class.forName(key.className(), true, loader);
            Class<?>[] paramTypes = key.paramTypes().stream()
                    .map(name -> {
                        try {
                            return Class.forName(name, true, loader);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }).toArray(Class<?>[]::new);
            return clazz.getDeclaredMethod(key.methodName(), paramTypes);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
            throw new DslMethodLoadException("Failed to resolve method: " + key, e);
        }
    }

}   
