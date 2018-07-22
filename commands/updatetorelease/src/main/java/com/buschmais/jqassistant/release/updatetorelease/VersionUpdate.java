package com.buschmais.jqassistant.release.updatetorelease;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.io.File;

public interface VersionUpdate {
    DOMResult update(DOMSource source) throws Exception;

    String getNextVersion();

    void setNextVersion(String nextVersion);

    String getId();

    void setId(String id);

    //void setPropertyName(String string);

    //CharSequence getPropertyName();
}
