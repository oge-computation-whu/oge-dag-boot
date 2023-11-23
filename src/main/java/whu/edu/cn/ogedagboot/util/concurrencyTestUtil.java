package whu.edu.cn.ogedagboot.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static whu.edu.cn.ogedagboot.util.GlobalConstantUtil.LIVY_HOST;
import static whu.edu.cn.ogedagboot.util.GlobalConstantUtil.LIVY_PORT;
import static whu.edu.cn.ogedagboot.util.HttpRequestUtil.sendPost;

public class concurrencyTestUtil {
    public static void main(String[] args) {
        concurrencyLivy();
    }

    public static void concurrencyLivy() {
        String workTaskJSON = "";
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/java/whu/edu/cn/ogedagboot/util/json/NDVI.json"))) {
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }

            // 使用FastJSON解析JSON字符串
            JSONObject jsonObject = JSON.parseObject(jsonString.toString(), Feature.OrderedField);

            // 将JSON对象再次转换为字符串
            workTaskJSON = jsonObject.toJSONString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        String originTaskID = RandomStringUtils.randomAlphanumeric(10);
        for (int i = 0; i < 30; i++) {
            concurrencyLivyTrigger(workTaskJSON, originTaskID, Integer.toString(i));
        }

    }

    public static void concurrencyLivyTrigger(String workTaskJSON, String originTaskID, String sessionId) {
        String baseUrl = "http://" + LIVY_HOST + ":" + LIVY_PORT;

        workTaskJSON = BuildStrUtil.convertStr(workTaskJSON);
        // 提交任务给session
        JSONObject body = new JSONObject();
        String code = "whu.edu.cn.trigger.Trigger.runMain(sc," +
                "\"" + workTaskJSON /* modis.json */ + "\"," +
                "\"" + originTaskID /* 第一次点run标识的业务 */ + "\"" +
                ")";
        body.put("code", code);
        body.put("kind", "spark");
        String parameter = body.toJSONString();
        String outputString = sendPost(baseUrl + "/sessions/" + sessionId + "/statements", parameter);
        System.out.println("outputString = " + outputString);
        JSONObject jsonObject = JSON.parseObject(outputString);
        int statementId = jsonObject.getInteger("id");
    }
}
