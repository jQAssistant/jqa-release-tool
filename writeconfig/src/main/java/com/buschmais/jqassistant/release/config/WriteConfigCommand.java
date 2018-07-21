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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

            System.out.println(path);
            FileInputStream fis = new FileInputStream(path);
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(fis);
            System.out.println(model.getVersion());

            ProjectVersion dav = new ProjectVersion(model.getVersion());

            var id = model.getGroupId() + ":" + model.getArtifactId();
            var c = new ReleaseConfig();
            c.id = id;
            c.name = model.getName();
            c.currentVersion = dav.toString();
            c.releaseVersion = dav.getReleaseVersionString();
            c.nextVersion = dav.getNextVersion().toString();

            System.out.println(model.getName());
            ((LinkedList<ReleaseConfig>) rc).addLast(c);
        }

        try (FileWriter writer = new FileWriter(config)) {
            yaml.dump(rc, writer);
        }

        System.out.println("Wrote " + config.getAbsolutePath());
        System.out.println("You can edit now the file if you want.");
        System.out.println("Use showconfig to display the effective configuration.");
    }
}
