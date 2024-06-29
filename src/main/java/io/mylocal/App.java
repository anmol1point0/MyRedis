package io.mylocal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

public class App {

    private static void handleClient(Socket clientSocket) {
        BufferedReader stdIn = null;
        PrintWriter out = null;
        try {

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));

            String line;
            while ((line = in.readLine()) != null) {
                if (line.toLowerCase(Locale.ROOT).contains("ping"))
                    out.println("PONG");
            }
        } catch (Exception e) {
            System.out.println("Error while handling client request: " + e.getMessage());
        } finally {
            try {
                if (stdIn != null) {
                    stdIn.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                System.out.println("Error while closing streams in handle client");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Logs From the program will appear here");

        ServerSocket serverSocket = null;
        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);

            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);

            while (true) {
                // Wait for connection from client.
                Socket clientSocket = serverSocket.accept();

                new Thread(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (Exception e) {
                        System.out.println("Error in main: " + e.getMessage());
                    }
                }).start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
