package run;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Jsoup;
import settings.FIMSException;
import settings.PathManager;
import settings.bcidConnector;
import utils.SettingsManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Date;

/**
 * Class that handles getting configuration files.  Configuration files are stored as BCID/ARKs and thus this class
 * needs to handle redirection when fetching appropriate configuration files.
 */
public class configurationFileFetcher {
    private File outputFile;
    // TODO: Fix biscicol.org resolution -- can't see itself! The work-around here is to use a different port
//    private String projectLookup = "http://biscicol.org:8080/id/projectService/validation/";
    private Integer project_id;
    private String configFileName;

    public Integer getProject_id() {
        return project_id;
    }

    public File getOutputFile() {
        return outputFile;
    }

    /**
     * If file is more than 24 hours old or does not exist then return false
     *
     * @param defaultOutputDirectory
     * @return
     */
    public Boolean getCachedConfigFile(String defaultOutputDirectory) throws Exception {
        // Create a file reference
        File file = new File(defaultOutputDirectory,configFileName);

        // check for file existing, if not then return false
        if (!file.exists())
            return false;

        // check for files older than 24 hours
        if (new Date().getTime() - file.lastModified() > 24 * 60 * 60 * 1000)
            return false;

        // File exists and is younger than 24 hours old, set the outputFile class variable
        outputFile = new File(defaultOutputDirectory,configFileName);
        return true;
    }

    /**
     * Create the class object given a particular expedition code and a default Output Directory
     *
     * @param defaultOutputDirectory
     * @throws IOException
     */
    public configurationFileFetcher(Integer project_id, String defaultOutputDirectory, Boolean useCache) throws Exception {
        this.project_id = project_id;
        configFileName = "config." + project_id + ".xml";

        SettingsManager sm = SettingsManager.getInstance();
        sm.loadProperties();

        String project_lookup_uri = sm.retrieveValue("project_lookup_uri");

        Boolean useCacheResults = false;

        // call cache operation if user wants it
        if (useCache) {
            useCacheResults = getCachedConfigFile(defaultOutputDirectory);
        }

        // get a fresh copy if the useCacheResults is false
        if (!useCacheResults) {
            // Get the URL for this configuration File
            String projectServiceString = project_lookup_uri + project_id;
            bcidConnector connector = new bcidConnector();
            // Set a 10 second timeout on this connection
            JSONObject response = (JSONObject) JSONValue.parse(connector.createGETConnection(new URL(projectServiceString)));

            if (response.containsKey("error")) {
                throw new FIMSException(response.get("error").toString());
            }
            String urlString = (String) response.get("validation_xml");
            // Setup connection

            URL url = new URL(urlString);
            init(new URL(urlString), defaultOutputDirectory);
        }
    }

    private void init(URL url, String defaultOutputDirectory) throws Exception {
        boolean redirect = false;

        HttpURLConnection.setFollowRedirects(true);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(5000);
        connection.setUseCaches(false);
        connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        connection.addRequestProperty("User-Agent", "Mozilla");
        connection.addRequestProperty("Referer", "google.com");

        // Handle response Codes, Normally, 3xx is redirect, setting redirect boolean variable if it is a redirect
        int status = connection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER)
                redirect = true;
        }

        // Handle redirects
        if (redirect) {

            // get redirect url from "location" header field
            String newUrl = connection.getHeaderField("Location");

            // open the  connnection
            connection = (HttpURLConnection) new URL(newUrl).openConnection();
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.addRequestProperty("Cache-Control", "no-cache");

            // connection.setRequestProperty("Cookie", cookies);
            connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            connection.addRequestProperty("User-Agent", "Mozilla");
            connection.addRequestProperty("Referer", "google.com");

        }
        InputStream inputStream = connection.getInputStream();

        // Write configuration file to output directory
        try {
            //outputFile = PathManager.createUniqueFile("config.xml", defaultOutputDirectory);
            outputFile = PathManager.createFile(configFileName, defaultOutputDirectory);
        } catch (Exception e) {
            throw new IOException("Unable to create configuration file", e);
        }
        FileOutputStream os = new FileOutputStream(outputFile);
        try {
            byte[] buffer = new byte[1024];
            int bytesRead;
            //read from is to buffer
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            //flush OutputStream to write any buffered data to file
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new Exception("Unable to get configuration file, server down or network error ", e);
        }
    }


    /**
     * Readfile method -- used as a convenience in this class for testing.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static String readFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        configurationFileFetcher cFF = null;
        String defaultOutputDirectory = System.getProperty("user.dir") + File.separator + "tripleOutput";

        try {
            cFF = new configurationFileFetcher(1, defaultOutputDirectory, false);
            // System.out.println(readFile(cFF.getOutputFile()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
