import javax.swing.*; // GUI 컴포넌트를 위한 스윙 라이브러리 임포트
import java.awt.*; // AWT 그래픽 구성 요소 라이브러리 임포트
import java.awt.event.*; // 이벤트 처리를 위한 라이브러리 임포트
import java.util.HashMap; // HashMap 자료구조 임포트 (채팅방 관리를 위해 사용)

// 채팅 애플리케이션 메인 화면 클래스
public class ChatAppMain extends JFrame {
    private JList<String> chatRoomList; // 채팅방 목록을 표시하는 JList 컴포넌트
    private DefaultListModel<String> chatRoomModel; // 채팅방 목록의 데이터 모델
    private HashMap<String, ChatRoomWindow> openChatRooms; // 열린 채팅방을 관리하는 HashMap (방 이름: 창 객체)
    private String userName; // 사용자 이름
    private String serverIp; // 서버 IP 주소
    private int serverPort; // 서버 포트 번호

    // 생성자: 사용자 이름, 서버 IP, 서버 포트를 인자로 받아 UI 설정 및 초기화
    public ChatAppMain(String userName, String serverIp, int serverPort) {
        this.userName = userName;
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        // 기본 창 설정
        setTitle("채팅방"); // 창 제목 설정
        setSize(280, 600); // 창 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 창 종료 시 애플리케이션 종료

        // 메인 패널 (채팅방 목록 표시)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // 상단 패널 (프로필 및 메뉴 아이콘)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 왼쪽 프로필 아이콘 버튼
        JButton profileBtn = new JButton("\uD83D\uDC64"); // 사용자 프로필 이모지
        profileBtn.setBorderPainted(false); // 버튼 테두리 제거
        profileBtn.setContentAreaFilled(false); // 버튼 배경 제거
        profileBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        // 오른쪽 메뉴 아이콘 (검색 및 옵션)
        JPanel rightIcons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightIcons.setBackground(Color.WHITE);

        JButton searchBtn = new JButton("\uD83D\uDD0D"); // 검색 버튼 이모지
        JButton menuBtn = new JButton("⋮"); // 메뉴 버튼 이모지

        // 검색 및 메뉴 버튼 설정
        for (JButton btn : new JButton[]{searchBtn, menuBtn}) {
            btn.setBorderPainted(false); // 테두리 제거
            btn.setContentAreaFilled(false); // 배경 제거
            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            rightIcons.add(btn);
        }

        // 상단 패널에 프로필 및 메뉴 버튼 추가
        topPanel.add(profileBtn, BorderLayout.WEST);
        topPanel.add(rightIcons, BorderLayout.EAST);

        // 채팅방 목록 데이터 모델 설정
        chatRoomModel = new DefaultListModel<>();
        chatRoomModel.addElement("채팅방1");
        chatRoomModel.addElement("채팅방2");
        chatRoomModel.addElement("채팅방3");

        // 채팅방 목록 JList 설정
        chatRoomList = new JList<>(chatRoomModel);
        chatRoomList.setCellRenderer(new ChatRoomListRenderer()); // 커스텀 렌더러 사용
        chatRoomList.setFixedCellHeight(50); // 각 셀의 높이 설정
        chatRoomList.setSelectionBackground(new Color(0, 102, 255)); // 선택된 항목 배경색
        chatRoomList.setSelectionForeground(Color.WHITE); // 선택된 항목 글자색
        chatRoomList.setBorder(null);

        // 채팅방 더블 클릭 이벤트 리스너 추가
        chatRoomList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // 더블 클릭 이벤트 감지
                    String selectedRoom = chatRoomList.getSelectedValue(); // 선택된 채팅방 이름 가져오기
                    openChatRoom(selectedRoom); // 선택된 채팅방 열기
                }
            }
        });

        // 스크롤 가능한 채팅방 목록
        JScrollPane scrollPane = new JScrollPane(chatRoomList);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        // 메인 패널에 상단 패널과 채팅방 목록 추가
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(mainPanel); // 메인 패널을 콘텐츠 패널로 설정
        setLocationRelativeTo(null); // 화면 중앙에 표시

        // 열린 채팅방 관리용 HashMap 초기화
        openChatRooms = new HashMap<>();
    }

    // main 메서드: 애플리케이션 시작 지점
    public static void main(String[] args) {
        try {
            // 시스템 룩앤필 대신 크로스플랫폼 룩앤필 사용
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            // 버튼 기본 스타일 설정
            UIManager.put("Button.background", new Color(254, 229, 0));
            UIManager.put("Button.foreground", Color.BLACK);
            UIManager.put("Button.font", new Font("맑은 고딕", Font.BOLD, 12));

            // 리스트 스타일 설정
            UIManager.put("List.background", Color.WHITE);
            UIManager.put("List.selectionBackground", new Color(0, 102, 255));
            UIManager.put("List.selectionForeground", Color.WHITE);
            UIManager.put("List.focusCellHighlightBorder", BorderFactory.createEmptyBorder());

            // 창 테두리 스타일 설정
            UIManager.put("Panel.background", Color.WHITE);
            UIManager.put("TextField.background", Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            StartScreen startScreen = new StartScreen(); // 시작 화면 표시
            startScreen.setVisible(true);
        });
    }

    // 채팅방 열기 메서드
    private void openChatRoom(String roomName) {
        if (!openChatRooms.containsKey(roomName)) { // 채팅방이 열려 있지 않은 경우
            ChatRoomWindow chatRoomWindow = new ChatRoomWindow(roomName, userName, serverIp, serverPort); // 채팅방 창 생성
            openChatRooms.put(roomName, chatRoomWindow); // 열린 채팅방 목록에 추가
        } else {
            openChatRooms.get(roomName).setVisible(true); // 이미 열려 있는 경우 해당 창 표시
        }
    }

    // 채팅방 목록 커스텀 렌더러 클래스
    class ChatRoomListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {

            JPanel panel = new JPanel(new BorderLayout(10, 0)); // 셀 패널 설정
            panel.setBackground(isSelected ? new Color(240, 240, 240) : Color.WHITE);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            // 프로필 이미지 (임시로 이모지 사용)
            JLabel profileLabel = new JLabel("\uD83D\uDC64");
            profileLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));

            // 채팅방 이름과 마지막 메시지
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setBackground(panel.getBackground());

            JLabel nameLabel = new JLabel(value.toString());
            nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));

            JLabel messageLabel = new JLabel("마지막 메시지...");
            messageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            messageLabel.setForeground(Color.GRAY);

            textPanel.add(nameLabel);
            textPanel.add(messageLabel);

            panel.add(profileLabel, BorderLayout.WEST); // 왼쪽에 프로필 아이콘
            panel.add(textPanel, BorderLayout.CENTER); // 중앙에 텍스트 패널

            return panel; // 최종 셀 컴포넌트 반환
        }
    }
}
