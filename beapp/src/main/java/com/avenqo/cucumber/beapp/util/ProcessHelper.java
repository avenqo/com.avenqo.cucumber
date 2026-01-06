package com.avenqo.cucumber.beapp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProcessHelper {

    private final static Logger LOG = LoggerFactory.getLogger(ProcessHelper.class);

    /**
     * List all installed packages
     *
     * @param p
     * @return String, comma-separated Packages, i.e. 'package:com.avenqo.barcodeService, ...'
     * @throws IOException
     */
    public static String read(Process p) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        String txtReturn = null;

        while ((line = br.readLine()) != null) {
            LOG.trace(line);
            if (txtReturn == null)
                txtReturn = line;
            else
                txtReturn += ("," + line);
        }
        return txtReturn;
    }


    public static String start(String command, String para) throws IOException {
        return start(new ProcessBuilder(command, para));
    }

    public static String start(String command, String para1, String para2) throws IOException {
        return start(new ProcessBuilder(command, para1, para2));
    }

    public static String start(ProcessBuilder builder) throws IOException {
        LOG.info("ProcessBuilder started ({})", builder.command().toString());
        builder.redirectErrorStream(true);
        Process p = builder.start();
        String s = ProcessHelper.read(p);
        // LOG.info(s);
        return s;
    }


    public static void killProcessNumbers(Integer[] procNumbers) throws IOException {
        LOG.info("Killing {} processes.", procNumbers != null ? procNumbers.length : 0);
        if (procNumbers != null) {
            for (Integer proc : procNumbers) {
                LOG.info("Kill Process #[{}]", proc);
                String killResponse = start(new ProcessBuilder("kill", proc.toString()));
                LOG.info("Kill response: [{}]", killResponse);
            }
        } else {
            LOG.info("ProcessNumber List is NULL.");
        }
    }

    /**
     * Beispiel: Wird grepPara = 'java' gesetzt, dann wird ein 'ps -ax | grep java' ausgeführt.
     *
     * @param grepPara
     * @param ignores
     * @return Array aller Prozessnummern der gefundenen Prozesse.
     * @throws IOException
     */
    public static Integer[] listProcesses(String grepPara, String[] ignores) throws IOException {

        List<Integer> alProcesses = new ArrayList<Integer>();
        String s = start(new ProcessBuilder("/bin/sh", "-c", "ps -ax | grep " + grepPara));
        LOG.info("Processes listed: [{}]", s);


        String sa[] = s.split(", ");

        if (sa != null)
            for (String strLine : sa) {
                LOG.info("Investigating listed process [{}]", strLine);

                boolean bIgnore = false;
                if (ignores != null) {
                    for (String singleIgnore : ignores) {
                        if (strLine.contains(singleIgnore)) {
                            bIgnore = true;
                            break;
                        }
                    }
                }
                if (bIgnore) {
                    LOG.info("Preceedingly listed process is ignored!");
                    continue;
                }

                int pos = strLine.trim().indexOf(" ");

                if (pos > -1) {
                    String processNumber = strLine.trim().substring(0, pos).trim();

                    if (processNumber != null && processNumber.length() > 0) {
                        try {
                            LOG.info("Adding Process #[{}]", processNumber);
                            alProcesses.add(Integer.parseInt(processNumber));
                        } catch (NumberFormatException nfe) {
                            LOG.info("Ignoring this process number '{}'.", processNumber);
                        }
                    } else {
                        LOG.warn("Process number '{}' seems to be invalid, processNumber");
                    }
                } else
                    LOG.error("Prozessnummer konnte in dieser Zeile nicht gefunden werden: [{}].", strLine);
            }
        return alProcesses.toArray(new Integer[alProcesses.size()]);
    }
}
