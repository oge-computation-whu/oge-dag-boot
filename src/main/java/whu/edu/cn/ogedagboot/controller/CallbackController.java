package whu.edu.cn.ogedagboot.controller;


import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 交给 oge_computation_ogc 使用的回调服务
 */
@RestController
@CrossOrigin(origins = "*",maxAge = 3600)
public class CallbackController {

    // 不同用户（或不同原始请求）下 TMS 服务输出的json
    public static final ConcurrentHashMap<String, String> outJsonsOfTMS = new ConcurrentHashMap<>();


    /**
     *
     * 实现 oge_computation_ogc 将 url 传递给 springboot 项目
     * @param outJson TMS 服务输出的json，主要内容是前端获取结果的url（下载链接）
     * @param workID 工作标识，就是某次原始请求的唯一标识（前端点击run后）
     */
    @PostMapping("/deliverUrl")
    public void deliverUrl(@RequestBody String outJson, @RequestHeader(value = "workID") String workID){
        outJsonsOfTMS.put(workID,outJson);
        System.out.println("outJsonsOfTMS = " + outJsonsOfTMS);
        System.out.println("url = " + outJson);
    }
}
