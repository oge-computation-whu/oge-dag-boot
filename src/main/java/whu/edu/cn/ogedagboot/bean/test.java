package whu.edu.cn.ogedagboot.bean;

import java.io.*;

public class test {
    public static void main(String[] args) {
        //读取output.txt
        File file = new File("src/main/java/whu/edu/cn/ogedagboot/bean/json.json");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String st;
        while (true) {
            try {
                if (((st = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("st = " + st);
    }
}
