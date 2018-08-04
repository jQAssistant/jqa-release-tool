package com.buschmais.jqassistant.release.updatetorelease;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

public class PomFileVisitor extends SimpleFileVisitor<Path> {

    private LinkedList<File> poms = new LinkedList<>();

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        var result = FileVisitResult.CONTINUE;
        var name = file.getFileName().toFile();

        if (name.equals(new File("pom.xml"))) {
            //System.out.println(file);
            poms.addLast(file.toFile());
        }

        return result;
    }

    public List<File> getFoundFiles() {
        return poms;
    }
}
