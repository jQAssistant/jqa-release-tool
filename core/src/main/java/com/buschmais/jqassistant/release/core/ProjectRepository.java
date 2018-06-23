package com.buschmais.jqassistant.release.core;

import org.eclipse.jgit.transport.URIish;

import java.net.URISyntaxException;
import java.util.Objects;

public class ProjectRepository {
    private int buildOrder;
    private String name;
    private String repositoryURL;

    public int getBuildOrder() {
        return buildOrder;
    }

    public void setBuildOrder(int buildOrder) {
        this.buildOrder = buildOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepositoryURL() {
        return repositoryURL;
    }

    public void setRepositoryURL(String url) {
        this.repositoryURL = url;
    }

    @Override
    public String toString() {
        return "ProjectRepository{name='" + name + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectRepository that = (ProjectRepository) o;
        return buildOrder == that.buildOrder &&
            Objects.equals(name, that.name) &&
            Objects.equals(repositoryURL, that.repositoryURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildOrder, name, repositoryURL);
    }

    public String getHumanName() {
        try {
            return new URIish(getRepositoryURL()).getHumanishName();
        } catch (URISyntaxException e) {
            // todo
        }

        return null;
    }
}
