package whu.edu.cn.ogedagboot.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import scala.annotation.meta.param;
import whu.edu.cn.ogedagboot.controller.CallbackController;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

import static whu.edu.cn.ogedagboot.util.HttpRequestUtil.*;
import static whu.edu.cn.ogedagboot.util.SSHClientUtil.runCmd;
import static whu.edu.cn.ogedagboot.util.SSHClientUtil.versouSshUtil;

public class LivyUtil {
    public static void main(String[] args) {
        String host = "125.220.153.26";
        String port = "19101";
        String baseUrl = "http://" + host + ":" + port;
        System.out.println(sendDelete(baseUrl + "/sessions/" + 3));
    }

    public static boolean isPortInUse(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            //如果端口没有被占用，则Socket连接会被立即关闭，返回true
            return true;
        } catch (Exception e) {
            //如果端口被占用，则Socket连接会抛出异常，返回false
            return false;
        }
    }

    public static void initLivy() {
        String host = "125.220.153.26";
        String port = "19101";
        String baseUrl = "http://" + host + ":" + port;
        int sessionNum = 5;
        try {
            versouSshUtil(host, "geocube", "ypfamily608", 22);
            String st =
                    "cd /home/geocube/livy/bin/" + "\n" + "./livy-server stop" + "\n" + "./livy-server start" + "\n";
            System.out.println("st = " + st);
            runCmd(st, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            if (isPortInUse(host, Integer.parseInt(port))) {
                break;
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < sessionNum; i++) {
            JSONObject body = new JSONObject();
            body.put("name", "spark" + i);
            body.put("kind", "spark");
            body.put("executorCores", 8);
            body.put("executorMemory", "3g");
            String[] str = {"local:/home/geocube/oge/oge-server/dag-boot/dependency/oge-computation_ogc.jar"};
            body.put("jars", str);
            JSONObject bodyChildren = new JSONObject();
            bodyChildren.put("spark.driver.extraClassPath", "local:/home/geocube/spark/jars/*");
            bodyChildren.put("spark.executor.extraClassPath", "local:/home/geocube/spark/jars/*");
            body.put("conf", bodyChildren);
            String param = body.toJSONString();
            sendPost(baseUrl + "/sessions", param);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String livyTrigger(String workTaskJSON, String originTaskID) {
        String host = "125.220.153.26";
        String port = "19101";
        String baseUrl = "http://" + host + ":" + port;
        int sessionNumExpected = 5;
        try {
            versouSshUtil(host, "geocube", "ypfamily608", 22);
            String st =
                    "cd /home/geocube/oge" + "\n" + "rm -rf on-the-fly" + "\n" + "mkdir on-the-fly" + "\n" +
                            "cd /home/geocube/oge/oge-server/dag-boot" + "\n" + "rm -rf webapi" + "\n" + "mkdir webapi" + "\n" +
                            "cd /home/geocube/tomcat8/apache-tomcat-8.5.57/webapps/" + "\n" + "rm -rf webapi" + "\n" + "mkdir webapi" + "\n";
            System.out.println("st = " + st);
            runCmd(st, "UTF-8");
        } catch (Exception e) {//TODO
            e.printStackTrace();
        }
        long time = System.currentTimeMillis();
        String curWorkID = "" + time;

//        String fileNameJson = "/home/geocube/oge/oge-server/dag-boot/on-the-fly/outputjson_" + time + ".txt";


//        String fileName = "/home/geocube/oge/oge-server/dag-boot/on-the-fly/output_" + time + ".txt";
/*
        File writeFile = new File(fileNameJson);
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(writeFile));
            writer.write(param);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

        //获取所有的session
        String allSessionInfoString = sendGet(baseUrl + "/sessions/");
        JSONObject allSessionInfoObject = JSON.parseObject(allSessionInfoString);
        int sessionNum = Integer.parseInt(allSessionInfoObject.getString("total"));
        JSONArray sessionArray = allSessionInfoObject.getJSONArray("sessions");
        ArrayList<Integer> sessionIdList = new ArrayList<>();
        for (int i = 0; i < sessionArray.size(); i++) {
            JSONObject sessionInfoObject = sessionArray.getJSONObject(i);
            int sessionId = Integer.parseInt(sessionInfoObject.getString("id"));
            sessionIdList.add(sessionId);
        }

        //已经挂掉的session重新启动
        if (sessionNum < sessionNumExpected) {
            for (int i = 0; i < sessionNumExpected - sessionNum; i++) {
                JSONObject body = new JSONObject();
                body.put("name", "spark" + (sessionIdList.get(sessionIdList.size() - 1) + 1));
                body.put("kind", "spark");
                body.put("executorCores", 8);
                body.put("executorMemory", "3g");
                String[] str = {"local:/home/geocube/oge/oge-server/dag-boot/dependency/oge-computation_ogc.jar"};
                body.put("jars", str);
                JSONObject bodyChildren = new JSONObject();
                bodyChildren.put("spark.driver.extraClassPath", "local:/home/geocube/spark/jars/*");
                bodyChildren.put("spark.executor.extraClassPath", "local:/home/geocube/spark/jars/*");
                body.put("conf", bodyChildren);
                String paramRequest = body.toJSONString();
                sendPost(baseUrl + "/sessions", paramRequest);
            }
        }

        //检查session能否使用
        int sessionIdAvailable = -1;
        for (Integer integer : sessionIdList) {
            String sessionInfoString = sendGet(baseUrl + "/sessions/" + integer);
            JSONObject sessionInfoJson = JSON.parseObject(sessionInfoString);
            if (Objects.equals(sessionInfoJson.getString("state"), "idle")) {
                sessionIdAvailable = integer;
                break;
            } else if (Objects.equals(sessionInfoJson.getString("state"), "idle")) {
                sendDelete(baseUrl + "/sessions/" + integer);
            }
        }

        //如果没有一个session是闲置的，则取消第一个session正在执行的任务
        if (sessionIdAvailable == -1) {
            String statementsInfoString = sendGet(baseUrl + "/sessions/" + sessionIdList.get(0) + "/statements");
            JSONObject statementsInfoJson = JSON.parseObject(statementsInfoString);
            int totalStatements = statementsInfoJson.getInteger("total_statements");
            String cancelInfoString = sendPost(baseUrl + "/sessions/" + sessionIdList.get(0) + "/statements/" + (totalStatements - 1), "");
            JSONObject cancelInfoJson = JSON.parseObject(cancelInfoString);
            String cancelBool = cancelInfoJson.getString("msg");
            if (Objects.equals(cancelBool, "canceled")) {
                System.out.println("Already Cancel Session " + sessionIdList.get(0) + " And Statement " + (totalStatements - 1));
            }
            sessionIdAvailable = sessionIdList.get(0);
        }

        workTaskJSON = BuildStrUtil.convertStr(workTaskJSON);
        //提交任务给session
        JSONObject body = new JSONObject();
        String code = "whu.edu.cn.application.oge.Trigger.runMain(sc," +
                "\"" + workTaskJSON /* modis.json */ + "\"," +
                "\"" + curWorkID /* 当前的工作ID */ + "\"," +
                "\"" + originTaskID /* 第一次点run标识的业务 */ + "\"" +
                ")";
        body.put("code", code);
        body.put("kind", "spark");
        String parameter = body.toJSONString();
        String outputString = sendPost(baseUrl + "/sessions/" + sessionIdAvailable + "/statements", parameter);
        System.out.println("outputString = " + outputString);
        JSONObject jsonObject = JSON.parseObject(outputString);
        int statementId = jsonObject.getInteger("id");
        while (true) {
            String statementInfoString = sendGet(baseUrl + "/sessions/" + sessionIdAvailable + "/statements/" + statementId);
            JSONObject statementInfoJson = JSON.parseObject(statementInfoString);
            String state = statementInfoJson.getString("state");
            if (Objects.equals(state, "available")) {
                break;
            }
            if (Objects.equals(state, "error")) {
                throw new RuntimeException("Submit Session " + sessionIdAvailable + " And Statement " + statementId + " Error!!!");
            }
        }


        System.out.println(curWorkID);


        if (!(CallbackController.outJsonsOfTMS.containsKey(curWorkID))) {
            throw new RuntimeException("获取 outJson 失败！！");
        }
        // 返回 outJson,也就是当前工作ID对应的ogc计算结果，原out.txt
        return CallbackController.outJsonsOfTMS.remove(curWorkID);


//        //读取output.txt
//        File file = new File(fileName);
//        BufferedReader br = null;
//        try {
//            br = new BufferedReader(new FileReader(file));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        String stout;
//        while (true) {
//            try {
//                if (((stout = br.readLine()) != null)) {
//                    break;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return stout;
    }
}
