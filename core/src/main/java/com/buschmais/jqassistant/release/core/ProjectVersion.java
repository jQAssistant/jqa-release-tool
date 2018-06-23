package com.buschmais.jqassistant.release.core;

import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;

import static java.lang.String.format;

public class ProjectVersion extends DefaultVersionInfo {
    public ProjectVersion(String version) throws VersionParseException {
        super(version);
    }

    @Override
    public VersionInfo getNextVersion() {
        DefaultVersionInfo version = null;
        if (this.getDigits() != null) {
            ArrayList<String> digits = new ArrayList<>(this.getDigits());
            String annotationRevision = this.getAnnotationRevision();
            if (StringUtils.isNumeric(annotationRevision)) {
                annotationRevision = this.incrementVersionString(annotationRevision);
            } else {
                // todo PR für Basisklasse
                // todo RT wenn nicht zwei oder drei digits
                int index = digits.size() == 3 ? 2 : 1;
                digits.set(digits.size() - index,
                           this.incrementVersionString((String) digits.get(digits.size() - index)));
            }

            version = new DefaultVersionInfo(digits, this.getAnnotation(),
                                             getAnnotationRevision(),
                                             getBuildSpecifier(),
                                             // todo hier brauche ich getter!
                                             "-", "-", "-");
        }

        return version;

    }
}
