package com.buschmais.jqassistant.release.updatetorelease;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import com.buschmais.jqassistant.release.services.maven.MavenService;
import com.buschmais.jqassistant.release.updatetorelease.updates.UpdateCoreFrameworkVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.*;
import java.util.Set;

import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository",
    "com.buschmais.jqassistant.release.services.maven"
})
public class UpdateToReleaseCommand implements CommandLineRunner {

    RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(UpdateToReleaseCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(String... args) throws Exception {
        Set<ProjectRepository> projects = getRepositorySrv().getProjectRepositories();

        projects.forEach(this::setReleaseVersions);

    }

    public File makeBackUpOfPom(String directory) {
        File in = new File(directory, "pom.xml");
        File of = new File(directory, "pom.xml.org");

        try (var fos = new FileOutputStream(of); var fis = new FileInputStream(in)) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return of;
    }

    private void setReleaseVersions(ProjectRepository projectRepository) {
        String s = AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW,
                                       "About to update version information of ",
                                       BOLD, AnsiColor.BRIGHT_YELLOW, "'",
                                       projectRepository.getName(),
                                       NORMAL, AnsiColor.BRIGHT_YELLOW,
                                       "' to the next release.", AnsiColor.DEFAULT);
        System.out.println(s);
        String directory = projectRepository.getHumanName();
        File original = makeBackUpOfPom(directory);

        UpdateCoreFrameworkVersion u = new UpdateCoreFrameworkVersion();

        try {
            u.update(original, new File(directory, "pom.xml"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }
}
