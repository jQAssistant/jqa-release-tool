package com.buschmais.jqassistant.release.cli;

import org.springframework.shell.Bootstrap;

import java.io.IOException;

//@SpringBootApplication
public class ReleaseToolMain {
    public static void main(String[] args) {

        try {
            Bootstrap.main(args);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}