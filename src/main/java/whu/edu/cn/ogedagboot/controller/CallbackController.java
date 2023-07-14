package whu.edu.cn.ogedagboot.controller;


import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 交给 oge_computation_ogc 使用的回调服务
 */
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class CallbackController {

    // 不同用户（或不同原始请求）下 TMS 服务输出的json
    public static final
    ConcurrentHashMap<String, String> outJsonsOfTMS = new ConcurrentHashMap<>();

    public static final
    ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> noticeJsonMap = new ConcurrentHashMap<>();


    /**
     * 实现 oge_computation_ogc 将 url 传递给 springboot 项目
     *
     * @param outJson TMS 服务输出的json，主要内容是前端获取结果的url（下载链接）
     */
    @PostMapping("/deliverUrl")
    public void deliverUrl(@RequestBody String outJson) {
        outJsonsOfTMS.put(
                JSONObject.parseObject(outJson).getString("workID"),
                JSONObject.parseObject(outJson).getJSONObject("json").toJSONString()
        );
        System.out.println("outJsonsOfTMS = " + outJsonsOfTMS);
        System.out.println("url = " + outJson);
    }


    @PostMapping("/deliverNotice")
    public void deliverNotice(@RequestBody String noticeJson) {

        String workID = JSONObject
                .parseObject(noticeJson)
                .getString("workID");
        String notice = JSONObject
                .parseObject(noticeJson)
                .getJSONObject("notice").toJSONString();
        // springboot controller 默认为单例
        synchronized (this) {
            if (!noticeJsonMap.containsKey(workID)) {
                ConcurrentLinkedQueue<String> noticeQueue = new ConcurrentLinkedQueue<>();
                noticeQueue.add(notice);
                noticeJsonMap.put(workID, noticeQueue);
            } else {
                noticeJsonMap.get(workID).add(notice);
            }
        }

        return;


    }


}
