package com.buschmais.jqassistant.release.showconfig;

import com.buschmais.jqassistant.release.core.ReleaseConfig;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import static java.lang.String.format;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.showconfig"
})
public class ShowConfigCommand implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ShowConfigCommand.class);
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

        load.forEach(rc -> {
            var l2 = format("%-40s : %-15s -> %-5s -> %-15s",
                            rc.name, rc.currentVersion,
                            rc.releaseVersion, rc.nextVersion);
            System.out.println(l2);
        });
    }
}
