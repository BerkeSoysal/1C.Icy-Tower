package model.network;

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkManager {
    private static NetworkManager instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ServerSocket serverSocket;
    private boolean isHost;
    private boolean connected = false;

    private ConcurrentLinkedQueue<GameStatePacket> incomingPackets = new ConcurrentLinkedQueue<>();

    public static NetworkManager getInstance() {
        if (instance == null) instance = new NetworkManager();
        return instance;
    }

    public void startServer(int port, Runnable onConnect) {
        isHost = true;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Waiting for client on port " + port + "...");
                socket = serverSocket.accept();
                System.out.println("Client connected.");
                setupStreams();
                if (onConnect != null) onConnect.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void startClient(String ip, int port, Runnable onConnect) {
        isHost = false;
        new Thread(() -> {
            try {
                System.out.println("Connecting to " + ip + ":" + port + "...");
                socket = new Socket(ip, port);
                System.out.println("Connected to server.");
                setupStreams();
                if (onConnect != null) onConnect.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupStreams() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush(); // Send header
        in = new ObjectInputStream(socket.getInputStream());
        connected = true;

        // Start reading thread
        new Thread(this::readLoop).start();
    }

    private void readLoop() {
        while (connected && !socket.isClosed()) {
            try {
                Object obj = in.readObject();
                if (obj instanceof GameStatePacket) {
                    incomingPackets.add((GameStatePacket) obj);
                }
            } catch (EOFException | SocketException e) {
                System.out.println("Connection closed: " + e.getMessage());
                connected = false;
                close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendState(GameStatePacket packet) {
        if (!connected) return;
        try {
            out.writeObject(packet);
            out.flush();
            out.reset(); // Important to avoid caching objects
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
    }

    public GameStatePacket getLatestPacket() {
        return incomingPackets.poll(); // Returns null if empty
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isHost() {
        return isHost;
    }

    public void close() {
        connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Reset state
        out = null;
        in = null;
        socket = null;
        serverSocket = null;
        incomingPackets.clear();
    }
}
