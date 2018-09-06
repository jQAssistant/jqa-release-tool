package com.buschmais.jqassistant.release.services.maven;

import java.util.Collections;
import java.util.List;

public class MavenRequest {
    private String logPrefix;
    private List<String> profiles = Collections.emptyList();
    private List<String> goals = Collections.emptyList();
    private List<String> parameters = Collections.emptyList();
    private String workingDir;

    public String getLogPrefix() {
        return logPrefix;
    }

    public void setLogPrefix(String prefix) {
        this.logPrefix = prefix;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    public List<String> getGoals() {
        return goals;
    }

    public void setGoals(List<String> goals) {
        this.goals = goals;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public void setWorkingDir(String dir) {
        this.workingDir = dir;
    }

    public String getWorkingDir() {
        return workingDir;
    }
}
