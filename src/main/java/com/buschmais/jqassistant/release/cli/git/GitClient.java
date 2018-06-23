package com.buschmais.jqassistant.release.cli.git;

import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class GitClient {

    private File workingDir;

    public GitClient init(String wrkDir) {
        workingDir = new File(wrkDir);
        workingDir.mkdirs();

        return this;
    }
/*
    public void clone(ListCommand.jQAProject project) throws Exception {

        Git.cloneRepository()
           .setURI(project.repository)
           .setDirectory(

               t

           )
           .call();


        FileInputStream fis = new FileInputStream("/Users/plexus/pom.xml");
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(fis);

        BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));

        //new MavenXpp3Writer().write(log, model);

        ModelVersion v = new XBProjector().io()
                                          .file("/Users/plexus/bla.xml")
                                          .read(ModelVersion.class);

        System.out.println(v.gversion());
        v.sversion("Huch");

        //new XBProjector().io().stream(System.out).write(v);
        //new XBProjector().io().file("/Users/plexus/bla2.xml").write(v);

        //-----

        String xslt = "/Users/plexus/jqa/jqa-release-tools/src/main/java/com/buschmais/jqassistant/release/cli/git/t.xsl";
        String xml = "/Users/plexus/bla.xml";

        StreamSource source = new StreamSource(xml);
        StreamSource stylesource = new StreamSource(xslt);

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(stylesource);

        StreamResult result = new StreamResult(System.out);
        transformer.transform(source, result);

        System.out.println("Target für " + project.name +
        " ist " + t.toString());
        File t = new File(workingDir.toString() + "/" + project.id);


    }

    */
}

