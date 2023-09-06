package whu.edu.cn.ogedagboot.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.val;
import whu.edu.cn.ogedagboot.controller.CallbackController;

import java.util.ArrayList;
import java.util.Objects;

import static whu.edu.cn.ogedagboot.util.GlobalConstantUtil.*;
import static whu.edu.cn.ogedagboot.util.HttpRequestUtil.*;
import static whu.edu.cn.ogedagboot.util.SSHClientUtil.runCmd;
import static whu.edu.cn.ogedagboot.util.SSHClientUtil.versouSshUtil;

public class LivyUtil {
    public static void main(String[] args) {
        initLivy();
        //testLivy();
    }

    public static void testLivy() {
        for (int i = 0; i < LIVY_SESSION_NUM; i++) {
            //提交任务给session
            JSONObject body = new JSONObject();
            String code = "println(sc.range(1L, Int.MaxValue.toLong).reduce(_ + _))";
            body.put("code", code);
            body.put("kind", "spark");
            String parameter = body.toJSONString();
            String outputString = sendPost(LIVY_URL + "/sessions/" + i + "/statements", parameter);
        }
    }

    public static void initLivy() {
        try {
            versouSshUtil(LIVY_HOST, LIVY_USER, LIVY_PWD, 22);
            String st = "ssh ogecal0" + "\n" + "/root/hadoop/bin/hdfs dfs -rm -r /user/root/.sparkStaging" + "\n" + "livy-server stop" + "\n" + "livy-server start" + "\n" + "exit" + "\n";
            runCmd(st, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 等待Livy启动成功
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < LIVY_SESSION_NUM; i++) {
            JSONObject body = new JSONObject();
            body.put("kind", "spark");
            body.put("driverCores", 2);
            body.put("driverMemory", "1500m");
            body.put("executorCores", 2);
            body.put("executorMemory", "3g");
            body.put("numExecutors", 5);
            String[] str = {"local:/mnt/storage/dag-boot/oge-computation_ogc.jar"};
            body.put("jars", str);
            JSONObject bodyChildren = new JSONObject();
            bodyChildren.put("spark.driver.extraClassPath", "local:/root/spark/jars/*");
            bodyChildren.put("spark.executor.extraClassPath", "local:/root/spark/jars/*");
            body.put("conf", bodyChildren);
            String param = body.toJSONString();
            String postSt = sendPost(LIVY_URL + "/sessions", param);
            System.out.println("postSt = " + postSt);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String livyTrigger(String workTaskJSON, String originTaskID, String userId) {
        String baseUrl = LIVY_URL;
        int sessionNumExpected = LIVY_SESSION_NUM;

        // 获取所有的session
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

        // 已经挂掉的session重新启动
        if (sessionNum < sessionNumExpected) {
            for (int i = 0; i < sessionNumExpected - sessionNum; i++) {
                JSONObject body = new JSONObject();
                body.put("kind", "spark");
                body.put("driverCores", 2);
                body.put("driverMemory", "2g");
                body.put("executorCores", 8);
                body.put("executorMemory", "8g");
                body.put("numExecutors", 1);
                String[] str = {"local:/mnt/storage/dag-boot/oge-computation_ogc.jar"};
                body.put("jars", str);
                JSONObject bodyChildren = new JSONObject();
                bodyChildren.put("spark.driver.extraClassPath", "local:/root/spark/jars/*");
                bodyChildren.put("spark.executor.extraClassPath", "local:/root/spark/jars/*");
                body.put("conf", bodyChildren);
                String param = body.toJSONString();
                String postSt = sendPost(LIVY_URL + "/sessions", param);
                System.out.println("postSt = " + postSt);
            }
        }

        // 检查session能否使用
        int sessionIdAvailable = -1;
        for (Integer integer : sessionIdList) {
            String sessionInfoString = sendGet(baseUrl + "/sessions/" + integer);
            JSONObject sessionInfoJson = JSON.parseObject(sessionInfoString);
            if (Objects.equals(sessionInfoJson.getString("state"), "idle")) {
                sessionIdAvailable = integer;
                break;
            } else if (Objects.equals(sessionInfoJson.getString("state"), "dead")) {
                sendDelete(baseUrl + "/sessions/" + integer);
            }
        }

        //// 如果没有一个session是闲置的，则取消第一个session正在执行的任务
        //if (sessionIdAvailable == -1) {
        //    String statementsInfoString = sendGet(baseUrl + "/sessions/" + sessionIdList.get(0) + "/statements");
        //    JSONObject statementsInfoJson = JSON.parseObject(statementsInfoString);
        //    int totalStatements = statementsInfoJson.getInteger("total_statements");
        //    String cancelInfoString = sendPost(baseUrl + "/sessions/" + sessionIdList.get(0) + "/statements/" + (totalStatements - 1), "");
        //    JSONObject cancelInfoJson = JSON.parseObject(cancelInfoString);
        //    String cancelBool = cancelInfoJson.getString("msg");
        //    if (Objects.equals(cancelBool, "canceled")) {
        //        System.out.println("Already Cancel Session " + sessionIdList.get(0) + " And Statement " + (totalStatements - 1));
        //    }
        //    sessionIdAvailable = sessionIdList.get(0);
        //}

        // 如果没有一个session是闲置的，则提示在排队中
        JSONObject resultJsonObject = new JSONObject();
        if (sessionIdAvailable == -1) {
            resultJsonObject.put("status", "waiting");
            return resultJsonObject.toJSONString();
        } else {

            workTaskJSON = BuildStrUtil.convertStr(workTaskJSON);
            // 提交任务给session
            JSONObject body = new JSONObject();
            String code = "whu.edu.cn.trigger.Trigger.runMain(sc," +
                    "\"" + workTaskJSON /* modis.json */ + "\"," +
                    "\"" + originTaskID /* 第一次点run标识的业务 */ + "\"," +
                    "\"" + userId /* 第一次点run标识的业务 */ + "\"" +
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
                    resultJsonObject.put("status", "failure");
                    return resultJsonObject.toJSONString();
                }
            }

            if (!(CallbackController.outJsonsOfTMS.containsKey(originTaskID))) {
                throw new RuntimeException("获取 outJson 失败！！");
            }
            // 返回 outJson,也就是当前工作ID对应的ogc计算结果，原out.txt
            resultJsonObject = JSONObject.parseObject(CallbackController.outJsonsOfTMS.remove(originTaskID));
            if (!resultJsonObject.containsKey("error")) {
                resultJsonObject.put("status", "success");
            } else {
                resultJsonObject.put("status", "error");
            }
            return resultJsonObject.toJSONString();
        }
    }

    public static JSONObject runBatch(String workTaskJSON, String originTaskID, String userName, String crs,
                                      String scale, String folder, String filename, String format) {
        // 获取batches所需的body信息
        JSONObject body = new JSONObject();
        JSONArray args = new JSONArray();
        args.add(workTaskJSON);
        args.add(originTaskID);
        args.add(userName);
        args.add(crs);
        args.add(scale);
        args.add(folder);
        args.add(filename);
        args.add(format);

        body.put("args", args);
        body.put("file", "local:/mnt/storage/dag-boot/oge-computation_ogc.jar");
        body.put("className", "whu.edu.cn.trigger.TriggerBatch");
        body.put("driverCores", 2);
        body.put("driverMemory", "2g");
        body.put("executorCores", 8);
        body.put("executorMemory", "8g");
        body.put("numExecutors", 3);

        JSONObject bodyChildren = new JSONObject();
        bodyChildren.put("spark.driver.extraClassPath", "local:/root/spark/jars/*");
        bodyChildren.put("spark.executor.extraClassPath", "local:/root/spark/jars/*");
        body.put("conf", bodyChildren);

        //发送post /batches请求
        String param = body.toJSONString();
        String postSt = sendPost(LIVY_URL + "/batches", param);
        System.out.println("postSt = " + postSt);

        //将返回的batch转成JSONObject
        JSONObject BatchJSONObject = JSON.parseObject(postSt);
        int batchSessionId = BatchJSONObject.getInteger("id");
        String state = BatchJSONObject.getString("state");

        JSONObject result = new JSONObject();
        result.put("batchSessionId", batchSessionId);
        result.put("state", state);
        return result;
    }

    public static String getBatchesState(int batchSessionId) {
        String baseUrl = LIVY_URL;
        String stateStr = sendGet(baseUrl + "/batches/" + batchSessionId + "/state");
        JSONObject stateJSONObject = JSON.parseObject(stateStr);
        String state = stateJSONObject.getString("state");

        return state;
    }
}
