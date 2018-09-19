package com.buschmais.jqassistant.release.core.maven;


import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;


public class UpdateCoreFrameworkVersion implements VersionUpdate {

    private String id;
    private String string;
    private String nextVersion;

    public DOMResult update(DOMSource source) throws Exception {
        InputStream xslt = this.getClass().getResourceAsStream("/xsl/update-version-property.xsl");

        //StreamSource source = new StreamSource(sourcePom);
        StreamSource stylesource = new StreamSource(xslt);

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(stylesource);

        // todo System.out.println("\t" + getPropertyName() + " -> " + getNextVersion());
        DOMResult result = new DOMResult();
        transformer.setParameter("property_name", getPropertyName());
        transformer.setParameter("version_information", getNextVersion());
        transformer.transform(source, result);

        return result;
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

    public void setPropertyName(String string) {
        this.string = string;
    }

    public CharSequence getPropertyName() {
        return string;
    }
}
