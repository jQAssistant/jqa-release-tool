package com.buschmais.jqassistant.release.pushchanges;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.URIish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;

@SpringBootApplication(scanBasePackages = "com.buschmais.jqassistant.release")
public class PushChangesCommand implements CommandLineRunner {

    RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PushChangesCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(String... args) throws Exception {
        for (ProjectRepository projectRepository : getRepositorySrv().getProjectRepositories()) {

            System.out.println(projectRepository.getHumanName());

            String remote = "gh";
            String branch = "refs/heads/master";
            String trackingBranch = "refs/remotes/" + remote + "/master";

            Iterable<PushResult> gh = Git.open(new File(projectRepository.getHumanName()))
                                         .push().setRemote(remote).setOutputStream(System.out)
                                         //.setPushTags()

                                         .call();

            for (PushResult result : gh) {
                System.out.println(result.getURI().getHumanishName());
            }
        }
    }


}
