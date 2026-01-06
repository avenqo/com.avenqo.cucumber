package com.avenqo.cucumber.beapp.util;

import com.avenqo.cucumber.beapp.exceptions.EConfigException;
import com.avenqo.cucumber.beapp.exceptions.EInconsistencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;


public class FileHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileHelper.class);

    /**
     * Creates a 'save' string usable for file names.
     *
     * @param fileName
     * @return
     */
    public static String removeForbiddenChars(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

    /**
     * Check if the directory defined via the given property key exists. If not:
     * tries to create it.
     *
     * @param propertyName
     * @param dirPath
     * @return File to the (existing or created) directory.
     * @throws Throwable
     */
    public static File checkDestPathAndCreateIfNecessary(String propertyName, String dirPath) throws Throwable {
        File dest = new File(dirPath);
        if (!dest.exists()) {
            LOGGER.warn("Creating directory '{}' for property '{}' (doesn't exist yet)", dirPath, propertyName);
            if (!dest.mkdirs()) {
                LOGGER.error("Failed to create destination directory '{}'", dirPath);
            }
        }
        if (!dest.exists())
            throw new EConfigException("Directory doesn't exists: " + dest.getAbsolutePath());
        return dest;
    }


    /**
     * Check if the directory defined via the given property key exists. If not:
     * tries to create it.
     *
     * @param dirPath
     * @return File to the (existing or created) directory.
     * @throws Throwable
     */
    public static File checkDestPathAndCreateIfNecessary(String dirPath) throws Throwable {
        File dest = new File(dirPath);
        if (!dest.exists()) {
            LOGGER.warn("Creating directory '{}' (doesn't exist yet)", dirPath);
            if (!dest.mkdirs()) {
                LOGGER.error("Failed to create destination directory '{}'", dirPath);
            }
        }
        if (!dest.exists())
            throw new EConfigException("Directory doesn't exists: " + dest.getAbsolutePath());
        return dest;
    }

    /**
     * Checks if the string parameter is valid. Doesn't check if the file exists
     *
     * @param file
     * @return
     */

    public static boolean isFilenameValid(String file) {
        File f = new File(file);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Löscht den Inhalt des Dirs, aber nicht das DIr selbst.
     *
     * @param directory
     * @throws EConfigException
     */
    public static void deleteDirContent(String directory) throws Exception {
        LOGGER.info("Deleting content of directory: '{}'", directory);
        File file = new File(directory);
        if (!file.exists()) {
            throw new EConfigException("Directory doesn't exists: " + directory);
        }
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory())
                throw new EConfigException("Cannot delete directories: " + f.getAbsolutePath());
            if (!f.delete())
                throw new EConfigException("Cannot delete File: " + f.getAbsolutePath());
        }

        LOGGER.info("Deleted: {} files!", files.length);
    }

    /**
     * Search in the given directory for an unique file ending with extension.
     *
     * @param directoryPath The directory used when searching files ending with
     *                      'extension'.
     * @param extension     i.e. ".ipa"
     * @return Path to the unique file
     * @throws Exception
     */
    public static File findUniqueFileInDirectory(String directoryPath, String extension) throws Exception {
        File retFile = null; // file to be returned

        // Der Pfad muss existieren;.
        File file = new File(directoryPath);
        if (!file.exists()) {
            String errMsg = "Path [" + directoryPath + "] doesn't exist.";
            throw new EInconsistencyException(errMsg);
        }

        // Das File muss ein Dir sein
        if (!file.isDirectory()) {
            String errMsg = "Path [" + directoryPath + "] isn't a directory.";
            throw new EInconsistencyException(errMsg);
        }

        File[] kids = file.listFiles();
        if (kids != null && kids.length > 0) {
            for (File kid : kids) {
                if (kid.getAbsolutePath().endsWith(extension)) {
                    if (retFile == null)
                        retFile = kid;
                    else {
                        String errMsg = "There are existing more than one Files in directory [" + directoryPath
                                + "] ending with extension[" + extension + "].";
                        throw new EInconsistencyException(errMsg);
                    }
                }
            }
        } else {
            String errMsg = "No files found in directory [" + directoryPath + "].";
            LOGGER.error(errMsg);
            throw new EInconsistencyException(errMsg);
        }

        // Darf hier nicht mehr null sein; sonst nicht gefunden
        if (retFile == null) {
            String errMsg = "There is no file existing in directory [" + directoryPath + "] ending with extension["
                    + extension + "].";
            throw new EInconsistencyException(errMsg);
        }

        return retFile;
    }


    public  File getFromResources(String fileName) throws IOException
    {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);

        if (resource == null) {
            throw new IllegalArgumentException("file isn't defined!");
        } else {
            File file = new File(resource.getFile());
            if (!file.exists()){
                throw new IOException("File [" + fileName + "] not found");
            }
            return file;
        }
    }
}
