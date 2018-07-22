package com.buschmais.jqassistant.release.repository;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class RepositoryProviderService {

    private Set<ProjectRepository> projectRepositories;

    @PostConstruct
    private void loadRepositories() {
        ClassPathResource resource = new ClassPathResource("projects.yaml");

        try (InputStream is = resource.getInputStream()) {
            Yaml yaml = new Yaml();
            Iterable<Object> documents = yaml.loadAll(is);

            Supplier<Set<ProjectRepository>> setSupplier = () -> new TreeSet<>(new RepositoryComparator());

            Set<ProjectRepository> set = StreamSupport.stream(documents.spliterator(), false)
                                                      .map(Map.class::cast)
                                                      .map(m -> {
                                                          ProjectRepository rp = new ProjectRepository();
                                                          rp.setName((String) m.get("name"));
                                                          rp.setRepositoryURL((String) m.get("scmurl"));
                                                          rp.setBuildOrder((int) m.get("order"));
                                                          return rp;
                                                      })
                                                      .collect(Collectors.toCollection(setSupplier));

            setProjectRepository(set);
        } catch (IOException ioe) {

            System.out.println(ioe);
            System.exit(1);
        }
    }

    public Set<ProjectRepository> getProjectRepositories() {
        return projectRepositories;
    }

    private void setProjectRepository(Set<ProjectRepository> set) {
        projectRepositories = set;
    }

    public Optional<ProjectRepository> findById(String id) {
        return projectRepositories.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

}
