package com.buschmais.jqassistant.release.core.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class POMFileBackuper {

    private final String extension;

    public POMFileBackuper(String ext) {
        extension = ext;
    }

    public File makeBackUpOfPom(String directory) {
        File in = new File(directory, "pom.xml");
        File of = new File(directory, "pom.xml." + extension);

        try (var fos = new FileOutputStream(of); var fis = new FileInputStream(in)) {
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        //System.out.println("Backup " + in.toString() + " -> " + of.toString());
        return of;
    }

}
