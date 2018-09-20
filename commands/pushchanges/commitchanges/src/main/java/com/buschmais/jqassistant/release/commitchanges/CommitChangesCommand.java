package com.buschmais.jqassistant.release.checkout;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
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

import java.io.File;
import java.util.Collection;
import java.util.UUID;

@SpringBootApplication(scanBasePackages = "com.buschmais.jqassistant.release")
public class CommitChangesCommand implements CommandLineRunner {

    RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CommitChangesCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(String... args) throws Exception {
        for (ProjectRepository projectRepository : getRepositorySrv().getProjectRepositories()) {
            URIish u = new URIish(projectRepository.getRepositoryURL());
            System.out.println(projectRepository.getHumanName());

            Git git = Git.open(new File(projectRepository.getHumanName()));

            git.add().setUpdate(true).addFilepattern(".").call();
            Git.open(new File(projectRepository.getHumanName()))
               .commit().setCommitter("Oliver B. Fischer", "o.b.fischer@swe-blog.net")
               .setMessage("DAS IST EIN TEST")
               .call();
        }
    }


}
