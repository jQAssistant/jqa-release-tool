package com.buschmais.jqassistant.release.cli.command.hi;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class HiCommand implements CommandMarker {

    @CliCommand(value = "hi",
                help = "Says hi to the operator")
    public String sayHi() {
        return format("Hi operator!");
    }

}
