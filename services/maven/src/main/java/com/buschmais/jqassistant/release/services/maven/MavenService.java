package com.buschmais.jqassistant.release.services.maven;

import org.apache.maven.shared.invoker.*;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@Service
public class MavenService {
    public static List<String> BUILD_ONLY_GOALS = Arrays.asList("clean", "install");
    public static String BUILD_ONLY_OPTS = "-DskipTests=true -Djqassistant.skip=true";
    public static String BUILD_ALL_OPTS = "-Dmaven.test.failure.ignore=false -Djqassistant.failOnSeverity=INFO";
    public static String RELEASE_BUILD_OPTS = BUILD_ALL_OPTS;


    private File ensureThatLocalRepositoryExists() {
        File lrd = new File("maven-local-repo");
        lrd.mkdirs();

        return lrd;
    }


    public void buildWithoutAnyTests(String directory) {
        File settings = new File("maven-settings.xml");
        File repo = ensureThatLocalRepositoryExists();

        File log = new LogfileNameBuilder().inDirectory("log")
                                           .withPrefix("jqa").withProjectName(directory)
                                           .withKeyword("simplebuild").withDate().build();


        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File(directory + "/pom.xml" ) );
        request.setGoals(BUILD_ONLY_GOALS);
        request.setMavenOpts(BUILD_ONLY_OPTS);
        request.setLocalRepositoryDirectory(repo);
        request.setUserSettingsFile(settings);
        request.setGlobalSettingsFile(settings);
        request.setUpdateSnapshots(false);
        request.setInteractive(false);
        request.setProfiles(Collections.emptyList());
        request.setJavaHome(new File("/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home"));
        //request.setProperties()


        Invoker invoker = new DefaultInvoker();

        try (var bl = new LogWriter(log)){
            bl.start();
            request.setOutputHandler(bl);
            request.setErrorHandler(bl);
            InvocationResult result = invoker.execute(request);
            if (0 != result.getExitCode()) {
                bl.stop();
                String s = AnsiOutput.toString(AnsiColor.BRIGHT_RED,
                                               "Maven build failed for ",
                                               BOLD, AnsiColor.BRIGHT_RED, "'",
                                               directory,
                                               NORMAL, "'", AnsiColor.DEFAULT);

                System.out.println(s);

                s = AnsiOutput.toString(AnsiColor.BRIGHT_RED,
                                        "Check the log file at ",
                                        BOLD, AnsiColor.BRIGHT_RED, "'",
                                        log.toString(),
                                        NORMAL, "'", AnsiColor.DEFAULT);

                System.out.println(s);
                System.exit(1);
            }
        } catch (MavenInvocationException|IOException e) {
            e.printStackTrace();
            //
            throw new RuntimeException(e);
        }

    }

    public void buildWithAllTests(String directory) {
        File settings = new File("maven-settings.xml");
        File repo = ensureThatLocalRepositoryExists();

        File log = new LogfileNameBuilder().inDirectory("log")
                                           .withPrefix("jqa").withProjectName(directory)
                                           .withKeyword("fullbuild").withDate().build();
        // TODO: 01.07.18 class path resource verwenden
        InputStream is = MavenService.class.getResourceAsStream("/settings.xml");

        log.getParentFile().mkdirs();

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File(directory + "/pom.xml" ) );
        request.setGoals(BUILD_ONLY_GOALS);
        request.setMavenOpts(BUILD_ALL_OPTS);
        request.setLocalRepositoryDirectory(repo);
        request.setUserSettingsFile(settings);
        request.setGlobalSettingsFile(settings);
        request.setUpdateSnapshots(false);
        request.setInteractive(false);
        request.setProfiles(List.of("IT"));
        request.setJavaHome(new File("/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home"));

        Invoker invoker = new DefaultInvoker();
        try (var bl = new LogWriter(log)){
            bl.start();
            request.setOutputHandler(bl);
            request.setErrorHandler(bl);
            InvocationResult result = invoker.execute(request);
            if (0 != result.getExitCode()) {
                bl.stop();
                String s = AnsiOutput.toString(AnsiColor.BRIGHT_RED,
                                               "Maven build failed for ",
                                               BOLD, AnsiColor.BRIGHT_RED, "'",
                                               directory,
                                               NORMAL, "'", AnsiColor.DEFAULT);

                System.out.println(s);

                s = AnsiOutput.toString(AnsiColor.BRIGHT_RED,
                                        "Check the log file at ",
                                        BOLD, AnsiColor.BRIGHT_RED, "'",
                                        log.toString(),
                                        NORMAL, "'", AnsiColor.DEFAULT);

                System.out.println(s);
                System.exit(1);
            }
        } catch (MavenInvocationException|IOException e) {
            e.printStackTrace();
            //
            throw new RuntimeException(e);
        }
    }

    public void buildRelease(String directory) {
        File settings = new File("maven-settings.xml");
        File repo = ensureThatLocalRepositoryExists();

        Properties properties = new Properties();

        try (var is = new FileInputStream("gpg.properties")) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var id = properties.get("gpg.keyid");
        var pp = properties.get("gpg.passphrase");
        var s1 = String.format(" -Dgpg.keyname=%s -Dgpg.passphrase=%s", id, pp);

        File log = new LogfileNameBuilder().inDirectory("log")
                                           .withPrefix("jqa").withProjectName(directory)
                                           .withKeyword("releasebuild").withDate().build();
        log.getParentFile().mkdirs();

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File(directory + "/pom.xml" ) );
        request.setGoals(BUILD_ONLY_GOALS);
        request.setMavenOpts(RELEASE_BUILD_OPTS + s1);
        request.setLocalRepositoryDirectory(repo);
        request.setUserSettingsFile(settings);
        request.setGlobalSettingsFile(settings);
        request.setUpdateSnapshots(false);
        request.setInteractive(false);
        request.setProfiles(List.of("IT", "release"));
        request.setJavaHome(new File("/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home"));

        Invoker invoker = new DefaultInvoker();
        try (var bl = new LogWriter(log)){
            bl.start();
            request.setOutputHandler(bl);
            request.setErrorHandler(bl);
            InvocationResult result = invoker.execute(request);
            if (0 != result.getExitCode()) {
                bl.stop();
                String s = AnsiOutput.toString(AnsiColor.BRIGHT_RED,
                                               "Maven release build failed for ",
                                               BOLD, AnsiColor.BRIGHT_RED, "'",
                                               directory,
                                               NORMAL, "'", AnsiColor.DEFAULT);

                System.out.println(s);

                s = AnsiOutput.toString(AnsiColor.BRIGHT_RED,
                                        "Check the log file at ",
                                        BOLD, AnsiColor.BRIGHT_RED, "'",
                                        log.toString(),
                                        NORMAL, "'", AnsiColor.DEFAULT);

                System.out.println(s);
                System.exit(1);
            }
        } catch (MavenInvocationException|IOException e) {
            e.printStackTrace();
            //
            throw new RuntimeException(e);
        }

    }
}
