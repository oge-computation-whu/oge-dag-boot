package whu.edu.cn.ogedagboot.util;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Properties;

@Component
public class SSHClientUtil {
    private static final Logger log = LoggerFactory.getLogger(SSHClientUtil.class);
    private static Session session = null;
    private static final int TIMEOUT = 60000;

    /**
     * 连接远程服务器
     *
     * @param host     ip地址
     * @param userName 登录名
     * @param password 密码
     * @param port     端口
     * @throws Exception
     */
    public static void versouSshUtil(String host, String userName, String password, int port) throws Exception {
        log.info("尝试连接到....host:" + host + ",username:" + userName + ",password:" + password + ",port:"
                + port);
        JSch jsch = new JSch();
        session = jsch.getSession(userName, host, port);
        session.setPassword(password);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setTimeout(TIMEOUT); // 设置timeout时间
        session.connect(); // 通过Session建立链接
    }

    /**
     * 在远程服务器上执行命令
     *
     * @param cmd     要执行的命令字符串
     * @param charset 编码
     * @throws Exception
     */
    public static void runCmd(String cmd, String charset) throws Exception {
        ChannelShell channelShell = (ChannelShell) session.openChannel("shell");
        channelShell.connect();
        InputStream in = channelShell.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName(charset)));
        PrintWriter writer = new PrintWriter(channelShell.getOutputStream());

        // 发送要执行的命令
        writer.println(cmd);
        writer.flush();

        String buf;
        while ((buf = reader.readLine()) != null) {
            System.out.println(buf);
            if (buf.contains("Connection to ogecal0 closed.")) {
                // 当读取到命令结束标记时退出循环
                break;
            }
        }
        reader.close();
        writer.close();
        channelShell.disconnect();
        session.disconnect();
    }
}