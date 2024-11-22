import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme;
import com.sun.tools.javac.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.desktop.SystemSleepEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

class InternetServer {
    public static ArrayList<Clients_List> Clients= new ArrayList<>();
    public void main(String Args[]) throws IOException, UnsupportedLookAndFeelException {
        ServerSocket serverForClient = null;
        try {
            serverForClient=new ServerSocket(6606);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Socket socketOnServer= null;
        DataInputStream dis=null;
        System.out.println("服务端已启动，请启动客服端以连接");

        FlatLightLaf.setup();
        UIManager.setLookAndFeel( new FlatLightLaf());
        UIManager.put( "Button.arc", 40 );
        UIManager.put( "Component.arc", 40 );

        Server_UI server_ui = new Server_UI();
        Time_Now now = new Time_Now();
        while(true)
        {
            boolean logged = false;
            while(logged==false) {
                try {
                    socketOnServer = serverForClient.accept();
                    System.out.println("有新客户端加入");
                    System.out.println("客户端地址：" + socketOnServer.getInetAddress());
                    System.out.println("客户的端口：" + socketOnServer.getPort());

                    Color apply = new Color(227,227,227);

                    server_ui.insertText(now.get()+"\n", Color.GRAY, 12, StyleConstants.ALIGN_CENTER);
                    server_ui.insertTextWithBubble("有新客户端申请登录！\n",Color.WHITE, apply,12,StyleConstants.ALIGN_CENTER);
                    server_ui.insertText("客户端地址:"+socketOnServer.getInetAddress().toString()+"\t客户端端口:"+socketOnServer.getPort()+"\n",Color.BLACK,12,StyleConstants.ALIGN_CENTER);


                    dis = new DataInputStream(socketOnServer.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    String state = dis.readUTF();
                    if (state.equals("Connected")) {
                        logged = true;
                    }
                } catch (IOException e) {
                    System.out.println("等待状态");
                }
            }

            System.out.println(socketOnServer.getPort()+"已成功登录");
            String user_name;
            try{user_name = dis.readUTF();}
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            Clients.add(new Clients_List(socketOnServer,user_name));
            server_ui.insertText(now.get()+"\n",  Color.GRAY, 12, StyleConstants.ALIGN_CENTER);
            server_ui.insertImageWithAlignment(new Profile(user_name).chat_icon,StyleConstants.ALIGN_CENTER);
            Color online = new Color(23,221,132);
            server_ui.insertTextWithBubble("账号: "+user_name+" 已成功登录\n",Color.WHITE, online,16,StyleConstants.ALIGN_CENTER);
            server_ui.insertText("客户端地址:"+socketOnServer.getInetAddress().toString()+"\t客户端端口"+socketOnServer.getPort()+"\n",Color.BLACK,12,StyleConstants.ALIGN_CENTER);

            System.out.println("在线人数:"+Clients.size());

            sendOnlineUserListToAllClients();//更新服务器列表

            server_ui.ClearOnlineUsers();
            for(int i=0;i<Clients.size();i++)
            {
                server_ui.addOnlineUsers(Clients.get(i).user_name);
            }

            new Thread(new ServerReceiveThread(socketOnServer,server_ui)).start();

        }

    }
    static void sendOnlineUserListToAllClients()  {
        for(int i=0;i<Clients.size();i++)
        {
            try {
                DataOutputStream dos = new DataOutputStream(Clients.get(i).sk.getOutputStream());
                dos.writeUTF("INFORMATION:UPDATE_LIST");
                dos.write(Clients.size());//发送的是用户的总量
                for(int j=0;j<Clients.size();j++)
                {
                    String user_name = Clients.get(j).user_name;//群发新的用户列表
                    dos.writeUTF(user_name);
                }
                System.out.println("更新列表");
            }
            catch (Exception e)
            {e.printStackTrace();}
        }
    }
}

class Server_UI extends JFrame {
    static JTextPane Message_Area;
    static JScrollPane scrollPane;
    static StyledDocument doc;
    static JFrame frame = null;
    static Socket socket = null;
    static JScrollPane userScrollPane = null;
    static DefaultListModel<String> userListModel = new DefaultListModel<>();
    static JList<String> userList = null;
    static ArrayList<ImageIcon> avatars = new ArrayList<>();
    Time_Now now = new Time_Now();

    Server_UI() throws IOException {
        userList = new JList<>(userListModel);

        frame = new JFrame("线聊服务器端");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        ImageIcon icon = new ImageIcon("./icon/icon.png"); // 替换为你的图标路径
        Image image = icon.getImage(); // 获取图像
        frame.setIconImage(image); // 设置窗口图标

        Font yahei_20 = new Font("Microsoft Yahei", Font.PLAIN, 20);


        Profile user_profile = new Profile("Admin");
        ImageIcon head_icon = user_profile.head_icon;
        JLabel h_i = new JLabel(head_icon);
        JLabel user_name = new JLabel("服务器端");
        user_name.setFont(yahei_20);
        JPanel User_Info = new JPanel();
        User_Info.setLayout(new FlowLayout());
        User_Info.add(h_i);
        User_Info.add(user_name);

        constraints.gridx =0;
        constraints.gridy = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.4; // 让 splitPane 在水平方向上扩展
        constraints.weighty = 1.0; // 让 splitPane 在垂直方向上扩展
        frame.add(User_Info, constraints);

        frame.repaint();

        Message_Area = new JTextPane();
        Message_Area.setEditable(false);
        Message_Area.setFont(yahei_20);
        scrollPane = new JScrollPane(Message_Area);
        doc = Message_Area.getStyledDocument();
        Color back_c = new Color(245, 245, 247);
        Message_Area.setBackground(back_c);

        Font yahei_14 = new Font("Microsoft Yahei", Font.PLAIN, 14);
        JTextArea Type_Area = new JTextArea();
        Type_Area.setFont(yahei_14);
        Type_Area.setLineWrap(true);
        Type_Area.setWrapStyleWord(true);
        Type_Area.setPreferredSize(new Dimension(600,200));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(scrollPane);

        ImageIcon sendIcon = new ImageIcon(getClass().getResource("./icon/send.png"));
        JButton Send_Button = new JButton(sendIcon);
        Color send_c = new Color(0, 153, 255);
        Send_Button.setBackground(send_c);
        Send_Button.setPreferredSize(new Dimension(100, 40));

        JPanel Private_Panel = new JPanel();
        Private_Panel.setLayout(new FlowLayout());
        Private_Panel.add(Send_Button);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(Type_Area,BorderLayout.CENTER);
        bottomPanel.add(Private_Panel,BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        // 将面板添加到 JSplitPane
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);
        splitPane.setDividerLocation(0.8);
        splitPane.setResizeWeight(0.8);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridheight = 2;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0; // 让 splitPane 在水平方向上扩展
        constraints.weighty = 1.0; // 让 splitPane 在垂直方向上扩展
        frame.add(splitPane, constraints);


        Send_Button.addActionListener(e -> {
            if (Type_Area.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "发送的消息不能为空", "提示", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Thread st = new Thread(new ServerSendThread(this,Type_Area.getText()));
                st.start();
                Type_Area.setText("");
            }
        });

        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 自定义单元格渲染器
        userList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel panel = new JPanel(new BorderLayout(10, 0));
                panel.setOpaque(false);

                JLabel label = new JLabel();
                label.setIcon(avatars.get(index));
                label.setHorizontalAlignment(JLabel.CENTER);
                panel.add(label, BorderLayout.WEST);

                JLabel userNameLabel = new JLabel(value.toString());
                userNameLabel.setHorizontalAlignment(JLabel.CENTER);
                panel.add(userNameLabel, BorderLayout.CENTER);

                JButton addButton = new JButton("踢出用户");
                addButton.setMargin(new Insets(0, 0, 0, 0));
                panel.add(addButton, BorderLayout.EAST);
                addButton.addActionListener(e -> {
                    int response = JOptionPane.showConfirmDialog(null, "确定要踢出用户 " + value.toString() + " 吗?", "确认踢出",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.YES_OPTION) {
                        // 假设sendKickCommand是一个方法，接收用户名或用户ID作为参数
                        try {
                            sendKickCommand(value.toString());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
                return panel;
            }
        });
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = userList.locationToIndex(e.getPoint());
                if (index != -1) {
                    // 获取单元格的边界
                    Rectangle cellBounds = userList.getCellBounds(index, index);
                    if (cellBounds.contains(e.getPoint())) {
                        // 检测点击位置是否在按钮区域内
                        int buttonWidth = 80; // 假设按钮宽度为80
                        int buttonX = cellBounds.x + cellBounds.width - buttonWidth;

                        if (e.getX() > buttonX) {
                            // 弹出确认对话框
                            int response = JOptionPane.showConfirmDialog(null, "确定要踢出用户 " + userList.getModel().getElementAt(index) + " 吗?", "确认踢出",
                                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                            // 处理对话框的响应
                            if (response == JOptionPane.YES_OPTION) {
                                try {
                                    Color apply = new Color(227,227,227);
                                    insertText(now.get()+"\n",  Color.GRAY, 12, StyleConstants.ALIGN_CENTER);
                                    insertTextWithBubble("你将用户"+userList.getModel().getElementAt(index).toString()+"踢出\n",Color.WHITE, apply,12,StyleConstants.ALIGN_CENTER);
                                    sendKickCommand(userList.getModel().getElementAt(index).toString());
                                    System.out.println("用户 " + userList.getModel().getElementAt(index) + " 已被踢出。");
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                    throw new RuntimeException("踢出用户时发生错误", ex);
                                }
                            }
                        }
                    }
                }
            }
        });

        DefaultListModel<String> finalUserListModel = userListModel;
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = userList.locationToIndex(e.getPoint());

            }
        });
        // 为用户列表添加滚动面板
        userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(200, 400));

        JPanel left_panel = new JPanel();
        left_panel.setLayout(new FlowLayout());
        left_panel.add(User_Info);
        left_panel.add(userScrollPane);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setTopComponent(left_panel);
        mainSplitPane.setBottomComponent(splitPane);

        // 创建新的水平分割的 JSplitPane
        JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        horizontalSplitPane.setLeftComponent(left_panel);
        horizontalSplitPane.setRightComponent(splitPane);
        horizontalSplitPane.setDividerLocation(0.2);  // 你可以调整这个比例
        horizontalSplitPane.setResizeWeight(0.2);     // 左侧面板占据的比例

        // 将水平分割的 JSplitPane 添加到 frame 中
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridheight = 1;
        constraints.gridwidth = 2;  // 跨越两列，如果你使用的是 GridBagLayout
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;

        frame.add(horizontalSplitPane, constraints);


        InitChat();

        insertTextWithBubble("服务端已于" + now.get() + "启动\n", Color.BLACK, Color.RED, 12, StyleConstants.ALIGN_CENTER);

        FileWriter fw = new FileWriter(new File("./Chats/Server.txt"),true);
        fw.write(now.get()+"\n"+"Bootup"+"\n"+"服务端已于" + now.get() + "启动\n");
        fw.flush();


    }

    void InitChat() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File("./Chats/Server.txt")));
        String content;
        String former_time = null;
        boolean first_msg = true;
        while((content=br.readLine())!=null)
        {
            if(!content.equals("Group")&&!content.equals("Private")&&!content.equals("Bootup"))
            {
                    insertText(content+"\n", Color.GRAY, 12, StyleConstants.ALIGN_CENTER);
            }
            else if(content.equals("Bootup"))
            {
                content=br.readLine();
                insertTextWithBubble(content+"\n", Color.BLACK, Color.RED, 12, StyleConstants.ALIGN_CENTER);
            }
            else if(content.equals("Group"))
            {
                String user_name = br.readLine();
                Profile pf =new Profile(user_name);

                    insertImageWithAlignment(pf.chat_icon,StyleConstants.ALIGN_LEFT);
                    String msg;
                    insertText(" "+user_name+":\n",Color.BLACK,16,StyleConstants.ALIGN_LEFT);
                    while(!(msg=br.readLine()).equals("EOF"))
                    {
                        insertTextWithBubble("  "+msg,Color.WHITE,new Color(0, 153, 255),16, StyleConstants.ALIGN_LEFT);

                    }
                    insertText("\n",Color.black,16,StyleConstants.ALIGN_LEFT);
            }
            else if(content.equals("Private"))
            {
                String This_user_name = br.readLine();
                String target_user_name = br.readLine();
                Profile pf1 = new Profile(This_user_name);
                Profile pf2 = new Profile(target_user_name);
                insertImageWithAlignment(pf1.chat_icon,StyleConstants.ALIGN_LEFT);//头像
                insertText(This_user_name+" 给 ",Color.BLACK,16,StyleConstants.ALIGN_LEFT);
                insertImageWithAlignment(pf2.chat_icon,StyleConstants.ALIGN_LEFT);//头像
                insertText(target_user_name+" ",Color.BLACK,16,StyleConstants.ALIGN_LEFT);
                insertText("私发了一条消息: \n",Color.BLACK,16,StyleConstants.ALIGN_LEFT);
                String msg=null;
                while(!(msg=br.readLine()).equals("EOF"))
                {
                    insertTextWithBubble(msg+" \n",Color.WHITE,Color.ORANGE,16,StyleConstants.ALIGN_LEFT);

                }

            }
        }
    }

    public void addOnlineUsers(String newOnlineUsers) {
        // 清空当前列表
        System.out.println("正在更新" + newOnlineUsers);
        avatars.add(new Profile(newOnlineUsers).chat_icon);
        userListModel.addElement(newOnlineUsers);
        // 如果需要，可以在这里更新头像数组或其他相关数据
    }

    public void ClearOnlineUsers() {
        //初始
        avatars.clear();
        userListModel.clear();
    }

    void insertText(String text, Color colorName, int textSize, int textAlign) {
        SimpleAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setForeground(set, colorName);
        StyleConstants.setFontSize(set, textSize);
        StyleConstants.setAlignment(set, textAlign);
        doc.setParagraphAttributes(Message_Area.getText().length(), doc.getLength() - Message_Area.getText().length(), set, false);
        try {
            doc.insertString(doc.getLength(), text, set);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    void insertTextWithBubble(String text, Color textColor, Color bubbleColor, int textSize, int textAlign) {
        SimpleAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setForeground(set, textColor);
        StyleConstants.setFontSize(set, textSize);
        StyleConstants.setAlignment(set, textAlign);

        // 设置背景颜色（这里使用高亮颜色来模拟气泡效果）
        StyleConstants.setBackground(set, bubbleColor);
        // 设置左右边距来模拟气泡的圆角效果（这里以20为例）
        StyleConstants.setLeftIndent(set, 20);
        StyleConstants.setRightIndent(set, 20);
        // 设置段落间距
        StyleConstants.setSpaceAbove(set, 10);
        StyleConstants.setSpaceBelow(set, 10);

        try {
            doc.insertString(doc.getLength(), text, set);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    void insertImage(ImageIcon icon) {
        Style style = doc.addStyle("IconStyle", null);
        StyleConstants.setIcon(style, icon);
        try {
            doc.insertString(doc.getLength(), "\u200B", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    void insertImageWithAlignment(ImageIcon icon, int alignment) {
        // 创建一个新的样式来设置图像的对齐方式
        Style style = doc.addStyle("AlignedIconStyle", null);
        StyleConstants.setIcon(style, icon);
        // 这里我们不直接设置对齐方式，而是在插入文本时通过额外的空格来控制

        try {
            // 根据对齐方式插入不同数量的空格来模拟对齐效果
            String spaces = (alignment == StyleConstants.ALIGN_LEFT) ? "" : "                    "; // 右边对齐时插入空格
            doc.insertString(doc.getLength(), spaces + "\u200B", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    void  sendKickCommand(String user_name) throws IOException {
        int i;
        for(i=0;i< InternetServer.Clients.size();i++)
        {
            if(InternetServer.Clients.get(i).user_name.equals(user_name))
            {
                break;
            }
        }
        DataOutputStream dos = new DataOutputStream(InternetServer.Clients.get(i).sk.getOutputStream());
        dos.writeUTF("INFORMATION:KICKOFF");


    }


}

class ServerReceiveThread extends InternetServer implements Runnable
{
    Socket ThisClientSocket=null;
    int ThisClientPort;
    String This_user_name;
    DataInputStream dis = null;
    DataOutputStream dos =null;
    int This_Array_index=0;
    Server_UI server_ui = null;
    Time_Now now = new Time_Now();
    File file = new File("./Chats/Server.txt");
    FileWriter fw = new FileWriter(file,true);
    ServerReceiveThread(Socket sfc,Server_UI server_ui) throws IOException {
        try {
            ThisClientSocket=sfc;
            ThisClientPort=ThisClientSocket.getPort();
            dis = new DataInputStream(ThisClientSocket.getInputStream());
            dos = new DataOutputStream(ThisClientSocket.getOutputStream());

            this.server_ui = server_ui;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run()
    {
        while(true)
        {

            try {
                for(int i=0;i<Clients.size();i++)
                {
                    if(Clients.get(i).port==ThisClientPort)
                    {
                        This_Array_index=i;
                        break;
                    }
                }
                //获取这个端口的用户名，在Arraylist的第几个位置
                This_user_name=Clients.get(This_Array_index).user_name;
                String get_opreate;
                get_opreate= dis.readUTF();//获取这个人发送的请求

                System.out.println(ThisClientPort+"("+This_user_name+")"+"发送请求："+get_opreate);

                if(get_opreate.equals("A"))
                {
                    group_send();//群发
                }
                else if(get_opreate.equals("B"))
                {
                    String get_contact_user=null;
                    try {
                        get_contact_user=dis.readUTF();//获取要私发的人的名字
                        System.out.println("要私发的人的名字是"+get_contact_user);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int target = find_user(get_contact_user);//获取要私发这个人的List的下标
                    System.out.println("下标为"+target);
                    if(target==-1)
                    {
                        System.out.println("请求失败，未找到目标用户");
                        dos.writeUTF("ERROR:NOT_FIND_TARGET_USER");
                        //dis.readUTF();
                    }
                    else if(target==-2)
                    {
                        System.out.println("请求失败，不能对自己私发消息");
                        dos.writeUTF("ERROR:CANT'T_CHAT_WITH_YOU");
                        //dis.readUTF();
                    }
                    else
                    {
                        System.out.println("请求成功，找到目标用户");
                        dos = new DataOutputStream(Clients.get(this.This_Array_index).sk.getOutputStream());
                        dos.writeUTF("INFORMATION:FINDED_TARGET_USER");
                        String state = dis.readUTF();//获取状态
                        System.out.println("状态吗为"+state);
                        if(state.equals("CONTINUE"))
                        {
                             private_send(target);
                        }
                        else
                        {
                            dos.writeUTF("REJECT");//发送拒绝状态
                            continue;
                        }
                    }
                }
                else if(get_opreate.equals("INFORMATION:ADD_FRIENDS"))
                {
                    System.out.println("请求加好友");
                    add_friends();
                }
                else if(get_opreate.equals("INFORMATION:ACCEPT"))
                {
                    System.out.println("同意添加好友");
                    String Applier = dis.readUTF();
                    int Applier_index = find_user(Applier);//给申请者回个消息
                    System.out.println("下标为"+Applier_index);
                    DataOutputStream dos = new DataOutputStream(Clients.get(Applier_index).sk.getOutputStream());
                    dos.writeUTF("INFORMATION:ACCEPT");//给申请者客户端发消息
                    dos.writeUTF(this.This_user_name);
                }
                else if(get_opreate.equals("INFORMATION:REJECT"))
                {
                    System.out.println("拒绝添加好友");
                    String Applier = dis.readUTF();
                    int Applier_index = find_user(Applier);//给申请者回个消息
                    System.out.println("下标为"+Applier_index);
                    DataOutputStream dos = new DataOutputStream(Clients.get(Applier_index).sk.getOutputStream());
                    dos.writeUTF("INFORMATION:REJECT");//给申请者客户端发消息
                    dos.writeUTF(this.This_user_name);
                }

            } catch (IOException e) {
                e.printStackTrace();
                delete();

                return;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }



    void add_friends()
    {
        try {
            String Applier = dis.readUTF();//先是接收申请者的的名字
            String Target = dis.readUTF();//再接收要得到申请的名字

            int index = find_user(Target);//寻找目标用户的下标
            System.out.println("下标为"+index);
            dos = new DataOutputStream(Clients.get(index).sk.getOutputStream());
            dos.writeUTF("INFORMATION:FRIEND_APPLY");//给目标用户发送好友申请通知
            dos.writeUTF(Applier);


        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    void delete()
    {
        int This_Array_index=0;
        for(int i=0;i<Clients.size();i++)
        {
            if(Clients.get(i).port==ThisClientPort)
            {
                This_Array_index=i;
                break;
            }
        }
        System.out.println(ThisClientPort+"("+This_user_name+")"+"已断开链接");
        Clients.remove(This_Array_index);

        server_ui.ClearOnlineUsers();
        for(int i=0;i<Clients.size();i++)
        {
            System.out.println(i);
            server_ui.addOnlineUsers(Clients.get(0).user_name);
        }

        System.out.println("当前用户个数为"+Clients.size());
        server_ui.insertText(now.get()+"\n",  Color.GRAY, 12, StyleConstants.ALIGN_CENTER);
        server_ui.insertImageWithAlignment(new Profile(This_user_name).chat_icon,StyleConstants.ALIGN_CENTER);
        server_ui.insertTextWithBubble("账号: "+This_user_name+" 已离线\n",Color.WHITE, Color.GRAY,16,StyleConstants.ALIGN_CENTER);
        server_ui.insertText("客户端地址:"+Clients.get(This_Array_index).sk+"\t客户端端口"+ThisClientPort+"\n",Color.BLACK,12,StyleConstants.ALIGN_CENTER);

        //让其他人更新列表

        for(int i=0;i<Clients.size();i++)
        {

            try {
                DataOutputStream dos = new DataOutputStream(Clients.get(i).sk.getOutputStream());
                dos.writeUTF("INFORMATION:UPDATE_LIST");
                dos.write(Clients.size());
                for(int j=0;j<Clients.size();j++)
                {
                    String user_name = Clients.get(j).user_name;//群发新的用户列表
                    dos.writeUTF(user_name);

                }
                System.out.println("更新列表");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }


    }
    void group_send() throws IOException, InterruptedException {
        String get_msg=null;
        String fileName=null;
        try {
            get_msg=dis.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if(get_msg.equals("DOCUMENT"))
        {
            System.out.println("准备接收文件");
            try {
                fileName = dis.readUTF(); // 接收文件名
                get_msg="发送了个文件 "+fileName;
                System.out.println("接收到的文件名: " + fileName);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
// 读取文件内容，直到遇到"EOF"标记
                boolean eofReceived = false;
                while (!eofReceived) {
                    bytesRead = dis.read(buffer);
                    if (bytesRead == -1) {
                        break; // 连接关闭，跳出循环
                    }
                    String received = new String(buffer, 0, bytesRead, "UTF-8");
                    System.out.println(received);
                    if (received.contains("EOF")) {
                        eofReceived = true; // 接收到EOF，设置标记并跳出循环
                        break;
                    } else {
                        baos.write(buffer, 0, bytesRead); // 否则，将读取的数据写入到ByteArrayOutputStream中
                    }
                    System.out.println("正在从客户端读取文件");
                }
                System.out.println("读取成功");
                byte[] fileContent = baos.toByteArray();
                for (int i = 0; i < Clients.size(); i++) {
                        dos = new DataOutputStream(Clients.get(i).sk.getOutputStream());
                        dos.writeUTF("INFORMATION:GROUP");//给客户端发起接收提醒，这是群发消息
                        dos.writeUTF("DOCUMENT");
                        dos.writeUTF(fileName);
                        DataOutputStream otherDos = new DataOutputStream(Clients.get(i).sk.getOutputStream());
                        otherDos.write(fileContent); // 发送整个文件内容
                        otherDos.flush(); // 确保数据被发送出去
                        otherDos.writeUTF("EOF");//标记为EOF
                        otherDos.flush();
                        System.out.println("已发送文件给: " + Clients.get(i).user_name);
                }
                dos.flush();
                get_msg = "发送了个文件 " + fileName;
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        System.out.println(ThisClientPort+"("+This_user_name+")"+"发了一条消息："+get_msg);
        Color send_c = new Color(0, 153, 255);
        Profile pf = new Profile(This_user_name);
        server_ui.insertText(now.get()+"\n", Color.GRAY, 12, StyleConstants.ALIGN_CENTER);
        server_ui.insertImageWithAlignment(pf.chat_icon,StyleConstants.ALIGN_LEFT);//头像
        server_ui.insertText(This_user_name+" : \n",Color.BLACK,16,StyleConstants.ALIGN_LEFT);
        server_ui.insertTextWithBubble(" "+get_msg+" \n",Color.WHITE,send_c,16,StyleConstants.ALIGN_LEFT);

        fw.write(now.get()+"\n");
        fw.write("Group\n"+This_user_name+"\n"+get_msg+"\n"+"EOF"+"\n");
        fw.flush();
        //开始转发消息
        Thread.sleep(300);
        for(int i=0;i<Clients.size();i++)
        {
            try {
                dos = new DataOutputStream(Clients.get(i).sk.getOutputStream());
                dos.writeUTF("INFORMATION:GROUP");//给客户端发起接收提醒，这是群发消息
                if(Clients.get(i).user_name!=This_user_name)//不是发送者
                {
                    //System.out.println("不是发送者");
                    dos.writeUTF("INFORMATION:NOT_SENDER");
                    if(get_msg.equals("DOCUMENT"))
                    {
                        get_msg = "发送了个文件 " + fileName;
                    }
                    dos.writeUTF(This_user_name);//发送发送者的名字
                    dos.writeUTF(get_msg);
                    //System.out.println("给非发送者发了");
                }
                else
                {
                    //System.out.println("是发送者");
                    dos.writeUTF("INFORMATION:SENDER");
                    if(get_msg.equals("DOCUMENT"))
                    {
                        get_msg = "发送了个文件 " + fileName;
                    }
                    dos.writeUTF(get_msg);
                    dos.writeUTF(This_user_name);//发送发送者的名字
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }//给所有成员转发此消息
        System.out.println("已将此消息发给所有用户");
    }
    void private_send(int target) throws IOException {
        String get_msg=null;
        int target_user_port = Clients.get(target).port;
        String target_user_name = Clients.get(target).user_name;
        try {
            get_msg=dis.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(get_msg);
        System.out.println(ThisClientPort+"("+This_user_name+")"+"给"+target_user_port+"("+target_user_name+")"+"发了一条消息："+get_msg);

        try {
            dos = new DataOutputStream(Clients.get(target).sk.getOutputStream());
            dos.writeUTF("INFORMATION:PRIVATE");//给客户端发起接收提醒，这是私发消息
            boolean doc = false;
            if (get_msg.equals("DOCUMENT"))
            {
                doc=true;
                System.out.println("准备发送文件");
                dos.writeUTF("DOCUMENT");

                // 发送文件名
                String fileName = dis.readUTF();
                dos.writeUTF(fileName);
                System.out.println("发送的文件名: " + fileName);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                boolean eofReceived = false;
                while (!eofReceived) {
                    bytesRead = dis.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    String received = new String(buffer, 0, bytesRead, "UTF-8");
                    if (received.contains("EOF")) {
                        eofReceived = true;
                        break;
                    } else {
                        baos.write(buffer, 0, bytesRead);
                    }
                }
                byte[] fileContent = baos.toByteArray();
                dos.write(fileContent);
                dos.flush();
                dos.writeUTF("EOF"); // 标记文件结束
                dos.flush();

                get_msg = "发送了个文件 " + fileName;
                System.out.println("文件发送完成");
            }

            Thread.sleep(300);

            dos = new DataOutputStream(Clients.get(target).sk.getOutputStream());
            if(doc==true)
            {dos.writeUTF("INFORMATION:PRIVATE");}
            dos.writeUTF("INFORMATION:RECEIVER");
            dos.writeUTF(This_user_name);//发送发送者的名字
            dos.writeUTF(get_msg);


            dos = new DataOutputStream(Clients.get(This_Array_index).sk.getOutputStream());
            dos.writeUTF("INFORMATION:PRIVATE");
            dos.writeUTF("INFORMATION:SENDER");
            dos.writeUTF(target_user_name);
            dos.writeUTF(This_user_name);//发送发送者的名字
            dos.writeUTF(get_msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Profile pf1 = new Profile(This_user_name);
        Profile pf2 = new Profile(target_user_name);
        server_ui.insertText(now.get()+"\n", Color.GRAY, 12, StyleConstants.ALIGN_CENTER);
        server_ui.insertImageWithAlignment(pf1.chat_icon,StyleConstants.ALIGN_LEFT);//头像
        server_ui.insertText(This_user_name+" 给 ",Color.BLACK,16,StyleConstants.ALIGN_LEFT);
        server_ui.insertImageWithAlignment(pf2.chat_icon,StyleConstants.ALIGN_LEFT);//头像
        server_ui.insertText(target_user_name+" ",Color.BLACK,16,StyleConstants.ALIGN_LEFT);
        server_ui.insertText("私发了一条消息:\n ",Color.BLACK,16,StyleConstants.ALIGN_LEFT);
        server_ui.insertTextWithBubble(get_msg+" \n",Color.WHITE,Color.ORANGE,16,StyleConstants.ALIGN_LEFT);
        //给指定成员转发此消息
        System.out.println("已将此消息发给"+target_user_name);

        fw.write(now.get()+"\n");
        fw.write("Private\n"+This_user_name+"\n"+target_user_name+"\n"+get_msg+"\n"+"EOF"+"\n");
        fw.flush();

    }
    int find_user(String target_user)
    {
        int target_index=-1;
        for(int i=0;i<Clients.size();i++)
        {
            if(Clients.get(i).user_name.equals(target_user))
            {
                target_index=i;
                if(Clients.get(i).user_name.equals(this.This_user_name))
                {
                    target_index=-2;
                }
                break;
            }
        }
        return target_index;
    }
}


class ServerSendThread extends InternetServer implements Runnable {
    Server_UI server_ui = null;
    String send_msg=null;
    Time_Now now = new Time_Now();

    ServerSendThread(Server_UI server_ui,String send_msg)
    {
        this.server_ui = server_ui;
        this.send_msg=send_msg;
    }
    public void run()
    {

            try {
                for(int i=0;i<Clients.size();i++)
                {
                    DataOutputStream dos = null;
                    dos = new DataOutputStream(Clients.get(i).sk.getOutputStream());
                    dos.writeUTF("INFORMATION:SERVER_MESSAGE");
                    dos.writeUTF(send_msg);
                }
                System.out.println(send_msg);
                server_ui.insertText(now.get()+"\n", Color.GRAY, 12, StyleConstants.ALIGN_CENTER);
                server_ui.insertTextWithBubble("————服务器提醒————\n",Color.BLACK, Color.YELLOW,14,StyleConstants.ALIGN_CENTER);
                server_ui.insertTextWithBubble(send_msg+"\n",Color.BLACK, Color.YELLOW,14,StyleConstants.ALIGN_CENTER);
                server_ui.insertTextWithBubble("—————————————\n",Color.BLACK, Color.YELLOW,14,StyleConstants.ALIGN_CENTER);

            } catch (IOException e) {
                e.printStackTrace();
            }

    }
}

