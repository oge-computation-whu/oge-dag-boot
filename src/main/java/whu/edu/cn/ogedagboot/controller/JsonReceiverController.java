package whu.edu.cn.ogedagboot.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import whu.edu.cn.ogedagboot.bean.WebSocket;
import whu.edu.cn.ogedagboot.util.BuildStrUtil;
import whu.edu.cn.ogedagboot.util.LivyUtil;
import whu.edu.cn.ogedagboot.util.SystemConstants;

import javax.servlet.http.HttpSession;
import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;

import static whu.edu.cn.ogedagboot.util.LivyUtil.livyTrigger;
import static whu.edu.cn.ogedagboot.util.SSHClientUtil.runCmd;
import static whu.edu.cn.ogedagboot.util.SSHClientUtil.versouSshUtil;
import static whu.edu.cn.ogedagboot.util.SparkLauncherUtil.sparkSubmitTrigger;
import static whu.edu.cn.ogedagboot.util.SparkLauncherUtil.sparkSubmitTriggerBatch;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)

public class JsonReceiverController {


    @Autowired
    private WebSocket webSocket;

    @Autowired
    private JedisPool jedisPool;

    @PostMapping("/saveDagJson")
    public void saveDagJson(@RequestBody String params, HttpSession httpSession) {



        JSONObject paramsObject = JSONObject.parseObject(params);
        // 取出dag的
        String ogeDagJson = paramsObject.getString("dag");

        // 写入session
        httpSession.setAttribute("OGE_DAG_JSON", ogeDagJson);

        // 看下是不是
        System.out.println(ogeDagJson);


        // 生成原始业务ID，就是用户点击run之后的整个业务
        long timeMillis = System.currentTimeMillis();
        String originTaskID = SystemConstants.ORIGIN_TASK_PREFIX + timeMillis;
        httpSession.setAttribute("ORIGIN_TASK_ID",originTaskID);

        String spaceParam = "None";
        if (paramsObject.containsKey("spaceParams")) {
            spaceParam = paramsObject.getJSONObject("spaceParams").toJSONString();
        }
        try {
//            Jedis jedis = jedisPool.getResource();
//            jedis.set("ogeDag", dagString);
//            // 设置为5分钟
//            jedis.expire("ogeDag", 300);
//            System.out.println(jedis.get("ogeDag"));
//            jedis.close();
            webSocket.sendStatusOfSaveDag(spaceParam);
        } catch (Exception e) {
            webSocket.sendStatusOfSaveDag("Fail");
        }
    }

    @PostMapping("/initLivy")
    public void initLivy() {
        LivyUtil.initLivy();
    }

    @PostMapping("/runDagJson")
    public String runDagJson(@RequestParam("level") int level,
                             @RequestParam("spatialRange") String spatialRange,
                             HttpSession httpSession) {//TODO

        String ogeDagJsonStr = (String) httpSession.getAttribute("OGE_DAG_JSON");

        // 如果没有获取到数据
        if (ogeDagJsonStr.isEmpty()) {
            return "Error";
        }


        String flagKey = "isRunDagJsonFinished"; // 标记状态
        String resKey = "resultDagJson"; // 传递结果

        if (httpSession.getAttribute(flagKey) == null){
            httpSession.setAttribute(flagKey,false);
            new Thread(()->{
                JSONObject ogeDagJson = JSONObject.parseObject(ogeDagJsonStr);
                String originTaskId = (String) httpSession.getAttribute("ORIGIN_TASK_ID");
                String res = livyTrigger(
                        BuildStrUtil.buildChildTaskJSON(
                        level, spatialRange, ogeDagJson
                        ),originTaskId);
                httpSession.setAttribute(flagKey,true);
                httpSession.setAttribute(resKey,res);
            }).start();

            return "start";
        }

        if (httpSession.getAttribute(flagKey).equals(true) &&
                httpSession.getAttribute(resKey) != null
        ){
            String resJson = (String) httpSession.getAttribute(resKey);
            httpSession.removeAttribute(flagKey);
            httpSession.removeAttribute(resKey);
            return resJson;
        }




        return "running";







//        Jedis jedis = jedisPool.getResource();
//        if (jedis.exists("ogeDag")) {
//            String ogeDagStr = jedis.get("ogeDag");
//            JSONObject ogeDagJson = JSONObject.parseObject(ogeDagStr);
//            String[] spatialRangeList = spatialRange.split(",");
//            ArrayList<Float> spatialRangeFloat = new ArrayList<>();
//            for (String s : spatialRangeList) {
//                spatialRangeFloat.add(Float.parseFloat(s));
//            }
//            JSONObject mapObject = new JSONObject();
//            mapObject.put("level", level);
//            mapObject.put("spatialRange", spatialRangeFloat);
//            ogeDagJson.put("map", mapObject);
//            ogeDagJson.put("oorB", "0");
//            String paramStr = ogeDagJson.toJSONString();
//            jedis.close();
//            return livyTrigger(paramStr);
//        } else {
//            jedis.close();
//            return "Error";
//        }


    }



    @PostMapping("/runDagJsonBatch")
    public String runDagJsonBatch() {
        Jedis jedis = jedisPool.getResource();
        if (jedis.exists("ogeDag")) {
            String ogeDagStr = jedis.get("ogeDag");
            JSONObject ogeDagJson = JSONObject.parseObject(ogeDagStr);
            ogeDagJson.put("oorB", "1");
            String paramStr = ogeDagJson.toJSONString();
            jedis.close();
            return sparkSubmitTriggerBatch(paramStr);
        } else {
            jedis.close();
            return "Error";
        }
    }

    @PostMapping("/testDagJson")
    public String testDagJson(@RequestParam("level") int level, @RequestParam("spatialRange") String spatialRange) {
        //读取output.txt
        File file = new File("/home/geocube/oge/oge-server/dag-boot/json.json");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String ogeDagStr;
        while (true) {
            try {
                assert br != null;
                if (((ogeDagStr = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject ogeDagJson = JSONObject.parseObject(ogeDagStr);
        String paramStr = BuildStrUtil.buildChildTaskJSON(level, spatialRange, ogeDagJson);
        return sparkSubmitTrigger(paramStr);
    }

    @PostMapping("/postjsonstring")
    public String postJson(@RequestBody String param) {
        String jsonString = param;

        File writeFile = new File("/home/geocube/oge/oge-server/dag-boot/testJson.json");
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(writeFile));
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            versouSshUtil("125.220.153.26", "geocube", "ypfamily608", 22);
            String st = "/home/geocube/spark/bin/spark-submit --master spark://125.220.153.26:7077 --class whu.edu.cn.application.oge.Trigger --driver-memory 30G --executor-memory 10G --conf spark.driver.maxResultSize=4G /home/geocube/oge/oge-server/dag-boot/oge-computation.jar\n";
            runCmd(st, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //读取output.txt
        File file = new File("/home/geocube/oge/oge-server/dag-boot/output.txt");
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
//        try {
//            System.out.println("st = " + st);
//            webSocket.sendMessage(st);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return st;
    }

    @GetMapping("/getjsonstring")
    public void getJson(@RequestBody String param) {
        String jsonString = param;

        File writeFile = new File("/home/geocube/oge/oge-server/dag-boot/testJson.json");
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(writeFile));
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            versouSshUtil("125.220.153.26", "geocube", "ypfamily608", 22);
            String st = "/home/geocube/spark/bin/spark-submit --master spark://125.220.153.26:7077 --class whu.edu.cn.application.oge.Trigger --driver-memory 30G --executor-memory 10G --conf spark.driver.maxResultSize=4G /home/geocube/oge/oge-server/dag-boot/oge-computation.jar\n";
            runCmd(st, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }


        //读取output.txt
        File file = new File("/home/geocube/oge/oge-server/dag-boot/output.txt");
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
        try {
            System.out.println("st = " + st);
            webSocket.sendMessage(st);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/postjsonstringmodelbuilder")
    public void postJsonModelBuilder(@RequestBody String param) {
        String jsonString = param;

        File writeFile = new File("/home/geocube/oge/oge-server/dag-boot/testJson.json");
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(writeFile));
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            versouSshUtil("125.220.153.26", "geocube", "ypfamily608", 22);
            String st = "/home/geocube/spark/bin/spark-submit --master spark://125.220.153.26:7077 --class whu.edu.cn.application.oge.TriggerModelBuilder --driver-memory 30G --executor-memory 10G --conf spark.driver.maxResultSize=4G /home/geocube/oge/oge-server/dag-boot/oge-computation.jar\n";
            runCmd(st, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //读取output.txt
        File file = new File("/home/geocube/oge/oge-server/dag-boot/output.txt");
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
        try {
            System.out.println("st = " + st);
            webSocket.sendMessage(st);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}