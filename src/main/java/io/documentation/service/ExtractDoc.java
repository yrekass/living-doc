package io.documentation.service;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ExtractDoc {

    public List<Class<?>> scanForAnnotatedClasses(File outputDirectory,String annotationClassName) throws ClassNotFoundException, IOException {
        List<Class<?>> annotatedClasses = new ArrayList<>();
        ClassLoader classLoader = new URLClassLoader(new URL[]{outputDirectory.toURI().toURL()});
        File[] files = outputDirectory.listFiles((dir, name) -> name.endsWith(".class"));
        if (files != null) {
            for (File file : files) {
                String className = file.getName().replace(".class", "");
                Class<?> clazz = classLoader.loadClass(className);
                Annotation annotation = clazz.getAnnotation((Class<? extends Annotation>) classLoader.loadClass(annotationClassName));
                if (annotation != null) {
                    annotatedClasses.add(clazz);
                }
            }
        }
        return annotatedClasses;
    }
}
