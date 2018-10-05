package com.buschmais.jqassistant.release.config;

import com.buschmais.jqassistant.release.core.ProjectVersion;
import com.buschmais.jqassistant.release.core.RTException;
import com.buschmais.jqassistant.release.core.ReleaseConfig;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

import static org.springframework.boot.ansi.AnsiColor.*;
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository"
})
public class WriteConfigCommand implements ApplicationRunner {
    private static final String GPG_PROP_FILE = "gpg.properties";
    private static final String MAVEN_SETTING_FILE = "maven-settings.xml";
    private static final String VERSION_CONFIG_FILE = "rconfig.yaml";

    private RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        var app = new SpringApplication(WriteConfigCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        var run = app.run(args);
        var exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    public void makeLocalCopyOfMavenSettings() {
        // TODO: 23.06.18 class path resource verwenden
        InputStream is = WriteConfigCommand.class.getResourceAsStream("/settings.xml");

        File of = new File(MAVEN_SETTING_FILE);

        try (FileOutputStream fos = new FileOutputStream(of)) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            var rt = new RTException("Failed to write " + MAVEN_SETTING_FILE, e, true, false);
            throw rt;
        }

        String s1 = AnsiOutput.toString(BRIGHT_YELLOW, "Wrote Maven settings file '",
                                        BOLD, MAVEN_SETTING_FILE, NORMAL, "'", DEFAULT);

        String s2 = AnsiOutput.toString(BRIGHT_YELLOW, "Enter your credentials for the deployment ",
                                        "to OSS Sonatype in this file.", DEFAULT);

        System.out.println(s1);
        System.out.println(s2);
        System.out.println();
    }


    @Override
    public void run(ApplicationArguments __) throws Exception {
        makeLocalCopyOfMavenSettings();
        writeGPGPropertiesFile();
        generateVersionConfig();

        var mf = AnsiOutput.toString(BRIGHT_YELLOW, "Use the command showconfig to display the ",
                                     "effective configuration.", DEFAULT);

        System.out.println(mf);
    }

    private void generateVersionConfig() {
        var config = new File(VERSION_CONFIG_FILE);

        var options = new DumperOptions();
        options.setCanonical(false);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        var yaml = new Yaml(options);

        var rc = new LinkedList<>();
        var projects = getRepositorySrv().getProjectRepositories();

        try {
            for (var project : projects) {
                var path = project.getHumanName() + "/pom.xml";

                try (var fis = new FileInputStream(path)) {
                    var reader = new MavenXpp3Reader();
                    var model = reader.read(fis);
                    var dav = new ProjectVersion(model.getVersion());
                    var id = model.getGroupId() + ":" + model.getArtifactId();
                    var releaseConfig = new ReleaseConfig();

                    releaseConfig.id = id;
                    releaseConfig.name = model.getName();
                    releaseConfig.currentVersion = dav.toString();
                    releaseConfig.releaseVersion = dav.getReleaseVersionString();
                    releaseConfig.nextVersion = dav.getNextVersion().toString();

                    rc.addLast(releaseConfig);
                }
            }

            try (var writer = new FileWriter(config)) {
                yaml.dump(rc, writer);
            }
        } catch (IOException | XmlPullParserException | VersionParseException e) {
            var rt = new RTException("Failed to generate and write " + VERSION_CONFIG_FILE, e, true, false);
            throw rt;
        }


        var s1 = AnsiOutput.toString(BRIGHT_YELLOW, "Wrote version configuration file '",
                                     BOLD, VERSION_CONFIG_FILE, NORMAL, "'", DEFAULT);
        var s2 = AnsiOutput.toString(BRIGHT_YELLOW, "Check the computed version information and ",
                                     "change them if needed.", DEFAULT);

        System.out.println(s1);
        System.out.println(s2);
        System.out.println();
    }

    private void writeGPGPropertiesFile() throws IOException {
        Properties properties = new Properties();
        properties.put("gpg.keyid", "...");
        properties.put("gpg.passphrase", "...");

        try (OutputStream os = new FileOutputStream(GPG_PROP_FILE)) {
            properties.store(os, "# noop");
        } catch (IOException e) {
            var rt = new RTException("Failed to write " + GPG_PROP_FILE, e, true, false);
            throw rt;
        }

        var s1 = AnsiOutput.toString(BRIGHT_YELLOW, "Wrote configuration file '",
                                     BOLD, BRIGHT_YELLOW, GPG_PROP_FILE,
                                     NORMAL, "'", DEFAULT);
        var s2 = AnsiOutput.toString(BRIGHT_YELLOW, "Enter your GPG key and passphrase ",
                                     "in this file.", DEFAULT);


        System.out.println(s1);
        System.out.println(s2);
        System.out.println();
    }
}
