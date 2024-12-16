import javax.swing.*;
import java.awt.*;

// 애플리케이션 시작 화면 클래스 (로그인 UI)
public class StartScreen extends JFrame {
    private JTextField userNameField;   // 사용자 이름 입력 필드
    private JTextField serverIpField;   // 서버 IP 입력 필드
    private JTextField serverPortField; // 서버 포트 입력 필드

    // 생성자: UI 설정 및 초기화
    public StartScreen() {
        setTitle("카카오톡 로그인"); // 창 제목 설정
        setSize(300, 200); // 창 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창 종료 시 애플리케이션 종료
        setResizable(false); // 창 크기 고정

        // 메인 패널 설정: 그리드 레이아웃 (4행, 2열, 수평 10px, 수직 10px 간격)
        JPanel mainPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        mainPanel.setBackground(new Color(254, 229, 0)); // 카카오톡 노란색 배경
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // 여백 추가

        // 라벨 스타일 설정
        Font labelFont = new Font("맑은 고딕", Font.BOLD, 12);
        JLabel userLabel = new JLabel("사용자 이름:", SwingConstants.CENTER); // 사용자 이름 입력 라벨
        JLabel ipLabel = new JLabel("서버 IP:", SwingConstants.CENTER); // 서버 IP 입력 라벨
        JLabel portLabel = new JLabel("서버 포트:", SwingConstants.CENTER); // 서버 포트 입력 라벨
        userLabel.setFont(labelFont);
        ipLabel.setFont(labelFont);
        portLabel.setFont(labelFont);

        // 입력 필드 설정
        userNameField = new JTextField(); // 사용자 이름 입력 필드
        serverIpField = new JTextField("localhost"); // 기본값: localhost
        serverPortField = new JTextField("12345");   // 기본값: 12345

        // 입력 필드에 여백 추가 (스타일링)
        userNameField.setBorder(BorderFactory.createCompoundBorder(
                userNameField.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        serverIpField.setBorder(BorderFactory.createCompoundBorder(
                serverIpField.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        serverPortField.setBorder(BorderFactory.createCompoundBorder(
                serverPortField.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // 로그인 버튼 설정
        JButton loginButton = new JButton("로그인"); // 로그인 버튼
        loginButton.setBackground(new Color(65, 54, 42)); // 버튼 배경색 (어두운 갈색)
        loginButton.setForeground(Color.WHITE); // 버튼 글자색 (흰색)
        loginButton.setFont(new Font("맑은 고딕", Font.BOLD, 12)); // 글꼴 설정
        loginButton.setFocusPainted(false); // 버튼 선택 테두리 제거
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // 버튼 내부 여백 추가
        loginButton.addActionListener(e -> onLogin()); // 클릭 시 로그인 동작 실행

        // 메인 패널에 컴포넌트 추가 (라벨과 입력 필드)
        mainPanel.add(userLabel);
        mainPanel.add(userNameField);
        mainPanel.add(ipLabel);
        mainPanel.add(serverIpField);
        mainPanel.add(portLabel);
        mainPanel.add(serverPortField);
        mainPanel.add(new JLabel()); // 빈 공간 추가
        mainPanel.add(loginButton); // 로그인 버튼 추가

        // 메인 패널을 프레임에 추가
        add(mainPanel);
        setLocationRelativeTo(null); // 화면 중앙에 창 위치
    }

    // 로그인 버튼 클릭 시 실행되는 메서드
    private void onLogin() {
        // 입력 필드에서 데이터 가져오기
        String userName = userNameField.getText().trim(); // 사용자 이름 가져오기
        String serverIp = serverIpField.getText().trim(); // 서버 IP 가져오기
        int serverPort = Integer.parseInt(serverPortField.getText().trim()); // 서버 포트 가져오기

        // 사용자 이름이 비어있지 않은 경우에만 로그인
        if (!userName.isEmpty()) {
            // ChatAppMain 창 열기 (메인 채팅방 UI)
            ChatAppMain mainWindow = new ChatAppMain(userName, serverIp, serverPort);
            mainWindow.setVisible(true);
            this.dispose(); // 현재 로그인 창 닫기
        } else {
            // 사용자 이름이 비어있으면 경고 메시지 표시
            JOptionPane.showMessageDialog(this, "사용자 이름을 입력해주세요.");
        }
    }
}
