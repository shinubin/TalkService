import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {
    private ServerSocket serverSocket;
    private Map<String, Set<ClientHandler>> roomClients = new HashMap<>(); // 채팅방별 사용자 목록
    private Map<String, String> userCurrentRoom = new HashMap<>();         // 사용자별 현재 채팅방

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("채팅 서버가 포트 " + port + "에서 시작되었습니다.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.out.println("서버 에러: " + e.getMessage());
        }
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String currentRoom = null;
        private String userName = null;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String message;
                while ((message = in.readLine()) != null) {
                    String[] parts = message.split("\\|");
                    String type = parts[0];

                    if (type.equals("ENTER")) {
                        handleEnterRoom(parts[1], parts[2]);
                    } else if (type.equals("MESSAGE")) {
                        broadcastMessage(parts[1], parts[2], parts[3], false); // 일반 메시지
                    } else if (type.equals("FILE")) {
                        handleFileTransfer(parts[1], parts[2], parts[3], parts[4]);
                    } else if (type.equals("LEAVE")) {
                        leaveRoom();
                    }
                }
            } catch (IOException e) {
                System.out.println("클라이언트 처리 에러: " + e.getMessage());
            } finally {
                leaveRoom();
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleEnterRoom(String roomName, String user) {
            currentRoom = roomName;
            userName = user;

            synchronized (roomClients) {
                roomClients.computeIfAbsent(roomName, k -> new HashSet<>()).add(this);
                userCurrentRoom.put(userName, roomName); // 사용자 상태 갱신
            }
            // 입장 메시지: 읽음 표시 제외
            broadcastMessage(roomName, "SERVER", userName + "님이 입장하셨습니다.", true);
        }

        private void leaveRoom() {
            if (currentRoom != null) {
                synchronized (roomClients) {
                    roomClients.get(currentRoom).remove(this);
                    userCurrentRoom.remove(userName); // 사용자 상태 제거
                    broadcastMessage(currentRoom, "SERVER", userName + "님이 퇴장하셨습니다.", true);
                    currentRoom = null;
                }
            }
        }

        private void handleFileTransfer(String roomName, String sender, String fileName, String fileContent) {
            synchronized (roomClients) {
                if (roomClients.containsKey(roomName)) {
                    Set<ClientHandler> clients = roomClients.get(roomName);

                    // 읽음 상태 추가
                    StringBuilder readStatus = new StringBuilder();
                    for (ClientHandler client : clients) {
                        if (!client.userName.equals(sender) &&
                                userCurrentRoom.get(client.userName).equals(roomName)) {
                            readStatus.append(client.userName).append(" ");
                        }
                    }

                    String readStatusMessage = readStatus.length() > 0 ? "(" + readStatus.toString().trim() + "읽음)" : "";
                    String fileBroadcastMsg = "SERVER: " + sender + "님이 파일을 전송했습니다: " + fileName + " " + readStatusMessage;

                    // 파일 메시지 전송
                    String fileMessage = "FILE|" + roomName + "|" + sender + "|" + fileName + "|" + fileContent;

                    for (ClientHandler client : clients) {
                        client.out.println(fileMessage); // 파일 전송
                    }

                    // 읽음 상태 포함 알림 메시지
                    for (ClientHandler client : clients) {
                        client.out.println(fileBroadcastMsg);
                    }
                }
            }
        }


        // 메시지 브로드캐스트: 시스템 메시지는 읽음 표시 제외
        private void broadcastMessage(String roomName, String sender, String message, boolean isSystem) {
            if (roomClients.containsKey(roomName)) {
                Set<ClientHandler> clients = roomClients.get(roomName);

                String readMessage = sender + ": " + message;

                if (!isSystem) { // SYSTEM 메시지가 아니면 읽음 표시 추가
                    StringBuilder readStatus = new StringBuilder();
                    for (ClientHandler client : clients) {
                        if (!client.userName.equals(sender) &&
                                userCurrentRoom.get(client.userName).equals(roomName)) {
                            readStatus.append(client.userName).append(" ");
                        }
                    }
                    if (readStatus.length() > 0) {
                        readMessage += " (" + readStatus.toString().trim() + "읽음)";
                    }
                }

                // 모든 클라이언트에게 메시지 전송
                for (ClientHandler client : clients) {
                    client.out.println(readMessage);
                }
            }
        }
    }

    public static void main(String[] args) {
        int port = 12345; // 기본 포트
        ChatServer server = new ChatServer();
        server.start(port);
    }
}

