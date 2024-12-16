import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Base64;

// 채팅방 창 클래스: 개별 채팅방 UI와 기능을 담당
public class ChatRoomWindow extends JFrame {
    private String roomName;           // 채팅방 이름
    private String userName;           // 사용자 이름
    private String serverIp;           // 서버 IP 주소
    private int serverPort;            // 서버 포트 번호
    private JTextArea chatArea;        // 채팅 메시지 표시 영역
    private JTextField messageField;   // 메시지 입력 필드
    private JButton sendButton;        // 메시지 전송 버튼
    private Socket socket;             // 서버와의 연결 소켓
    private PrintWriter out;           // 서버로 메시지를 보내는 출력 스트림
    private BufferedReader in;         // 서버로부터 메시지를 읽는 입력 스트림
    private JButton attachButton;      // 첨부파일 버튼 (클립모양)

    // 생성자: 채팅방 UI를 설정하고 서버에 연결
    public ChatRoomWindow(String roomName, String userName, String serverIp, int serverPort) {
        this.roomName = roomName;
        this.userName = userName;
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        // 채팅방 UI 설정
        this.setTitle(roomName); // 채팅방 제목 설정
        this.setSize(380, 600);  // 창 크기 설정
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 메인 패널
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(186, 206, 224));

        // 헤더 패널: 채팅방 제목 및 아이콘
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel titleLabel = new JLabel(roomName);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        // 오른쪽 아이콘 버튼 (검색, 전화, 메뉴)
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightButtons.setBackground(Color.WHITE);
        String[] icons = {"\uD83D\uDD0D", "☎", "≡"};
        for (String icon : icons) {
            JButton btn = new JButton(icon);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            rightButtons.add(btn);
        }

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightButtons, BorderLayout.EAST);

        // 채팅 메시지 영역
        chatArea = new JTextArea();
        chatArea.setEditable(false); // 읽기 전용
        chatArea.setLineWrap(true);  // 자동 줄바꿈
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(186, 206, 224));
        chatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(chatArea);

        // 입력 및 버튼 패널
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 첨부 파일 버튼 (클립 이모지)
        JPanel attachPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        attachPanel.setBackground(Color.WHITE);
        attachButton = new JButton("\uD83D\uDCCE");
        attachButton.setBorderPainted(false);
        attachButton.setContentAreaFilled(false);
        attachButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        attachButton.addActionListener(e -> sendFile()); // 파일 전송 이벤트 리스너 추가
        attachPanel.add(attachButton);

        // 메시지 입력 필드와 전송 버튼
        JPanel messagePanel = new JPanel(new BorderLayout(5, 0));
        messagePanel.setBackground(Color.WHITE);
        messageField = new JTextField();
        messageField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        sendButton = new JButton("전송");
        sendButton.setBackground(new Color(254, 229, 0));
        sendButton.setBorderPainted(false);
        sendButton.setFont(new Font("맑은 고딕", Font.PLAIN, 13));

        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        inputPanel.add(attachPanel, BorderLayout.NORTH);
        inputPanel.add(messagePanel, BorderLayout.SOUTH);

        // 메인 패널 조립
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        this.setContentPane(mainPanel);
        this.setLocationRelativeTo(null); // 창을 화면 중앙에 위치

        // 이벤트 리스너 설정
        messageField.addActionListener(e -> sendMessage()); // 엔터키 입력 시 메시지 전송
        sendButton.addActionListener(e -> sendMessage());   // 전송 버튼 클릭 시 메시지 전송

        connectToServer(); // 서버에 연결
    }

    // 서버에 연결하는 메서드
    private void connectToServer() {
        try {
            socket = new Socket(serverIp, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true); // 출력 스트림
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 입력 스트림
            out.println("ENTER|" + roomName + "|" + userName); // 채팅방 입장 메시지 전송
            new Thread(this::receiveMessages).start(); // 메시지 수신 스레드 시작
        } catch (IOException e) {
            appendMessage("서버 연결 실패: " + e.getMessage());
        }
    }

    // 파일 전송 메서드
    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                String encodedFile = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath())); // 파일을 Base64로 인코딩
                String fileName = file.getName();
                out.println("FILE|" + roomName + "|" + userName + "|" + fileName + "|" + encodedFile);
                appendMessage("나: 파일을 전송했습니다. (" + fileName + ")");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "파일 전송 실패", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 서버에서 메시지 수신
    private void receiveMessages() {
        while (true) {
            try {
                String message = in.readLine();
                if (message != null) {
                    if (message.startsWith("FILE")) {
                        handleIncomingFile(message);
                    } else {
                        SwingUtilities.invokeLater(() -> appendMessage(message));
                    }
                }
            } catch (IOException e) {
                appendMessage("서버와 연결이 끊어졌습니다.");
                break;
            }
        }
    }

    // 수신된 파일 처리
    private void handleIncomingFile(String message) {
        String[] parts = message.split("\\|", 5);
        String sender = parts[2];
        String fileName = parts[3];
        String fileContent = parts[4];

        if (!sender.equals(userName)) {
            int option = JOptionPane.showConfirmDialog(
                    this, sender + "님이 파일을 보냈습니다: " + fileName + "\n저장하시겠습니까?", "파일 수신", JOptionPane.YES_NO_OPTION
            );

            if (option == JOptionPane.YES_OPTION) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(fileName));
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try {
                        File saveFile = fileChooser.getSelectedFile();
                        Files.write(saveFile.toPath(), Base64.getDecoder().decode(fileContent));
                        appendMessage(sender + ": 파일이 저장되었습니다. (" + fileName + ")");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "파일 저장 실패", "오류", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    // 채팅 메시지를 화면에 추가
    public void appendMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // 메시지 전송
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            out.println("MESSAGE|" + roomName + "|" + userName + "|" + message);
            messageField.setText("");
        }
    }
}
