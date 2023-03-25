package whu.edu.cn.ogedagboot.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

public class BuildStrUtil {
    /**
     * 根据 on-the-fly 来的新请求构建子任务JSON
     * @param level on-the-fly 层级
     * @param spatialRange 前端传来的空间范围
     * @param ogeDagJson 点击run后生成的任务JSON
     * @return 子任务JSON
     */
    public static String buildChildTaskJSON(@RequestParam("level") int level, @RequestParam("spatialRange") String spatialRange, JSONObject ogeDagJson) {
        String[] spatialRangeList = spatialRange.split(",");
        ArrayList<Float> spatialRangeFloat = new ArrayList<>();
        for (String s : spatialRangeList) {
            spatialRangeFloat.add(Float.parseFloat(s));
        }
        JSONObject mapObject = new JSONObject();
        mapObject.put("level", level);
        mapObject.put("spatialRange", spatialRangeFloat);
        ogeDagJson.put("map", mapObject);
        ogeDagJson.put("oorB", "0");
        return ogeDagJson.toJSONString();
    }
}
