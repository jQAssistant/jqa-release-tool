package com.buschmais.jqassistant.release.commitchanges;

import com.buschmais.jqassistant.release.core.RTException;
import com.buschmais.jqassistant.release.core.RTExceptionWrapper;
import com.buschmais.jqassistant.release.repository.RepositoryProviderService;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.boot.ansi.AnsiColor.*;
import static org.springframework.boot.ansi.AnsiStyle.BOLD;
import static org.springframework.boot.ansi.AnsiStyle.NORMAL;

@SpringBootApplication(scanBasePackages = "com.buschmais.jqassistant.release")
public class CommitChangesCommand implements ApplicationRunner {
    private static final String MESSAGE_OPTION = "message";

    private RepositoryProviderService repositorySrv;

    public RepositoryProviderService getRepositorySrv() {
        return repositorySrv;
    }

    @Autowired
    public void setRepositorySrv(RepositoryProviderService service) {
        this.repositorySrv = service;
    }

    public static void main(String[] args) {
        var app = new SpringApplication(CommitChangesCommand.class);
        app.setBannerMode(Banner.Mode.OFF);
        var run = app.run(args);
        var exitCode = SpringApplication.exit(run);

        System.exit(exitCode);
    }

    @Override
    public void run(ApplicationArguments arguments) throws Exception {

        var messageFile = getFile(arguments);
        var message = readMessageFromFile(messageFile);

        System.out.println(AnsiOutput.toString(BRIGHT_GREEN, "Commiting all local changes", DEFAULT));

        try {
            for (var projectRepository : getRepositorySrv().getProjectRepositories()) {
                String s = AnsiOutput.toString(AnsiColor.BRIGHT_YELLOW,
                                               "Committing changes in ",
                                               BOLD, AnsiColor.BRIGHT_YELLOW, "'",
                                               projectRepository.getHumanName(),
                                               "'", AnsiColor.DEFAULT);
                System.out.println(s);

                Git.open(new File(projectRepository.getHumanName()))
                   .add().setUpdate(true).addFilepattern(".").call();
                Git.open(new File(projectRepository.getHumanName()))
                   .commit().setCommitter("Dirk Mahler", "dirk.mahler@buschmais.com")
                   .setMessage(message)
                   .call();
            }
        } catch (RuntimeException e) {
            throw RTExceptionWrapper.WRAPPER.apply(e, () -> "Failed to commit changes in all projects");
        }
    }

    private String readMessageFromFile(File file) {
        var result = "";
        try {
            List<String> content = Files.readAllLines(file.toPath());
            result = content.stream().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RTException("Failed to read " + file.getPath());
        }

        return result;
    }

    private File getFile(ApplicationArguments arguments) {
        if (!arguments.containsOption(MESSAGE_OPTION)) {
            throw new RTException("Option --" + MESSAGE_OPTION + " is missing");
        }

        var values = arguments.getOptionValues(MESSAGE_OPTION);

        if (values.isEmpty()) {
            throw new RTException("Value for --" + MESSAGE_OPTION + " is missing.");
        }

        var value = values.get(0);
        var messageFile = new File(value);

        if (Files.isDirectory(messageFile.toPath())) {
            throw new RTException(value + " is a directory");
        }

        if (!Files.exists(messageFile.toPath())) {
            throw new RTException(value + " does not exist");
        }

        if (!Files.isReadable(messageFile.toPath())) {
            throw new RTException(value + " is not readable");
        }

        return messageFile;
    }
}
