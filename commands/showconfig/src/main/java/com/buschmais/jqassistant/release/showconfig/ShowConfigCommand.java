package com.buschmais.jqassistant.release.showconfig;

import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.core.ReleaseConfig;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.boot.ansi.AnsiColor.*;

@SpringBootApplication(scanBasePackages = {
    "com.buschmais.jqassistant.release.core",
    "com.buschmais.jqassistant.release.showconfig"
})
public class ShowConfigCommand implements ApplicationRunner {
    private static final String VERSION_CONFIG_FILE = "rconfig.yaml";

    public static void main(String[] args) {
        var app = new SpringApplication(ShowConfigCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        var run = app.run(args);
        var exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments __) throws Exception {
        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Showing the configured versions for the release", DEFAULT));

        try {
            var configFile = new File(VERSION_CONFIG_FILE);

            try (var fileReader = new FileReader(configFile)) {
                var versionConfig = new Yaml();
                var releaseConfigs = versionConfig.<List<ReleaseConfig>>load(fileReader);

                releaseConfigs.forEach(rc -> {
                    var line = format("%-40s : %-15s -> %-5s -> %-15s",
                                      rc.name, rc.currentVersion,
                                      rc.releaseVersion, rc.nextVersion);
                    System.out.println(line);
                });
            }
        } catch (Exception e) {
            throw RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to show the configured versions for the release");
        }
    }
}
