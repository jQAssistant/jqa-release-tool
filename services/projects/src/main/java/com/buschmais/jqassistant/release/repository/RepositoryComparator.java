package com.buschmais.jqassistant.release.repository;

import com.buschmais.jqassistant.release.core.ProjectRepository;

import java.util.Comparator;

public class RepositoryComparator implements Comparator<ProjectRepository> {
    @Override
    public int compare(ProjectRepository left, ProjectRepository right) {
        if (left.getBuildOrder() < right.getBuildOrder()) {
            return -1;
        } else if (left.getBuildOrder() > right.getBuildOrder()) {
            return 1;
        }

        return 0;
    }
}
