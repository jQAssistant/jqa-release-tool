package com.buschmais.jqassistant.release.core.maven;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

public class UpdateParentVersion implements VersionUpdate {

    private String id;
    private String nextVersion;
    private String groupId;
    private String artifactId;

    public DOMResult update(DOMSource source) throws Exception {
        InputStream xslt = this.getClass().getResourceAsStream("/xsl/update-parent-version.xsl");

        //StreamSource source = new StreamSource(sourcePom);
        StreamSource stylesource = new StreamSource(xslt);

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(stylesource);

        // todo System.out.println("\t" + getPropertyName() + " -> " + getNextVersion());
        DOMResult result = new DOMResult();
        transformer.setParameter("version_information", getNextVersion());
        transformer.setParameter("group_id", getGroupId());
        transformer.setParameter("artifact_id", getArtifactId());
        transformer.transform(source, result);

        return result;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    @Override
    public String getNextVersion() {
        return nextVersion;
    }

    @Override
    public void setNextVersion(String nextVersion) {
        this.nextVersion = nextVersion;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
