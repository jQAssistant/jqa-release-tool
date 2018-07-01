package com.buschmais.jqassistant.release.services.maven;

import org.apache.maven.shared.invoker.InvocationOutputHandler;

import java.io.*;

public class LogWriter
    implements AutoCloseable, InvocationOutputHandler {

    private final File file;
    FileWriter writer;

    public LogWriter(File outputfile) {
        file = outputfile;
    }

    public void start() throws IOException {
        writer = new FileWriter(getFile());
    }

    public void stop() throws IOException {
        writer.flush();
        writer.close();
    }


    @Override
    public void consumeLine(String s) {
        try {
            writeLine(s);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void writeLine(String s) throws IOException {
        writer.write(s);
        writer.write("\r\n");
        writer.flush();
    }

    /**
     * Calls {@link #stop()}.
     */
    @Override
    public void close() throws IOException {
        this.stop();
    }

    protected File getFile() {
        return file;
    }
}
