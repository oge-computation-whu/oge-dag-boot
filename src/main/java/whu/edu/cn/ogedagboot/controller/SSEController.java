package whu.edu.cn.ogedagboot.controller;


import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Random;

/**
 * 为前端提供 SSE (Server-Sent Events) 服务
 */
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class SSEController {
    private static final Logger log = LoggerFactory.getLogger(SSEController.class);


    @GetMapping(value = "/runProcessNotice", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter runProcessNotice(@RequestParam("dagId") String dagId) {
        SseEmitter emitter = new SseEmitter(1000 * 120L); // 超时时间设置为2min


        new Thread(() -> {
            try {

                //TODO: 这里的死循环要修改，初步设想是添加一个结束标识及最大等待时间

                long start = System.currentTimeMillis();

                while (true) {
                    Thread.sleep(3000);

                    long end = System.currentTimeMillis();
                    System.out.println("(end - start) = " + (end - start));

                    if (!CallbackController.noticeJsonMap.containsKey(dagId)) {
                        emitter.send(SseEmitter.event().data("finished"));
                        System.out.println("finished");
                        break;
                    }


                    String message = CallbackController.noticeJsonMap.get(dagId).poll();
                    log.info("message = " + message);
                    if (message != null) {

                        log.info("not null");
                        // 发送消息给客户端
                        emitter.send(SseEmitter.event().data(message));
                    } else {
                        emitter.send(SseEmitter.event().data("还未收到数据"));
                    }

                }
            } catch (Exception e) {
//                log.error(e.getLocalizedMessage());
                // 超时或出错（正常情况下是超时,send 报错）
                // 注：前端如果再次请求该接口，也会导致异常（此时会重新启动服务）
//                log.error("e.getClass() = " + e.getClass());
                if (e instanceof IllegalStateException &&
                        CallbackController.noticeJsonMap.containsKey(dagId)
                ) {
                    // 超时
                    log.warn("长连接超时前未收到计算结束反馈，通知队列将被主动清空！");
                    CallbackController.noticeJsonMap.get(dagId).clear();
                    CallbackController.noticeJsonMap.remove(dagId);
                } else if (e instanceof ClientAbortException) {
                    // 再次被请求
                    log.warn("接口被再次请求，上次建立的连接被终止！");
                }

                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }


}
