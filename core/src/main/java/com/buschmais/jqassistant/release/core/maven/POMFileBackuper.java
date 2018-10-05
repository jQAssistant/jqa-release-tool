package com.buschmais.jqassistant.release.core.maven;

import com.buschmais.jqassistant.release.core.RTException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class POMFileBackuper {

    private final String extension;

    public POMFileBackuper(String ext) {
        extension = ext;
    }

    public void makeBackUpOfPom(String directory) {
        var in = new File(directory, "pom.xml");
        var of = new File(directory, "pom.xml." + extension);

        try (var fos = new FileOutputStream(of); var fis = new FileInputStream(in)) {
            var buffer = new byte[8 * 1024];
            var bytesRead = 0;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RTException("Failed to generate a backup of " + in);
        }
    }

}
