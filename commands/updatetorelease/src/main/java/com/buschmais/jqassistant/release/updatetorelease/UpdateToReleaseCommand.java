package com.buschmais.jqassistant.release.updatetorelease;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import com.buschmais.jqassistant.release.core.ReleaseConfig;
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
import org.springframework.context.annotation.ImportResource;
import org.w3c.dom.Document;
import org.yaml.snakeyaml.Yaml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;
import java.util.Set;

import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.repository",
    "com.buschmais.jqassistant.release.services.maven"
})
@ImportResource({"classpath:beans.xml", "classpath:project-version-updaters.xml"})
public class UpdateToReleaseCommand implements CommandLineRunner {

    @Autowired
    List<VersionUpdate> updaters;

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
        File config = new File("/Users/plexus/jqa-rel-tools/rconfig.yaml");

        FileReader r = new FileReader(config);
        Yaml y = new Yaml();
        var load = y.<List<ReleaseConfig>>load(r);


        updaters.forEach(updater -> {
            var rco = load.stream().filter(x -> x.id.equals(updater.getId())).findFirst();
            var rc = rco.orElseThrow();

            updater.setNextVersion(rc.releaseVersion);
        });

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

        System.out.println("Backup " + in.toString() + " -> " + of.toString());
        return of;
    }

    private void setReleaseVersions(ProjectRepository projectRepository) {
        String s = AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW,
                                       "About to update version information of ",
                                       BOLD, AnsiColor.BRIGHT_YELLOW, "'",
                                       projectRepository.getName(),
                                       NORMAL, AnsiColor.BRIGHT_YELLOW,
                                       "' to the next release.", AnsiColor.DEFAULT);
        //System.out.println(s);
        String directory = projectRepository.getHumanName();
        File original = makeBackUpOfPom(directory);

        DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();

        try {
            File f = original;
            DocumentBuilder builder = factory2.newDocumentBuilder();
            Document document = builder.parse(f);
            DOMSource source = new DOMSource(document);

            for (VersionUpdate updater : updaters) {
                DOMResult dr = updater.update(source);
                source = new DOMSource(dr.getNode());
            }

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            Document res = (Document) source.getNode();
            var source2 =  new DOMSource(res);
            transformer.transform(source2, new StreamResult(new File(directory, "pom.xml")));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }



    }
}
