package whu.edu.cn.ogedagboot.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * java发送http的get和post请求
 */
public class HttpRequestUtil {
    /**
     * 向指定URL发送GET方式的请求
     *
     * @param url 发送请求的URL
     * @return URL 代表远程资源的响应
     */
    public static String sendGet(String url) {
        String result = "";
        String urlName = url;
        try {
            URL realUrl = new URL(urlName);
            //打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            //设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //建立实际的连接
            conn.connect();
            //获取所有的响应头字段
            Map<String, List<String>> map = conn.getHeaderFields();
            //遍历所有的响应头字段
            //for (String key : map.keySet()) {
            //    System.out.println(key + "-->" + map.get(key));
            //}
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常" + e);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 向指定URL发送POST方式的请求
     *
     * @param url   发送请求的URL
     * @param param 请求参数
     * @return URL 代表远程资源的响应
     */
    public static String sendPost(String url, String param) {
        String result = "";
        try {
            URL realUrl = new URL(url);
            //打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            //设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            //发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //获取URLConnection对象对应的输出流
            PrintWriter out = new PrintWriter(conn.getOutputStream());
            //发送请求参数
            out.print(param);
            //flush输出流的缓冲
            out.flush();
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += "\n" + line;
            }
        } catch (Exception e) {
            System.out.println("发送POST请求出现异常" + e);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 向指定URL发送DELETE方式的请求
     *
     * @param url 发送请求的URL
     * @return URL 代表远程资源的响应
     */
    public static String sendDelete(String url) {
        String result = "";
        String urlName = url;
        try {
            URL realUrl = new URL(urlName);
            //打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            //设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestMethod("DELETE");
            conn.setDoOutput(true);
            //建立实际的连接
            conn.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送DELETE请求出现异常" + e);
            e.printStackTrace();
        }
        return result;
    }
}
