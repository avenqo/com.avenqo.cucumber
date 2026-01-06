package com.avenqo.cucumber.beapp.util;

import com.avenqo.cucumber.beapp.exceptions.EConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdbHelper {

    final private static Logger log = LoggerFactory.getLogger(AdbHelper.class);

    // ----------------------- ADB -------------------------
    // Diese Env-Variable muss den vollständigen Path zu ADB enthalten
    private static final String ADB_ENV = "ADB_HOME";
    private static String adb_loc = null;

    public static String getPath() throws EConfigException {
        if (adb_loc == null) {

            String s = System.getenv(ADB_ENV);

            // Not defi ned? Error
            if (s == null || s.length() == 0) {
                log.error("Die Variable '" + ADB_ENV + "' muss auf das ADB-Tool zeigen. Ist aber leer!");
                throw new EConfigException("Missing environment variable '" + ADB_ENV + "'.");
            } else
                adb_loc = s;

            log.info("Using ADB location '{}'.", adb_loc);
        }
        return adb_loc;
    }

    /**
     * Run adb tool with the given parameters
     *
     * @param para
     * @return
     * @throws IOException
     * @throws EConfigException
     */
    public static String run(List<String> para) throws IOException, EConfigException {
        log.info("Running with parameters: ");
        List<String> al = new ArrayList<String>();
        al.add(AdbHelper.getPath());
        al.addAll(para);

        for (String s : al)
            log.info("   Parameter '{}' ", s);
        ProcessBuilder builder = new ProcessBuilder(al);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        return ProcessHelper.read(p);
    }

    public static String run(String para) throws IOException, EConfigException {
        List<String> al = new ArrayList<String>();
        al.add(para);
        return run(al);
    }
}
