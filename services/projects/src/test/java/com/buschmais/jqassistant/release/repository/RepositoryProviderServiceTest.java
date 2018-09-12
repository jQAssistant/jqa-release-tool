package com.buschmais.jqassistant.release.repository;

import com.buschmais.jqassistant.release.core.ProjectRepository;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest()
class RepositoryProviderServiceTest {
    @Autowired
    private RepositoryProviderService service;

    public static String EXPECTED_NAME = "jQA Ueber Parent";
    public static String EXPECTED_URL = "git@github.com:jqara/jqa-uber-parent.git";
    public static int EXPECTED_BUILD_ORDER = 0;

    @Test
    void canLoadListOfJQAProjects() {
        var expected = new ProjectRepository();

        expected.setBuildOrder(EXPECTED_BUILD_ORDER);
        expected.setName(EXPECTED_NAME);
        expected.setRepositoryURL(EXPECTED_URL);

        var projectRepositories = service.getProjectRepositories();

        assertThat(projectRepositories).isNotNull()
                                       .isNotEmpty()
                                       .first()
                                       .isEqualTo(expected);
    }

    @Test
    void loadedProjectIsConfiguredProperly() {

        var project = service.getProjectRepositories().iterator().next();

        assertThat(project).isNotNull();
        assertThat(project.getBuildOrder()).isEqualTo(EXPECTED_BUILD_ORDER);
        assertThat(project.getName()).isEqualTo(EXPECTED_NAME);
        assertThat(project.getRepositoryURL()).isEqualTo(EXPECTED_URL);
    }

    @SpringBootApplication
    public static class Main {
        public static void main(String[] args) {
            SpringApplication.run(Main.class, args);
        }
    }
}