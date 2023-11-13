package whu.edu.cn.ogedagboot.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import whu.edu.cn.ogedagboot.RequestBody.OGEModelExecuteBody;
import whu.edu.cn.ogedagboot.RequestBody.OGEScriptExecuteBody;
import whu.edu.cn.ogedagboot.ResponseBody.OGEScriptExecuteResponse;
import whu.edu.cn.ogedagboot.bean.WebSocket;
import whu.edu.cn.ogedagboot.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static whu.edu.cn.ogedagboot.util.LivyUtil.livyTrigger;
import static whu.edu.cn.ogedagboot.util.SSHClientUtil.runCmd;
import static whu.edu.cn.ogedagboot.util.SSHClientUtil.versouSshUtil;
import static whu.edu.cn.ogedagboot.util.SparkLauncherUtil.sparkSubmitTriggerBatch;


@Slf4j
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)

public class JsonReceiverController {


    @Resource
    private WebSocket webSocket;

    @Resource
    private JedisPool jedisPool;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ShUtil shUtil;

    @PostMapping("/saveDagJson")
    public void saveDagJson(@RequestBody String params, HttpSession httpSession) {

        JSONObject paramsObject = JSONObject.parseObject(params);
        // 取出dag的
        String ogeDagJson = paramsObject.getString("dag");
        // 写入session
        httpSession.setAttribute("OGE_DAG_JSON", ogeDagJson);
        // 看下是不是
        System.out.println(ogeDagJson);

        // 取出各个 Render 参数 , 并存入 session
        JSONObject renderParamsObject = JSONObject.parseObject(ogeDagJson).getJSONObject("0").getJSONObject("functionInvocationValue").getJSONObject("arguments");
        // 系统色带类型
        String systemColorRamp = renderParamsObject.getJSONObject("palette").getString("constantValue");
        httpSession.setAttribute("systemColorRamp", systemColorRamp);
        // 用户输入的渲染灰度最大值
        int grayScaleMax = renderParamsObject.getJSONObject("max").getIntValue("constantValue");
        httpSession.setAttribute("grayScaleMax", grayScaleMax);
        // 用户输入的渲染灰度最小值
        int grayScaleMin = renderParamsObject.getJSONObject("min").getIntValue("constantValue");
        httpSession.setAttribute("grayScaleMin", grayScaleMin);

        // 生成原始业务ID，就是用户点击run之后的整个业务
        long timeMillis = System.currentTimeMillis();
        String originTaskID = SystemConstants.ORIGIN_TASK_PREFIX + timeMillis;
        httpSession.setAttribute("ORIGIN_TASK_ID", originTaskID);

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
                             @RequestParam("userId") String userId,
                             HttpSession httpSession) {//TODO

        String ogeDagJsonStr = (String) httpSession.getAttribute("OGE_DAG_JSON");

        // 如果没有获取到数据
        if (ogeDagJsonStr.isEmpty()) {
            return "Error";
        }

        JSONObject ogeDagJson = JSONObject.parseObject(ogeDagJsonStr);
        String originTaskId = (String) httpSession.getAttribute("ORIGIN_TASK_ID");

        return livyTrigger(BuildStrUtil.buildChildTaskJSON(level, spatialRange, ogeDagJson), originTaskId, userId);

    }

    @PostMapping("/runDagJsonBatch")
    public String runDagJsonBatch() {
        Jedis jedis = jedisPool.getResource();
        if (jedis.exists("ogeDag")) {
            String ogeDagStr = jedis.get("ogeDag");
            JSONObject ogeDagJson = JSONObject.parseObject(ogeDagStr);
            ogeDagJson.put("isBatch", "1");
            String paramStr = ogeDagJson.toJSONString();
            jedis.close();
            return sparkSubmitTriggerBatch(paramStr);
        } else {
            jedis.close();
            return "Error";
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

    /**
     * execute the code of oge script and return the dag result
     *
     * @param ogeScriptExecuteBody the request body
     * @return jsonObject { spaceParams: {}, dags:{"layerName" : "dagId"} }
     */
    @PostMapping("/executeCode")
    public JSONObject executeOGEScript(@RequestBody OGEScriptExecuteBody ogeScriptExecuteBody) {
        String code = ogeScriptExecuteBody.getCode();
        String userId = ogeScriptExecuteBody.getUserId();
        // 时间戳
        long timeMillis = System.currentTimeMillis();
        OGEScriptExecuteResponse ogeScriptExecuteResponse = shUtil.executeOGEScript(code);
        JSONArray dagArray = ogeScriptExecuteResponse.getDagList();
        JSONObject spaceParamsObj = ogeScriptExecuteResponse.getSpaceParams();
        JSONObject resultObj = new JSONObject();
        JSONObject dagsObj = new JSONObject();
        for (int i = 0; i < dagArray.size(); i++) {
            String dagId = userId + "_" + timeMillis + "_" + i;
            JSONObject dagObj = dagArray.getJSONObject(i);
            if (dagObj.containsKey("isBatch") && dagObj.getInteger("isBatch") == 1) {
                JSONObject taskObj = new JSONObject();
                taskObj.put("isBatch", true);
                LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timeMillis / 1000, 0, ZoneOffset.UTC);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = dateTime.format(formatter);
                taskObj.put("creatTime", formattedDateTime);
                if (dagObj.containsKey("exportCoverage")) {
                    taskObj.put("exportCoverage", dagObj.getJSONObject("exportCoverage"));
                } else {
                    taskObj.put("exportCoverage", null);
                }
                dagsObj.put(dagId, taskObj);
            } else {
                dagsObj.put(dagArray.getJSONObject(i).getString("layerName"), dagId);
            }
            redisUtil.saveKeyValue(dagId, dagArray.getJSONObject(i).toJSONString(), 3600 * 2);
        }
        resultObj.put("spaceParams", spaceParamsObj);
        resultObj.put("dags", dagsObj);
        resultObj.put("log", ogeScriptExecuteResponse.getLog());
        return resultObj;
    }

    /**
     * recevie the dag from modelbuilder and return the save result
     *
     * @param ogeModelExecuteBody the request body
     * @return the save result
     */
    @PostMapping("/executeModel")
    public JSONObject executeOGEModel(@RequestBody OGEModelExecuteBody ogeModelExecuteBody) {
        String modelString = ogeModelExecuteBody.getModelString();
        String userId = ogeModelExecuteBody.getUserId();
        // 时间戳
        long timeMillis = System.currentTimeMillis();
        JSONArray dagArray = JSONArray.parseArray(modelString);
        JSONObject resultObj = new JSONObject();
        JSONObject dagsObj = new JSONObject();
        for (int i = 0; i < dagArray.size(); i++) {
            String dagId = userId + "_" + timeMillis + "_" + i;
            dagsObj.put(dagArray.getJSONObject(i).getString("layerName"), dagId);
            redisUtil.saveKeyValue(dagId, dagArray.getJSONObject(i).toJSONString(), 60 * 5);
        }
        resultObj.put("spaceParams", ogeModelExecuteBody.getSpaceParams());
        resultObj.put("dags", dagsObj);
        resultObj.put("log", "");
        return resultObj;
    }

    /**
     * receive the dag and spatial geom and execute the dag
     *
     * @param level:int           map level
     * @param spatialRange:String spatial range
     * @param dagId:String        the Id of dag
     * @return String the url of tms
     */
    @PostMapping("/executeDag")
    public String executeDag(@RequestParam("level") int level,
                             @RequestParam("spatialRange") String spatialRange, @RequestParam("dagId") String dagId,
                             @RequestParam("userId") String userId) {
        String dagWithNameStr = redisUtil.getValueByKey(dagId);
        log.info("dag：" + dagWithNameStr);
        if (dagWithNameStr == null) {
            log.warn("未找到" + dagId + "对应的dag");
            return null;
        }
        JSONObject dagWithNameObj = JSONObject.parseObject(dagWithNameStr);
        JSONObject dagObj = JSONObject.parseObject(dagWithNameObj.getString("dag"));
        if (dagWithNameObj.containsKey("layerName") && dagWithNameObj.getString("layerName") != null) {
            dagObj.put("layerName", dagWithNameObj.getString("layerName"));
        }
        log.info(dagObj.toJSONString());
        return livyTrigger(BuildStrUtil.buildChildTaskJSON(level, spatialRange, dagObj), dagId, userId);
    }
}
