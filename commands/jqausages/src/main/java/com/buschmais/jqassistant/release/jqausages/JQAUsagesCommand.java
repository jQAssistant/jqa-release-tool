package com.buschmais.jqassistant.release.jqausages;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import com.buschmais.jqassistant.release.services.maven.MavenService;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository",
    "com.buschmais.jqassistant.release.services.maven"
})
public class JQAUsagesCommand implements CommandLineRunner {

    private RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(JQAUsagesCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext run = app.run(args);
        int exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(String... args) throws Exception {
        Set<ProjectRepository> projects = getRepositorySrv().getProjectRepositories();


        try {
            for (ProjectRepository p : projects) {

                FileInputStream fis = new FileInputStream("/Users/plexus/pom.xml");
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(fis);


                String s = AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW,
                                               "About to run analyse dependencies of ",
                                               BOLD, AnsiColor.BRIGHT_YELLOW, "'",
                                               p.getName(),
                                               NORMAL, "'", AnsiColor.DEFAULT);
                System.out.println(s);
                String directory = p.getHumanName() + "/pom.xml";
                File inf = new File(directory);
//                System.out.println(directory);

//                System.out.println(inf.exists());
                try (var f = new FileInputStream(inf)) {
                    MavenXpp3Reader r = new MavenXpp3Reader();
                    Model m = r.read(f);
                    List<List<Dependency>> dod = new LinkedList<>();

                    List<Dependency> dependencies = Optional.ofNullable(m.getDependencyManagement())
                                                            .orElseGet(DependencyManagement::new)
                                                            .getDependencies();
                    List<Dependency> dependencies1 = Optional.ofNullable(m.getDependencies())
                                                             .orElseGet(Collections::emptyList);

                    dod.add(dependencies);
                    dod.add(dependencies1);

                    dod.stream().flatMap(
                        (Function<List<Dependency>, Stream<Dependency>>) Collection::stream)
                       .filter(d -> d.getGroupId().startsWith("com.buschmais.jqassistant"))
                       .forEach(new Consumer<Dependency>() {
                        @Override
                        public void accept(Dependency o) {
                            System.out.println(o.getGroupId() +":" + o.getArtifactId()+
                            ":" + o.getType() + ":" + o.getVersion());

                        }
                    });
                }




            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
