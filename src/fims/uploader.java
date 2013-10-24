package fims;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.UUID;

/**
 * Uploader sends files to a particular service and a particular graph
 */
public class uploader {

    private File file;
    private String endpoint;
    private String graph;
    private String content_type = "text/turtle;charset=utf-8;";
    private String method = "POST";
    private String encoding = "UTF-8";
    private String service;

    /**
     * Constructor for the uploader, currently just takes a file
     *
     * @param service the fuseki service, e.g. http://localhost:3030/ds
     * @param graph          the name of the graph we want to use, should ttbe in URI form, e.g. http://biscicol.org/graph1
     *                       use "default" for the default graph
     * @param file           for now the file is always a turtle format file
     */
    public uploader(String service, String graph, File file) {
        this.graph = graph;
        this.service = service;
        try {
            this.endpoint = service + "?graph=" + URLEncoder.encode(graph, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.file = file;
    }

    /**
     * Constructor called without a graph specification uses a UUID for the graph
     * TODO: fetch an ARK for this graph-name and add some metadata
     * @param fuseki_service
     * @param file
     */
    public uploader(String fuseki_service, File file) {
        this(fuseki_service, "urn:uuid:" + UUID.randomUUID(), file);
    }

    /**
     * @method execute the update
     */
    public String execute() throws Exception {
        try {
            // Setup URL connection to this endpoint
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", content_type);
            conn.setRequestMethod(method);

            // Write the file to the connection, sending contents directly to the service
            WritableByteChannel channel = Channels.newChannel(conn.getOutputStream());
            new FileInputStream(file).getChannel().transferTo(0, 9999999, channel);

            // Get the response from the service
            BufferedReader rd = new BufferedReader(new
                    InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                // Process line...
            }
            rd.close();
        } catch (Exception e) {
            // throw a general exception here since we want to inform the call application of any mis-deeds
            // typically this will be the service being down
            throw e;
        }
        return graph;
    }

    public String getMethod() {
        return method;
    }

    public String getContent_type() {
        return content_type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getService() {
        return service;
    }

    public String getGraph() {
        return graph;
    }

    /**
     * Return an encoding of this graph so it can be packed up and sent to a service
     * @return
     */
    public String getEncodedGraph(boolean brackets) {
        try {
            String toEncode = "";
            if (brackets) toEncode += "<";
            toEncode += graph;
            if (brackets) toEncode += ">";
            return URLEncoder.encode(toEncode,encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
