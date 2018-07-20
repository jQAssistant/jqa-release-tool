package com.buschmais.jqassistant.release.updatetorelease.updates;

import com.buschmais.jqassistant.release.updatetorelease.VersionUpdate;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;

public class UpdateCoreFrameworkVersion extends VersionUpdate {


    public void update(File sourcePom, File targetPom) throws Exception {
        InputStream xslt = this.getClass().getResourceAsStream("/xsl/update-version-property.xsl");

        StreamSource source = new StreamSource(sourcePom);
        StreamSource stylesource = new StreamSource(xslt);

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(stylesource);

        StreamResult result = new StreamResult(targetPom);
        transformer.setParameter("property_name", getPropertyName());
        transformer.setParameter("version_information", getNewVersionNumber());
        transformer.transform(source, result);
    }

    public CharSequence getPropertyName() {
        return "jqa-core-framework.version";
    }

    public CharSequence getNewVersionNumber() {
        return "VORWÄRTS";
    }
}
