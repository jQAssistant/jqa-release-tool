package com.buschmais.jqassistant.release.cli.command;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.UUID;

@Component
public class CheckoutCommand implements CommandMarker {

    @CliCommand("checkout")
    public String checkout() {
        try {
            File directory = new File("/tmp/" + UUID.randomUUID());
            Git.cloneRepository()
               .setURI( "https://github.com/buschmais/jqa-uber-parent.git" )
               .setDirectory(directory)
               .call();
            return directory.toString();
        }
        catch (GitAPIException e) {
            e.printStackTrace();
        }

        return "???";
    }
}
