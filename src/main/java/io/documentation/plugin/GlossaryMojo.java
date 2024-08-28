package io.documentation.plugin;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import com.github.javaparser.ast.body.MethodDeclaration;
import io.documentation.service.ExtractDoc;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

@Mojo( name = "glossary", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class GlossaryMojo extends AbstractMojo {
    @Parameter(property = "scan.annotation", required = true)
    private String annotationClassName;

    @Parameter(property = "scan.outputDirectory", defaultValue = "${project.build.outputDirectory}")
    private File outputDirectory;
    private ExtractDoc extractDoc;
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            List<Class<?>> annotatedClasses = extractDoc.scanForAnnotatedClasses(outputDirectory,"glossary");
            if (annotatedClasses.isEmpty()) {
                getLog().info("No classes with annotation " + annotationClassName + " found.");
            } else {
                for (Class<?> clazz : annotatedClasses) {
                    String javadoc = getClassJavadoc(clazz.get);
                    getLog().info("Class: " + clazz.getName());
                    getLog().info("JavaDoc: " + (javadoc != null ? javadoc : "No JavaDoc available"));
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error scanning classes", e);
        }

    }

    private String getClassJavadoc(File classe) {
        String filePath = "src/main/java/com/example/MyClass.java";

        try {
            // Parse le fichier Java
            CompilationUnit cu = StaticJavaParser.parse(classe);

            // Récupère la classe ou l'interface déclarée dans ce fichier
            ClassOrInterfaceDeclaration classOrInterface = cu.findFirst(ClassOrInterfaceDeclaration.class).orElse(null);

            if (classOrInterface != null) {
                // Affiche le nom de la classe
                System.out.println("Classe : " + classOrInterface.getNameAsString());

                // Récupère et affiche la Javadoc de la classe
                classOrInterface.getJavadoc().ifPresent(javadoc -> {
                    System.out.println("Description : " + javadoc.getDescription().toText());
                });

                // Pour chaque méthode, affiche son nom et sa Javadoc
                for (MethodDeclaration method : classOrInterface.getMethods()) {
                    System.out.println("Méthode : " + method.getNameAsString());
                    method.getJavadoc().ifPresent(javadoc -> {
                        System.out.println("  Description : " + javadoc.getDescription().toText());
                    });
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String  scanForAnnotatedClasses(File outputDirectory,String annotationClassName) throws ClassNotFoundException, IOException {
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
