package com.buschmais.jqassistant.release.core.maven;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;

public class VersionSetter {
    public void set(String directory, List<VersionUpdate> updater) {
        try {
            DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory2.newDocumentBuilder();
            PomFileVisitor visitor = new PomFileVisitor();
            Path sp = new File(directory).toPath();
            Files.walkFileTree(sp, visitor);
            List<File> foundFiles = visitor.getFoundFiles();
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            //transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");


            foundFiles.forEach(pomFile -> {
                try {
                    Document document = builder.parse(pomFile);
                    DOMSource source = new DOMSource(document);

                    for (VersionUpdate vu : updater) {
                        //System.out.println();
                        //System.out.println(pomFile);
                        //System.out.println(updater);
                        if (UpdateParent.class.isAssignableFrom(updater.getClass())) {
                            //UpdateParent u = ((UpdateParent) updater);
                            //System.out.println("id: " + u.getId());
                            //System.out.println("gi: " + u.getGroupId());
                            //System.out.println("ai: " + u.getArtifactId());
                        }

                        DOMResult dr = vu.update(source);
                        source = new DOMSource(dr.getNode());

                    }

                    Document res = (Document) source.getNode();
                    var source2 = new DOMSource(res);
                    transformer.transform(source2, new StreamResult(pomFile));

                } catch (SAXException sax) {
                    System.out.println("Failed update " + pomFile);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
