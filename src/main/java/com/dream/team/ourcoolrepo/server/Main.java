package com.dream.team.ourcoolrepo.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final String HELP = "\n-h --help\tto print info\n -p --port\tto set custom port\n";
    private static final int DEFAULT_PORT = 8082;

    public static void main(String... args) {
        int port = DEFAULT_PORT;
        List<String> commands = Arrays.stream(args)
                .filter(x -> x.startsWith("-") | x.startsWith("--")).toList();
        if (!commands.isEmpty()) {

            Optional<String> helpCommandOptional = extractFlag(commands, "-h", "--help");
            if (helpCommandOptional.isPresent()) {
                logger.info(HELP);
                return;
            }

            Optional<String> portCommandOptional = extractFlag(commands, "-p", "--port");
            if (portCommandOptional.isPresent()) {
                String portCommand = portCommandOptional.get();
                try {
                    String portString = portCommand.split("=")[1];
                    port = Integer.parseInt(portString);
                    logger.info("Port changed successfully. Using port: " + port);
                } catch (NumberFormatException e) {
                    logger.warn("Argument is invalid, using default({}) port;", DEFAULT_PORT);
                }
            }
        }

        ServerSocket serverConnect;
        try {
            serverConnect = new ServerSocket(port);
        } catch (IOException e) {
            logger.error("Server cannot be started", e);
            return;
        }
        logger.info("Server started. Listening for connections on port :{}", port);

        while (true) {
            JavaHTTPServer myServer;
            try {
                myServer = new JavaHTTPServer(serverConnect.accept());
            } catch (IOException e) {
                logger.error("Socket cannot be open", e);
                break;
            }
            logger.info("Connection opened. ({})", LocalDate.now());

            Thread thread = new Thread(myServer);
            thread.start();
        }
    }

    private static Optional<String> extractFlag(List<String> commands, String shortFlag, String longFlag) {
        return commands.stream()
                .filter(x -> x.startsWith(shortFlag) || x.startsWith(longFlag))
                .findFirst();
    }

}
