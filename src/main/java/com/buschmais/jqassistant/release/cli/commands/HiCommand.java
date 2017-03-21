package com.buschmais.jqassistant.release.cli.commands;

import org.springframework.shell.core.CommandMarker;
import org.springframework.stereotype.Component;

@Component
public class HiCommand implements CommandMarker {

    public String sayHi() {

        return "hi";
    }

}
