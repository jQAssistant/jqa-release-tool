package com.buschmais.jqassistant.release.services.maven;

import org.apache.maven.shared.invoker.*;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@Service
public class MavenService {
    public static List<String> BUILD_ONLY_GOALS = Arrays.asList("clean", "install");
    public static String BUILD_ONLY_OPTS = "-DskipTests=true -Djqassistant.skip=true";
    public static String BUILD_ALL_OPTS = "-Dmaven.test.failure.ignore=false -Djqassistant.failOnSeverity=INFO";

    public void buildWithoutAnyTests(String directory) {
        // TODO: 23.06.18 class path resource verwenden
        // TODO: 01.07.18 Add logfile support
        System.out.println();
        InputStream is = MavenService.class.getResourceAsStream("/settings.xml");

        File of = new File("maven-settings.xml");

        try (FileOutputStream fos = new FileOutputStream(of)) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        File lrd = new File("maven-local-repo");
        lrd.mkdirs();

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File(directory + "/pom.xml" ) );
        request.setGoals(BUILD_ONLY_GOALS);
        request.setMavenOpts(BUILD_ONLY_OPTS);
        request.setLocalRepositoryDirectory(lrd);
        request.setUserSettingsFile(of);
        request.setGlobalSettingsFile(of);
        request.setUpdateSnapshots(false);
        request.setInteractive(false);
        request.setProfiles(Collections.emptyList());
        request.setJavaHome(new File("/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home"));
        //request.setProperties()


        Invoker invoker = new DefaultInvoker();
        try {
            InvocationResult result = invoker.execute(request);
            if (0 != result.getExitCode()) {
                System.exit(1);
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
            //
            throw new RuntimeException(e);
        }
    }

    public void buildWithAllTests(String directory) {
        // TODO: 01.07.18 class path resource verwenden
        InputStream is = MavenService.class.getResourceAsStream("/settings.xml");

        File of = new File("maven-settings.xml");
        File log = new File("log/"+directory + "-with-all-" + getLogSuffix() + ".log");

        log.getParentFile().mkdirs();

        try (var fos = new FileOutputStream(of)) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        File lrd = new File("maven-local-repo");
        lrd.mkdirs();

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File(directory + "/pom.xml" ) );
        request.setGoals(BUILD_ONLY_GOALS);
        request.setMavenOpts(BUILD_ALL_OPTS);
        request.setLocalRepositoryDirectory(lrd);
        request.setUserSettingsFile(of);
        request.setGlobalSettingsFile(of);
        request.setUpdateSnapshots(false);
        request.setInteractive(false);
        request.setProfiles(List.of("IT"));
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

    private String getLogSuffix() {
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");

        String suffix = formatter.format(now);

        return suffix;

    }
}
