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
import java.time.LocalDate;
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

    JavaHTTPServer(Socket connect) throws SocketException {
        this.connect = connect;
        logger.debug("Try to configure socket in the constructor");
        connect.setKeepAlive(true);
        connect.setSoTimeout(3000);

    }

    @Override
    public void run() {
        logger.debug("Run method start");
        String fileRequested;
        try {
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            out = new PrintWriter(connect.getOutputStream());
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            String input = in.readLine();
            logger.info("Input is:\n" + input);
            if (input == null) {
                logger.debug("Input is null");
                return;
            }
            StringTokenizer parse = new StringTokenizer(input);

            String method = parse.nextToken().toUpperCase();
            logger.info("Request method: " + method);
            fileRequested = parse.nextToken().toLowerCase();
            Optional<RequestType> requestType = RequestType.of(method);
            if (requestType.isPresent()) {
                switch (requestType.get()) {
                    case GET -> processGet(fileRequested);
                    case POST -> processPost();
                    case OPTIONS -> processOptions();
                }
            } else {
                methodNotSupported(method);
            }
            logger.info("File {} returned", fileRequested);
        } catch (IOException ioe) {
            logger.error("Server error", ioe);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close();
            } catch (Exception e) {
                logger.error("Error closing stream", e);
            }
            logger.info("Connection closed");
        }
        logger.debug("Run method end");
    }

    private void processGet(String fileRequested) throws IOException {
        logger.info("GET request was accepted");
        if (fileRequested.endsWith("/")) {
            fileRequested += DEFAULT_FILE;
        }
        try {
            InputStream inputStream = findFile(fileRequested, true);
            ContentType content = ContentType.findByFileName(fileRequested);
            byte[] data = content.getReader().read(inputStream);
            createResponse(HTTPCodes.OK, content, data.length, data);
            logger.info("File {} of type {} returned", fileRequested, content.getText());
        } catch (FileNotFoundException e) {
            fileNotFound(fileRequested);
        }
        logger.debug("GET request processing end");
    }

    private void processPost() throws IOException {
        logger.info("POST request was accepted");
        createResponse(HTTPCodes.CREATED, ContentType.PLAIN, 0, new byte[]{});
        logger.debug("POST request processing end");
    }

    private void processOptions() throws IOException {
        logger.info("OPTIONS request was accepted");
        InputStream inputStream = findFile("options.txt", false);
        byte[] data = ContentType.PLAIN.getReader().read(inputStream);
        createResponse(HTTPCodes.OK, ContentType.PLAIN, data.length, data);
        logger.debug("OPTION request processing end");
    }

    private void methodNotSupported(String method) throws IOException {
        logger.warn("Unknown method: {}", method);
        InputStream inputStream = findFile(METHOD_NOT_SUPPORTED, false);
        byte[] data = ContentType.HTML.getReader().read(inputStream);
        createResponse(HTTPCodes.NOT_IMPLEMENTED, ContentType.HTML, data.length, data);
    }

    private void fileNotFound(String fileRequested) throws IOException {
        InputStream inputStream = findFile(FILE_NOT_FOUND, false);
        byte[] data = ContentType.HTML.getReader().read(inputStream);
        createResponse(HTTPCodes.NOT_FOUND, ContentType.HTML, data.length, data);
        logger.warn("File {} not found", fileRequested);
    }

    private InputStream findFile(String fileName, boolean clientFile) throws FileNotFoundException {
        fileName = clientFile ? ROOT + fileName : "/" + fileName;
        InputStream inputStream = this.getClass().getResourceAsStream(fileName);
        logger.info("Requested path of the file is: {} ", this.getClass().getResource(fileName));
        if (inputStream == null) {
            throw new FileNotFoundException();
        }
        return inputStream;
    }

    private void createResponse(HTTPCodes code, ContentType content, int fileLength, byte[] fileData) throws IOException {
        out.println("HTTP/1.1 " + code.getCode() + " " + code.getDescription());
        out.println("Server: COOL Java HTTP Server");
        out.println("Date: " + LocalDate.now());
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
            logger.error("Time is corrupted. World end...", e);
        }
        logger.info("Creating header of response with code {}", code.getCode());
    }
}