package com.buschmais.jqassistant.release.cli.service;

import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class GitServiceIT {

    GitService service = new GitService();

    @Test
    public void gitServiceCanCloneRepository() throws Exception {
        Path parentDir = FileSystems.getDefault().getPath("/tmp");

        System.out.println(parentDir);

        GitURI uri = new GitURI("https://github.com/buschmais/jqa-uber-parent.git");

        service.cloneRepository(uri, parentDir);
    }

}