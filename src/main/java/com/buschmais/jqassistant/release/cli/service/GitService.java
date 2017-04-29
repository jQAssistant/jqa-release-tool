package com.buschmais.jqassistant.release.cli.service;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class GitService {

    public void cloneRepository(GitURI uri, Path parentDir) throws GitAPIException {
        boolean isDirectory = Files.isDirectory(parentDir);
        boolean isWritable = Files.isWritable(parentDir);

        if (!(isDirectory && isWritable)) {
            throw new RuntimeException(parentDir.toString() + " must be a writable directory");
        }

        Path n = parentDir.resolve(UUID.randomUUID().toString());
        Git.cloneRepository()
            .setURI( "https://github.com/buschmais/jqa-uber-parent.git" )

            .setDirectory(n.toFile())
            .call();


        throw new RuntimeException("Not implement or look at me!");
    }


}
