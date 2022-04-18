package com.dream.team.ourcoolrepo.server;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Optional;
import java.util.StringTokenizer;

public class JavaHTTPServer implements Runnable {
    private static final Logger logger = LogManager.getLogger(JavaHTTPServer.class);
    private static final String ROOT = "/sharedFiles";
    private static final String DEFAULT_FILE = "index.html";
    private static final String FILE_NOT_FOUND = "404.html";
    private static final String METHOD_NOT_SUPPORTED = "501.html";

    private final Socket connect;

    private BufferedReader in;
    private PrintWriter out;
    private BufferedOutputStream dataOut;

    JavaHTTPServer(Socket connect) {
        this.connect = connect;
        try {
            connect.setKeepAlive(true);
            connect.setSoTimeout(3000);
        } catch (SocketException e) {
            System.exit(0);
        }

    }

    @Override
    public void run() {
        String fileRequested;
        try {
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            out = new PrintWriter(connect.getOutputStream());
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            String input = in.readLine();
            logger.log(Level.INFO, "Input is:\n" + input);
            StringTokenizer parse;
            try {
                parse = new StringTokenizer(input);
            } catch (NullPointerException e) {
                return;
            }

            String method = parse.nextToken().toUpperCase();
            logger.log(Level.INFO, "Request method: " + method);
            fileRequested = parse.nextToken().toLowerCase();
            Optional<RequestType> requestType = RequestType.of(method);

            if (requestType.isEmpty()) {
                methodNotSupported(method);
            } else {
                switch (requestType.get()) {
                    case GET:
                        processGet(fileRequested);
                        break;
                    case POST:
                        processPost();
                    case OPTIONS:
                        processOptions();
                }
                logger.log(Level.INFO, "File " + fileRequested + " returned");
            }
        } catch (IOException ioe) {
            logger.log(Level.ERROR, "Server error : " + ioe);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close();
            } catch (Exception e) {
                logger.log(Level.ERROR, "Error closing stream : " + e.getMessage());
            }
            logger.log(Level.INFO, "Connection closed");
        }
    }

    private void processGet(String fileRequested) throws IOException {
        logger.log(Level.INFO, "GET request was accepted");
        if (fileRequested.endsWith("/")) {
            fileRequested += DEFAULT_FILE;
        }
        InputStream inputStream = findFile(fileRequested, true);
        ContentType content = ContentType.findByFileName(fileRequested);
        byte[] data = content.getReader().read(inputStream);
        createResponse(HTTPCodes.OK, content, data.length, data);
        logger.log(Level.INFO, "File " + fileRequested + " of type " + content.getText() + " returned");
    }

    private void processPost() throws IOException {
        logger.log(Level.INFO, "POST request was accepted");
        createResponse(HTTPCodes.CREATED, ContentType.PLAIN, 0, new byte[]{});
    }

    private void processOptions() throws IOException {
        logger.log(Level.INFO, "OPTIONS request was accepted");
        InputStream inputStream = findFile("options.txt", false);
        byte[] data = ContentType.PLAIN.getReader().read(inputStream);
        createResponse(HTTPCodes.OK, ContentType.PLAIN, data.length, data);
    }

    private void methodNotSupported(String method) throws IOException {
        logger.log(Level.WARN, "Unknown method: " + method);
        InputStream inputStream = findFile(METHOD_NOT_SUPPORTED, false);
        byte[] data = ContentType.HTML.getReader().read(inputStream);
        createResponse(HTTPCodes.NOT_IMPLEMENTED, ContentType.HTML, data.length, data);
    }

    private void fileNotFound(String fileRequested) throws IOException {
        InputStream inputStream = findFile(FILE_NOT_FOUND, false);
        byte[] data = ContentType.HTML.getReader().read(inputStream);
        createResponse(HTTPCodes.NOT_FOUND, ContentType.HTML, data.length, data);
        logger.log(Level.WARN, "File " + fileRequested + " not found");

    }

    private InputStream findFile(String fileName, boolean clientFile) throws FileNotFoundException {
        if (clientFile) {
            fileName = ROOT + fileName;
        } else {
            fileName = "/" + fileName;
        }
        InputStream inputStream = this.getClass().getResourceAsStream(fileName);
        logger.log(Level.INFO, "requested path of the file is: " + this.getClass().getResource(fileName));
        if (inputStream == null) {
            throw new FileNotFoundException();
        }
        return inputStream;
    }

    private void createResponse(HTTPCodes code, ContentType content, int fileLength, byte[] fileData) throws IOException {
        out.println("HTTP/1.1 " + code.getCode() + " " + code.getDescription());
        out.println("Server: Java HTTP Server");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content.getText());
        out.println("Content-length: " + fileLength);
        out.println("Access-Control-Allow-Origin: " + "localhost");
        out.println("Access-Control-Allow-Methods: " + "GET, POST, OPTIONS");
        out.println();
        out.flush();
        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
        logger.log(Level.INFO, "type " + content.getExtension() + " size " + fileLength);
        try {
            Thread.sleep(fileLength / 100);
        } catch (InterruptedException e) {

        }
        logger.log(Level.INFO, "Creating header of response with code " + code.getCode());
    }
}