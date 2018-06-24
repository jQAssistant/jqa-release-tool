package com.buschmais.jqassistant.release.services.maven;

import org.apache.maven.shared.invoker.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class MavenService {
    public static List<String> BUILD_ONLY_GOALS = Arrays.asList("clean", "install");
    public static String BUILD_ONLY_OPTS = "-DskipTests=true -Djqassistant.skip=true";

    public void buildWithoutAnyTests(String directory) {
        // TODO: 23.06.18 Streams schließen
        // TODO: 23.06.18 class path resource verwenden
        System.out.println();
        InputStream is = MavenService.class.getResourceAsStream("/settings.xml");

        File of = new File("maven-settings.xml");

        try {

            FileOutputStream fos = new FileOutputStream(of);

            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }



        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        File lrd = new File("maven-local-repo");
        lrd.mkdirs();

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File(directory + "/pom.xml" ) );
        request.setGoals(BUILD_ONLY_GOALS);
        request.setMavenOpts(BUILD_ONLY_OPTS);
        request.setLocalRepositoryDirectory(lrd);
        request.setUserSettingsFile(of);
        request.setGlobalSettingsFile(of);
        request.setUpdateSnapshots(false);
        request.setInteractive(false);
        request.setProfiles(Collections.emptyList());
        request.setJavaHome(new File("/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home"));
        //request.setProperties()


        Invoker invoker = new DefaultInvoker();
        try {
            InvocationResult result = invoker.execute(request);
            if (0 != result.getExitCode()) {
                System.exit(1);
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
            //
            throw new RuntimeException(e);
        }
    }
}
