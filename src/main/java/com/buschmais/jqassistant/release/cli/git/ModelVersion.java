package com.buschmais.jqassistant.release.cli.git;

import org.xmlbeam.annotation.XBRead;
import org.xmlbeam.annotation.XBWrite;

public interface ModelVersion {


    @XBWrite("/r/c")
    void sversion(String v);

    @XBRead("/r/c")
    String gversion();

}
