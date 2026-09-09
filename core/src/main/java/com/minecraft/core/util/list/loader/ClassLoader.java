package com.minecraft.core.util.list.loader;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Modified by Cassio Martim.
 */
public class ClassLoader {

    public static List<Class<?>> getClassesForPackage(Object instance, String source) {
        List<Class<?>> classes = new ArrayList<>();

        // Get a File object for the package
        CodeSource src = instance.getClass().getProtectionDomain().getCodeSource();
        if (src != null) {
            URL resource = src.getLocation();
            resource.getPath();
            processJarfile(resource, source, classes);
        }

        List<String> names = new ArrayList<>();
        List<Class<?>> classi = new ArrayList<>();

        classes.forEach(c -> {
            names.add(c.getSimpleName());
            classi.add(c);
        });

        classes.clear();

        names.sort(String.CASE_INSENSITIVE_ORDER);
        names.forEach(name -> {
            for (Class<?> c : classi)
                if (c.getSimpleName().equalsIgnoreCase(name))
                    classes.add(c);
        });

        return classes;
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unexpected ClassNotFoundException loading class '" + className + "'");
        }
    }

    private static void processJarfile(URL resource, String source, List<Class<?>> classes) {
        String relPath = source.replace('.', '/');
        String resPath = resource.getPath().replace("%20", " ");
        String jarPath = resPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
        JarFile jarFile;

        try {
            jarFile = new JarFile(jarPath);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected IOException reading JAR File '" + jarPath + "'", e);
        }

        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            String className = null;
            if (entryName.endsWith(".class") && entryName.startsWith(relPath)
                    && entryName.length() > (relPath.length() + "/".length())) {
                className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
            }
            if (className != null) {
                classes.add(loadClass(className));
            }
        }
    }
}