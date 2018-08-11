package com.buschmais.jqassistant.release.config;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.core.ProjectVersion;
import com.buschmais.jqassistant.release.core.ReleaseConfig;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.transport.URIish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository"
})
public class WriteConfigCommand implements CommandLineRunner {

    RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(WriteConfigCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    public File makeLocalCopyOfMavenSettings() {
        // TODO: 23.06.18 class path resource verwenden
        InputStream is = WriteConfigCommand.class.getResourceAsStream("/settings.xml");

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

        return of;
    }


    @Override
    public void run(String... args) throws Exception {
        File config = new File("rconfig.yaml");

        ReleaseConfig a = new ReleaseConfig();
        a.currentVersion = "a";
        a.nextVersion = "b";
        a.releaseVersion = "c";

        ReleaseConfig b = new ReleaseConfig();
        b.currentVersion = "a";
        b.nextVersion = "b";
        b.releaseVersion = "c";

        DumperOptions options = new DumperOptions();
        options.setCanonical(false);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        List<ReleaseConfig> rc = new LinkedList<>();
        Set<ProjectRepository> projects = getRepositorySrv().getProjectRepositories();

        for (ProjectRepository p : projects) {
            var path = p.getHumanName() + "/pom.xml";

            //System.out.println(path);
            FileInputStream fis = new FileInputStream(path);
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(fis);
            //System.out.println(model.getVersion());

            ProjectVersion dav = new ProjectVersion(model.getVersion());

            var id = model.getGroupId() + ":" + model.getArtifactId();
            var c = new ReleaseConfig();
            c.id = id;
            c.name = model.getName();
            c.currentVersion = dav.toString();
            c.releaseVersion = dav.getReleaseVersionString();
            c.nextVersion = dav.getNextVersion().toString();

            //System.out.println(model.getName());
            ((LinkedList<ReleaseConfig>) rc).addLast(c);
        }

        try (FileWriter writer = new FileWriter(config)) {
            yaml.dump(rc, writer);
        }

        makeLocalCopyOfMavenSettings();
        Properties properties = new Properties();
        properties.put("gpg.keyid", "...");
        properties.put("gpg.passphrase", "...");


        try (OutputStream os = new FileOutputStream("gpg.properties")) {
            properties.store(os, "# noop");
        }

        System.out.println("Wrote gpg.properties");
        System.out.println("Please provide your GPG key and passphrase.");
        System.out.println();
        System.out.println("Wrote maven-settings.xml for Maven.");
        System.out.println("Please provide your credentials for the deployment to OSS Sonatype.");
        System.out.println();
        System.out.println("Wrote " + config.getAbsolutePath());
        System.out.println("You can edit now the configuration file if you want.");
        System.out.println("Use showconfig to display the effective configuration.");
    }
}
