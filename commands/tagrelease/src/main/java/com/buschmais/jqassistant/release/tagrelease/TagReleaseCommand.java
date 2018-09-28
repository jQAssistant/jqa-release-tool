package com.buschmais.jqassistant.release.tagrelease;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.core.ReleaseConfig;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.URIish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.List;

import static java.lang.String.format;

@SpringBootApplication(scanBasePackages = "com.buschmais.jqassistant.release")
public class TagReleaseCommand implements CommandLineRunner {

    RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TagReleaseCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(String... args) throws Exception {
        File config = new File("/Users/plexus/jqa-rel-tools/rconfig.yaml");

        FileReader r = new FileReader(config);
        Yaml y = new Yaml();
        var load = y.<List<ReleaseConfig>>load(r);


        for (ProjectRepository projectRepository : getRepositorySrv().getProjectRepositories()) {

            System.out.println(projectRepository.getHumanName());
            var path = projectRepository.getHumanName() + "/pom.xml";

            FileInputStream fis = new FileInputStream(path);
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(fis);
            String line = format("%s:%s", model.getGroupId(), model.getArtifactId());

            System.out.println(line);

            ReleaseConfig releaseConfig = load.stream().filter(rc -> rc.id.equals(line)).findFirst().get();
            String tagName = "REL-" + releaseConfig.releaseVersion;
            System.out.println(tagName);

            Git git = Git.open(new File(projectRepository.getHumanName()));

            git.tag()
               .setName(tagName).setMessage("Release of " + releaseConfig.name + " " + releaseConfig.releaseVersion)
               .call();

        }
    }


}
