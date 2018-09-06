package com.buschmais.jqassistant.release.services.maven;

import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.cli.Commandline;
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
import java.util.stream.Collectors;

import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@Service
public class MavenService {
    private File ensureThatLocalRepositoryExists() {
        File lrd = new File("maven-local-repo");
        lrd.mkdirs();

        return lrd;
    }

    public void doRequest(MavenRequest request) {
        var settings = new File("maven-settings.xml");
        var workingDirectory = request.getWorkingDir();
        var repo = ensureThatLocalRepositoryExists();

        File log = new LogfileNameBuilder().inDirectory("log")
                                           .withPrefix("jqa").withProjectName(request.getWorkingDir())
                                           .withKeyword("releasebuild").withDate().build();
        log.getParentFile().mkdirs();

        InvocationRequest ir = new DefaultInvocationRequest();
        ir.setPomFile( new File(workingDirectory + "/pom.xml" ) );
        ir.setGoals(request.getGoals());
        ir.setMavenOpts(String.join(" ", request.getParameters()));
        ir.setLocalRepositoryDirectory(repo);
        ir.setUserSettingsFile(settings);
        ir.setGlobalSettingsFile(settings);
        ir.setUpdateSnapshots(false);
        ir.setInteractive(false);
        ir.setProfiles(request.getProfiles());
        ir.setJavaHome(new File("/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home"));

      //  MavenCommandLineBuilder mavenCommandLineBuilder = new MavenCommandLineBuilder();
      //  Commandline build = null;
      //  try {
      //      build = mavenCommandLineBuilder.build(ir);
      //  } catch (CommandLineConfigurationException e) {
      //      e.printStackTrace();
      //  }
      // System.out.println(build);
        Invoker invoker = new DefaultInvoker();
        try (var bl = new LogWriter(log)){
            bl.start();
            ir.setOutputHandler(bl);
            ir.setErrorHandler(bl);
            InvocationResult result = invoker.execute(ir);
            if (0 != result.getExitCode()) {
                bl.stop();
                String s = AnsiOutput.toString(AnsiColor.BRIGHT_RED,
                                               "Maven build failed for ",
                                               BOLD, AnsiColor.BRIGHT_RED, "'",
                                               request.getWorkingDir(),
                                               NORMAL, "'", AnsiColor.DEFAULT);

                System.out.println(s);

                s = AnsiOutput.toString(AnsiColor.BRIGHT_RED,
                                        "Check the log file at ",
                                        BOLD, AnsiColor.BRIGHT_RED, "'",
                                        log.toString(),
                                        NORMAL, "'", AnsiColor.DEFAULT);

                System.out.println(s);
                //System.exit(1);
            }
        } catch (MavenInvocationException|IOException e) {
            e.printStackTrace();
            //
            throw new RuntimeException(e);
        }
    }
}
