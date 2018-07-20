package com.buschmais.jqassistant.release.services.maven;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Builder class to used to build the name of a logfile of a Maven run
 * to ensure a unique format of the logfile names.
 */
public class LogfileNameBuilder {
    private StringBuilder builder = new StringBuilder();

    public LogfileNameBuildStepTwo inDirectory(CharSequence path) {

        builder.append(path);

        if (path.charAt(path.length() - 1) != '/') {
            builder.append("/");
        }

        return new LogfileNameBuildStepTwo();
    }

    public class LogfileNameBuildStepTwo {
        public LogfileNameBuilderStepThree withPrefix(CharSequence prefix) {
            builder.append(prefix);

            return new LogfileNameBuilderStepThree();
        }
    }

    public class LogfileNameBuilderStepThree {

        public LogfileNameBuilderStepFour withProjectName(CharSequence project) {
            builder.append("-").append(project);

            return new LogfileNameBuilderStepFour();
        }
    }

    public class LogfileNameBuilderStepFour {
        public LogfileNameBuilderStepFour withKeyword(CharSequence keyword) {
            builder.append("-").append(keyword);

            return this;
        }

        public LogfileNameBuilderStepFive withDate() {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");
            String dateString = formatter.format(now);

            builder.append("-").append(dateString);

            return new LogfileNameBuilderStepFive();
        }
    }

    public class LogfileNameBuilderStepFive {
        public File build() {
            String fullPath = builder.append(".log").toString();

            return new File(fullPath);
        }
    }
}
