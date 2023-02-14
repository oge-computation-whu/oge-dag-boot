package whu.edu.cn.ogedagboot.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.Objects;

import static whu.edu.cn.ogedagboot.util.HttpRequestUtil.sendGet;
import static whu.edu.cn.ogedagboot.util.HttpRequestUtil.sendPost;
import static whu.edu.cn.ogedagboot.util.SSHClientUtil.runCmd;
import static whu.edu.cn.ogedagboot.util.SSHClientUtil.versouSshUtil;

public class LivyUtil {
    public static void main(String[] args) {

    }
    public static String livyTrigger(String param) {
        String host = "125.220.153.26";
        String port = "19101";
        String baseUrl = "http://" + host + ":" + port;
        try {
            versouSshUtil(host, "geocube", "ypfamily608", 22);
            String st =
                    "cd /home/geocube/oge" + "\n" + "rm -rf on-the-fly" + "\n" + "mkdir on-the-fly" + "\n" +
                    "cd /home/geocube/oge/oge-server/dag-boot" + "\n" + "rm -rf webapi" + "\n" + "mkdir webapi" + "\n" +
                    "cd /home/geocube/tomcat8/apache-tomcat-8.5.57/webapps/" + "\n" + "rm -rf webapi" + "\n" + "mkdir webapi" + "\n";
            System.out.println("st = " + st);
            runCmd(st, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        int sessionNum = 6;
        String jsonString = param;
        Long time = System.currentTimeMillis();
        String fileNameJson = "/home/geocube/oge/oge-server/dag-boot/on-the-fly/outputjson_" + time + ".txt";
        String fileName = "/home/geocube/oge/oge-server/dag-boot/on-the-fly/output_" + time + ".txt";
        File writeFile = new File(fileNameJson);
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(writeFile));
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int sessionId = -1;
        for(int i = 0; i < sessionNum; i++){
            String sessionInfoString = sendGet(baseUrl + "/sessions/" + i);
            JSONObject sessionInfoJson = JSON.parseObject(sessionInfoString);
            if(Objects.equals(sessionInfoJson.getString("state"), "idle")){
                sessionId = i;
                break;
            }
        }
        if(sessionId == -1){
            String statementsInfoString = sendGet(baseUrl + "/sessions/0/statements");
            JSONObject statementsInfoJson = JSON.parseObject(statementsInfoString);
            int totalStatements = statementsInfoJson.getInteger("total_statements");
            String cancelInfoString = sendPost(baseUrl + "/sessions/0/statements/" + (totalStatements - 1),"");
            JSONObject cancelInfoJson = JSON.parseObject(cancelInfoString);
            String cancelBool = cancelInfoJson.getString("msg");
            if(Objects.equals(cancelBool, "canceled")){
                System.out.println("Already Cancel Session " + 0 + " And Statement " + (totalStatements - 1));
            }
            sessionId = 0;
        }

        JSONObject body = new JSONObject();
        String code = "whu.edu.cn.application.oge.Trigger.runMain(sc,\"" + fileNameJson + "\",\"" + fileName + "\")";
        body.put("code", code);
        body.put("kind", "spark");
        String parameter = body.toJSONString();
        String outputString = sendPost(baseUrl + "/sessions/" + sessionId + "/statements", parameter);
        System.out.println("outputString = " + outputString);
        JSONObject jsonObject = JSON.parseObject(outputString);
        int statementId = jsonObject.getInteger("id");
        while(true) {
            String statementInfoString = sendGet(baseUrl + "/sessions/" + sessionId + "/statements/" + statementId);
            JSONObject statementInfoJson = JSON.parseObject(statementInfoString);
            String state = statementInfoJson.getString("state");
            if(Objects.equals(state, "available")){
                break;
            }
            if(Objects.equals(state, "error")){
                throw new RuntimeException("Submit Session "+ sessionId + " And Statement " + statementId + " Error!!!");
            }
        }

        //读取output.txt
        File file = new File(fileName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String stout;
        while (true) {
            try {
                if (((stout = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stout;
    }
}
