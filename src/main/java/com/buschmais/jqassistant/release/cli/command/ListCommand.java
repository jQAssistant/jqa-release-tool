package com.buschmais.jqassistant.release.cli.command;

import com.buschmais.jqassistant.release.cli.git.GitClient;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

public class ListCommand {
    public static void main(String[] args) throws Exception {
        Yaml yaml = new Yaml();

        FileInputStream fis = new FileInputStream("/Users/plexus/jqa/jqa-release-tools/src/test/resources/project-list-01.yaml");

        jQAProjects p = yaml.loadAs(fis, jQAProjects.class);

        p.projects.forEach(i -> System.out.println(i.name));

        GitClient client = new GitClient().init("/tmp/" + UUID.randomUUID());

        for (int i = 0; i < p.projects.size(); i++) {
            jQAProject jQAProject = p.projects.get(i);
            client.clone(jQAProject);
            break;
        }
    }

    static public class jQAProjects {
        public List<jQAProject> projects;
    }

    static public class jQAProject {
        public String id;
        public String name;
        public String version;
        public String groupId;
        public String artifactId;
        public String repository;
    }
}
