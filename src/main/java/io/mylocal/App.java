package io.mylocal;

import io.mylocal.parser.RespParser;
import io.mylocal.streams.MyRedisInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Locale;

public class App {

    private static void handleClient(Socket clientSocket) throws IOException {
        PrintWriter out = null;
        MyRedisInputStream in = null;
        try {

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new MyRedisInputStream(clientSocket.getInputStream());
            System.out.println("Reached here");

            Object obj = RespParser.read(in);

            if(obj instanceof List<?>) {
                List<?> list = (List<?>) obj;
                if (!list.isEmpty() && list.get(0) instanceof byte[]) {
                    byte[] command = (byte[]) list.get(0);
                    if(new String(command).toLowerCase(Locale.ROOT).equals("echo")){
                        out.println(new String((byte[])list.get(1)));
                        out.flush();
                    }
                }
            }
            else if (obj instanceof String) {
                String command = (String) obj;
                if (command.toLowerCase(Locale.ROOT).equals("ping")) {
                    out.println("+PONG\r\n");
                    out.flush();
                }
            }
    } catch (Exception e) {
            System.out.println("Error while handling client request: " + e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
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
