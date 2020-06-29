package com.buschmais.jqassistant.release.services.maven;

import com.buschmais.jqassistant.release.core.Environment;
import com.buschmais.jqassistant.release.core.RTException;
import org.apache.maven.shared.invoker.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

import static java.lang.String.format;
import static org.springframework.boot.ansi.AnsiColor.BRIGHT_RED;
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@Service
public class MavenService {

    private com.buschmais.jqassistant.release.core.Environment environment;

    public Environment getEnvironment() {
        return environment;
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private File ensureThatLocalRepositoryExists() {
        // todo Directory should be determined by the settings.xml
        var repoDir = new File("maven-local-repo");
        repoDir.mkdirs();

        return repoDir;
    }

    public void doRequest(MavenRequest request) throws IOException, MavenInvocationException {
        var settings = new File("maven-settings.xml");
        var workingDirectory = request.getWorkingDir();
        var repo = ensureThatLocalRepositoryExists();
        var mavenHome = getHomeValueOrAbort("RT_MAVEN_HOME");
        var javaHome = getHomeValueOrAbort("RT_JAVA_HOME");


        // todo Keyword in the name of the log file should reflect the actual command
        var log = new LogfileNameBuilder().inDirectory("log")
                                          .withPrefix("jqa").withProjectName(request.getWorkingDir())
                                          .withKeyword("releasebuild").withDate().build();
        log.getParentFile().mkdirs();

        var ir = new DefaultInvocationRequest();
        var invoker = new DefaultInvoker();

        ir.setPomFile( new File(workingDirectory + "/pom.xml" ) );
        ir.setGoals(request.getGoals());
        ir.setMavenOpts(String.join(" ", request.getParameters()));
        ir.setLocalRepositoryDirectory(repo);
        ir.setUserSettingsFile(settings);
        ir.setGlobalSettingsFile(settings);
        ir.setUpdateSnapshots(false);
        ir.setInteractive(false);
        ir.setProfiles(request.getProfiles());
        ir.setJavaHome(new File(javaHome));
        invoker.setMavenHome(new File(mavenHome));

        try (var bl = new LogWriter(log)) {
            bl.start();
            ir.setOutputHandler(bl);
            ir.setErrorHandler(bl);

            var result = invoker.execute(ir);

            if (0 != result.getExitCode()) {
                bl.stop();
                var s = AnsiOutput.toString(BRIGHT_RED, "Maven build failed for '", BOLD, BRIGHT_RED,
                                            request.getWorkingDir(), NORMAL, "'", AnsiColor.DEFAULT);

                System.out.println(s);

                s = AnsiOutput.toString(BRIGHT_RED, "Check the log file at '", BOLD, BRIGHT_RED,
                                        log.toString(), NORMAL, "'", AnsiColor.DEFAULT);

                System.out.println(s);
            }
        }
    }

    private String getHomeValueOrAbort(String variableName) {
        var variablePresent = getEnvironment().isVariableSet(variableName);

        if (!variablePresent) {
            var message = format("Environment variable %s is not set", variableName);
            throw new RTException(message);
        }

        return getEnvironment().getVariable(variableName);
    }
}
