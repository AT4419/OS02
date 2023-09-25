import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            String serverIP = "192.168.1.19";
            System.out.println("Connecting to server...");
            Socket clientSocket = new Socket(serverIP, 55555);
            System.out.println("Connect successful");

            // kb client
            Scanner sc = new Scanner(System.in);
            // out stream
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            // in stream
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String line;
            while (!clientSocket.isClosed()) {
                printChoice();
                // form kb
                line = sc.nextLine();
                switch (line) {
                    case "exit":
                        System.out.println("Bye bye!");
                        clientSocket.close();
                        break;
                    // but s not close
                    case "1":
                        out.println(line);
                        // change to int form String
                        int size = Integer.parseInt(in.readLine());
                        for (int i = 0; i < size; i++) {
                            System.out.println(in.readLine());
                        }
                        break;
                    case "2":
                        out.println(line);
                        System.out.println("Please input file name :");
                        String filename = sc.nextLine();
                        out.println(filename);

                        long fileSize = Long.parseLong(in.readLine());
                        int threadNumbs = 10;
                        if (fileSize != -1) {
                            for (int i = 0; i < threadNumbs; i++) {
                                long start = (i * fileSize) / threadNumbs;
                                long end = ((i + 1) * fileSize) / threadNumbs;

                                // connect
                                Socket clientSocket2 = new Socket(serverIP, 55555);
                                new Downloader(clientSocket2, start, end, filename).start();
                                // run thread
                            }
                        } else {
                            System.out.println("invalid file");
                        }
                        break;
                    default:
                        System.out.println("Wrong input");
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Connection error");
        }

    }

    public static void printChoice() {
        System.out.println("Type 1 to see all files");
        System.out.println("Type 2 to choose a file to download");
        System.out.println("Type exit to exit the program");
    }

    public static class Downloader extends Thread {
        private long start;
        private long end;
        private Socket client;
        private String fileName;

        public Downloader(Socket client, long start, long end, String fileName) throws IOException {
            this.client = client;
            this.start = start;
            this.end = end;
            this.fileName = fileName;

        }

        public void run() {
            try {
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                out.println("DOWNLOAD");
                out.println(fileName);// j to 74
                out.println(start);
                out.println(end);

                byte[] buffer = new byte[1024];
                RandomAccessFile raf = new RandomAccessFile(".//" + fileName, "rw");
                int bytesRead;

                InputStream in = client.getInputStream();

                while (start < end && (bytesRead = in.read(buffer)) != -1) {
                    raf.seek(start);
                    raf.write(buffer);
                    start += bytesRead;
                }
                raf.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
