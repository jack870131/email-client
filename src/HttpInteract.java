import java.net.*;
import java.io.*;

/**
 * Class for downloading one object from http server.
 */
public class HttpInteract {
    private String host;
    private String path;
    private String requestMessage;
    private String initialURL;
    private String finalURL;


    private static final int HTTP_PORT = 80;
    private static final String CRLF = "\r\n";
    private static final int BUF_SIZE = 4096;
    private static final int MAX_OBJECT_SIZE = 102400;
    /* Create a HttpInteract object. */

    public HttpInteract(String url) {
        /* Split "URL" into "host name" and "path name", and
         * set host and path class variables.
         * if the URL is only a host name, use "/" as path
         */

        /* Fill in */
        URL obj = null;
        try {
            obj = new URL("http://" + url);
            host = obj.getHost();
            path = obj.getPath();
            initialURL = host + path;

            if (path == null || path.length() == 0) {
                path = "/";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Construct requestMessage, add a header line so that
         * server closes connection after one response. */

        /* Fill in */
        requestMessage = "GET " + path + " HTTP/1.1" + CRLF +
                "Host: " + host + CRLF +
                "Connection: close" + CRLF + CRLF;
        return;
    }

    /* Send Http request, parse response and return requested object
     * as a String (if no errors),
     * otherwise return meaningful error message.
     * Don't catch Exceptions. EmailClient will handle them. */

    public String send() throws IOException {

        /* buffer to read object in 4kB chunks */
        char[] buf = new char[BUF_SIZE];

        /* Maximum size of object is 100kB, which should be enough for most objects.
         * Change constant if you need more. */
        char[] body = new char[MAX_OBJECT_SIZE];

        String statusLine = "";    // status line
        int status;        // status code
        String headers = "";    // headers
        int bodyLength = -1;    // length of body

        String[] tmp;

        /* The socket to the server */
        Socket connection;

        /* Streams for reading from and writing to socket */
        BufferedReader fromServer;
        DataOutputStream toServer;

        System.out.println("Connecting server: " + host + CRLF);

        /* Connect to http server on port 80.
         * Assign input and output streams to connection. */
        connection = new Socket(host, HTTP_PORT);
        fromServer = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        toServer = new DataOutputStream(connection.getOutputStream());

        System.out.println("Send request:\n" + requestMessage);

        /* Send requestMessage to http server */
        /* Fill in */
        toServer.writeBytes(requestMessage);

        /* Read the status line from response message */
        statusLine = fromServer.readLine();
        System.out.println("Status Line:\n" + statusLine + CRLF);

        /* Extract status code from status line. If status code is not 200,
         * close connection and return an error message.
         * Do NOT throw an exception */
        /* Fill in */
        status = Integer.parseInt(statusLine.substring(9, 12));
        if (status != 200 && status != 301 && status != 302) {
            connection.close();
            return "There's an error within the connection";
        }

        String location;
        if (status == 301 || status == 302) {
            while (!(location = fromServer.readLine()).equals("")) {
                if (location.indexOf("Location: http://") != -1) {
                    finalURL = location.substring(
                            location.indexOf("Location: http://") + 17,
                            location.length());
                }
            }
            connection.close();
            HttpInteract redirect = new HttpInteract(finalURL);
            return redirect.send();
        } else {
            finalURL = initialURL;
        }

        /* Read header lines from response message, convert to a string,
         * and assign to "headers" variable.
         * Recall that an empty line indicates end of headers.
         * Extract length  from "Content-Length:" (or "Content-length:")
         * header line, if present, and assign to "bodyLength" variable.
         */
        /* Fill in */        // requires about 10 lines of code
        String line;
        StringBuilder builder = new StringBuilder();
        while (!(line = fromServer.readLine()).equals("")) {
            builder.append(line + CRLF);
            if (line.toLowerCase().indexOf("Content-Length:".toLowerCase()) > -1) {
                bodyLength = Integer.parseInt(
                        line.substring(
                                line.toLowerCase().
                                        indexOf("Content-Length:".toLowerCase()) + 16,
                                line.length()));
            }
        }
        System.out.println("Headers:\n" + builder.toString() + CRLF);

        /* If object is larger than MAX_OBJECT_SIZE, close the connection and
         * return meaningful message. */
        if (bodyLength > MAX_OBJECT_SIZE) {
            /* Fill in */
            connection.close();
            return ("The length " + bodyLength + " exceed the maximum object size");
        }

        /* Read the body in chunks of BUF_SIZE using buf[] and copy the chunk
         * into body[]. Stop when either we have
         * read Content-Length bytes or when the connection is
         * closed (when there is no Content-Length in the response).
         * Use one of the read() methods of BufferedReader here, NOT readLine().
         * Make sure not to read more than MAX_OBJECT_SIZE characters.
         */
        int bytesRead = 0;
        /* Fill in */   // Requires 10-20 lines of code
        int index = 0;
        while ((index = fromServer.read(buf, 0, BUF_SIZE)) != -1) {
            System.arraycopy(buf, 0, body, bytesRead, BUF_SIZE);
            bytesRead += index;
            if (bytesRead >= MAX_OBJECT_SIZE || bytesRead == bodyLength) {
                break;
            }
        }

        /* At this points body[] should hold to body of the downloaded object and
         * bytesRead should hold the number of bytes read from the BufferedReader
         */

        /* Close connection and return object as String. */
        System.out.println("Done reading file. Closing connection.");
        connection.close();
        return (new String(body, 0, bytesRead));
    }

    /*
     * Get the URL which should be filled into the URL field
     */
    public String getURL() {
        return finalURL.toString();
    }
}
