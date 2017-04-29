package com.buschmais.jqassistant.release.cli.service;

import java.net.URI;
import java.net.URISyntaxException;

public class GitURI {

    private final URI repositoryURI;

    public GitURI(String uri) throws URISyntaxException {
        this(new URI(uri));
    }

    public GitURI(URI uri) {
        repositoryURI = uri;
    }

    public URI toURI() {
        return repositoryURI;
    }
}
