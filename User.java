import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Scanner;

class Clients_List
{

    Socket sk;
    String user_name;
    int port;
    Clients_List(Socket socketOnServer,String user_name)
    {
        this.sk=socketOnServer;
        this.user_name=user_name;
        this.port = sk.getPort();
    }
}

class User extends JFrame {
    Socket sk;
    String user_name;
    String password;
    JLabel labelAvatar;
    Scanner sc = new Scanner(System.in);
    File defaultProfilePicture;
    JLabel profilePictureLabel;
    String selectedImagePath = "";


    User(JFrame frame, Socket sk) {
        this.sk = sk;
        frame.setSize(450, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon icon = new ImageIcon("./icon/icon.png"); // 替换为你的图标路径
        Image image = icon.getImage(); // 获取图像
        frame.setIconImage(image); // 设置窗口图标
        /*
        for(Font allFont : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
        {
            System.out.println(allFont);
        }

         */
        UIManager.put("Label.font", new Font("Microsoft YaHei", Font.PLAIN, 14));//设置全局字体

        JLabel Label_Account = new JLabel("账号");
        Label_Account.setBounds(20, 20, 50, 30);

        JTextField TF_Account = new JTextField();
        TF_Account.setBounds(80, 20, 200, 30);

        labelAvatar = new JLabel();//登录时头像
        Profile defalut = new Profile("null");
        ImageIcon defalut_icon = defalut.login_icon;
        labelAvatar.setIcon(defalut_icon);
        labelAvatar.setVisible(true);
        labelAvatar.setBounds(300, 10, 150, 150);
        frame.add(labelAvatar);

        TF_Account.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                textChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                textChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                textChanged();
            }

            void textChanged() {

                String user_name = TF_Account.getText();
                if (Find_User(user_name) == true) {
                    Profile user_profile = new Profile(user_name);
                    ImageIcon login_icon = user_profile.login_icon;
                    labelAvatar.setIcon(login_icon);
                    labelAvatar.setVisible(true);

                } else {
                    labelAvatar.setIcon(defalut_icon);
                    //labelAvatar.setVisible(false);
                }
                frame.repaint();
            }
        });
        JLabel Label_pwd = new JLabel("密码");//密码框
        Label_pwd.setBounds(20, 60, 50, 30);
        JPasswordField pwd = new JPasswordField();
        pwd.setBounds(80, 60, 200, 30);

        JButton Button_Login = new JButton("登录");
        Button_Login.setBounds(60, 100, 100, 30);
        Button_Login.addActionListener(e -> {
            String get_Account = TF_Account.getText();
            String get_pwd = pwd.getText();
            System.out.println("用户的账号是" + get_Account);
            System.out.println("用户的密码是" + get_pwd);
            if (get_Account.isEmpty() || get_pwd.isEmpty()) {
                System.out.println("账号或密码为空！");
                JOptionPane.showMessageDialog(null, "账号或密码不能为空", "警告", JOptionPane.ERROR_MESSAGE);
            } else {
                if (Find_User(get_Account) == true) {

                    if (Find_pwd(get_Account, get_pwd) == true) {
                        user_name = get_Account;
                        password = get_pwd;
                        System.out.println("验证成功！");
                        frame.setVisible(false);
                        frame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "账号或密码错误，请检查账号或密码", "账号或密码错误", JOptionPane.INFORMATION_MESSAGE);
                        System.out.println("密码错误！");
                    }
                } else {
                    System.out.println("未找到该用户，请注册");
                    JOptionPane.showMessageDialog(null, "未找到此用户", "警告", JOptionPane.ERROR_MESSAGE);

                }
            }

        });
        JButton Button_Register = new JButton("注册");
        Button_Register.setBounds(180, 100, 100, 30);
        Button_Register.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Register_Page();
            }
        });

        frame.add(Label_Account);
        frame.add(TF_Account);
        frame.add(Label_pwd);
        frame.add(pwd);
        frame.add(Button_Login);
        frame.add(Button_Register);
        frame.repaint();
    }

    boolean Find_User(String UID) {
        String filename = "Users.txt";
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            String user;

            while ((user = br.readLine()) != null) {
                if (user.equals(UID)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean Find_pwd(String UID, String pwd) {
        String filename = "Users.txt";
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            String user;

            while ((user = br.readLine()) != null) {
                if (user.equals(UID)) {
                    String correct_pwd;
                    correct_pwd = br.readLine();
                    if (correct_pwd.equals(pwd)) {
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    void Register_Page() {
        JFrame frame = new JFrame("欢迎注册");
        frame.setSize(600, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        frame.setLayout(null);

        JLabel label = new JLabel("注册");
        label.setFont(new Font("微软雅黑", Font.BOLD, 25));
        label.setBounds(160, 0, 50, 50);
        frame.add(label);

        JLabel Label_Account = new JLabel("注册账号");
        Label_Account.setBounds(40, 55, 60, 20);
        frame.add(Label_Account);

        JTextField TF_Account = new JTextField("");
        TF_Account.setBounds(120, 50, 200, 30);
        frame.add(TF_Account);

        JLabel Label_pwd = new JLabel("注册密码");
        Label_pwd.setBounds(40, 100, 60, 20);
        frame.add(Label_pwd);

        JPasswordField pwd = new JPasswordField(20);
        pwd.setBounds(120, 95, 200, 30);
        frame.add(pwd);

        JLabel Re_pwd = new JLabel("重复密码");
        Re_pwd.setBounds(40, 150, 60, 20);
        frame.add(Re_pwd);

        JPasswordField PF_repwd = new JPasswordField(20);
        PF_repwd.setBounds(120, 145, 200, 30);
        frame.add(PF_repwd);

        JButton confirm = new JButton("确认注册");
        confirm.setBounds(140, 200, 100, 40);
        frame.add(confirm);

        confirm.addActionListener(e -> {
            String get_UID = TF_Account.getText();
            String first_pwd = pwd.getText();
            String re_pwd = PF_repwd.getText();
            System.out.println("用户请求注册:" + get_UID);
            System.out.println("用户输入密码:" + first_pwd);
            System.out.println("用户重复密码:" + re_pwd);
            if (get_UID.isEmpty() || first_pwd.isEmpty() || re_pwd.isEmpty()) {
                System.out.println("账号或密码为空！");
                JOptionPane.showMessageDialog(frame, "账号或密码不能为空", "警告", JOptionPane.ERROR_MESSAGE);

            } else {
                if (Find_User(get_UID) == true) {
                    JOptionPane.showMessageDialog(frame, "请更改注册的账号", "账号已存在", JOptionPane.INFORMATION_MESSAGE);
                    System.out.println("账号已存在！");
                } else {
                    if (first_pwd.equals(re_pwd) == false) {
                        JOptionPane.showMessageDialog(frame, "两次输入的密码不同，请检查输入", "密码不同", JOptionPane.INFORMATION_MESSAGE);
                        System.out.println("两次密码不正确！");
                    } else {
                        try {
                            Register(get_UID, first_pwd);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        Modify_User_Profile(get_UID);
                        JOptionPane.showMessageDialog(frame, "注册成功，请返回登录", "注册成功", JOptionPane.INFORMATION_MESSAGE);
                        System.out.println("已成功注册");
                        frame.setVisible(false);
                    }
                }
            }
        });
        profilePictureLabel = new JLabel();
        profilePictureLabel.setBounds(380, 30, 150, 150); // 设置头像显示位置和大小
        frame.add(profilePictureLabel);

        JButton uploadButton = new JButton("上传头像");
        uploadButton.setBounds(400, 200, 100, 40);
        uploadButton.addActionListener(new UploadButtonListener(frame));
        frame.add(uploadButton);

        defaultProfilePicture = new File("./User_Profile/Default.jpg");
        displayDefaultProfilePicture();
    }

    void displayDefaultProfilePicture() {
        if (defaultProfilePicture.exists()) {
            Profile pro = new Profile("default");
            profilePictureLabel.setIcon(pro.login_icon);
        } else {
            System.out.println("Default profile picture not found!");
        }
    }

    void displayProfilePicture(File file,boolean div) {
        if(div==false) {
            Profile pro = new Profile(file.getName().substring(0, file.getName().length() - 4));
            profilePictureLabel.setIcon(pro.login_icon);
        }
        else
        {Profile pro = new Profile(user_name,file);
            profilePictureLabel.setIcon(pro.login_icon);}

    }


    void Register(String UID, String pwd) throws IOException {
        File chat_file = new File("./Chats/"+UID+".txt");
        FileWriter fw1 = new FileWriter(chat_file, true);
        fw1.write("");
        fw1.close();
        String filename = "Users.txt";
        File downloadDir = new File("./User_Download");
        File userDir = new File(downloadDir, UID);
        try {
            FileWriter fw = new FileWriter(filename, true);
            fw.write(UID + "\n" + pwd + "\n");
            fw.close();
            userDir.mkdir();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("注册成功，欢迎！");
    }

    public void Modify_User_Profile(String UID) {
        String USER_PROFILE_DIR = "./User_Profile/";
        File userProfileDir = new File(USER_PROFILE_DIR);
        if (!userProfileDir.exists()) {
            userProfileDir.mkdirs();
        }

        File userAvatarFile = new File(userProfileDir, UID + ".jpg");

        // 检查是否已经选择了图片
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            File selectedFile = new File(selectedImagePath);
            try (FileInputStream inputStream = new FileInputStream(selectedFile);
                 FileOutputStream outputStream = new FileOutputStream(userAvatarFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("User avatar copied and renamed to: " + userAvatarFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error copying user avatar: " + e.getMessage());
            }
        } else {
            // 如果没有选择图片，使用默认图片
            File defaultAvatar = new File(USER_PROFILE_DIR, "default.jpg");
            if (!defaultAvatar.exists()) {
                System.err.println("Default avatar 'default.jpg' does not exist in the directory: " + USER_PROFILE_DIR);
                return;
            }

            try (FileInputStream inputStream = new FileInputStream(defaultAvatar);
                 FileOutputStream outputStream = new FileOutputStream(userAvatarFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("Default avatar copied and renamed to: " + userAvatarFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error copying default avatar: " + e.getMessage());
            }
        }
    }
    private class UploadButtonListener implements ActionListener {
        private JFrame parentFrame;

        public UploadButtonListener(JFrame frame) {
            this.parentFrame = frame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();//选择文件
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(parentFrame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (isImageFile(selectedFile)) {
                    selectedImagePath = selectedFile.getAbsolutePath(); // 保存所选图片路径
                    //System.out.println(selectedFile);
                    displayProfilePicture(selectedFile,true); // 直接显示所选图片
                    JOptionPane.showMessageDialog(parentFrame, "Image selected: " + selectedFile.getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "请选择.jpg格式的图片", "图片类型不支持", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    private boolean isImageFile(File file) {
        String[] imageExtensions = {"jpg", "jpeg"};
        for (String extension : imageExtensions) {
            if (file.getName().toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

}

