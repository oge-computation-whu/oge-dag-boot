package whu.edu.cn.ogedagboot.bean;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/webSocket")
@Component
public class WebSocket {

    private Session session;

    private static final CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<>();

    @OnOpen
    public void opOpen(Session session){
        this.session = session;
        webSocketSet.add(this);
    }

    @OnClose
    public void onClose(Session session){
        this.session = session;
        webSocketSet.remove(this);
    }

    //@OnMessage
    //public void onMessage(String message){
    //    log.info("【websocket消息】 收到客户端发来的消息：{}",message);
    //}

    // TODO 将客户端需要发送的 message 都交给 websocket 实例
    public void sendMessage(String message) throws InterruptedException {
        System.out.println(message);
        for (WebSocket webSocket: webSocketSet){
            try {
                webSocket.session.getBasicRemote().sendText(message);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    // TODO 与上面类似
    public void sendStatusOfSaveDag(String message){
        for (WebSocket webSocket: webSocketSet){
            try {
                webSocket.session.getBasicRemote().sendText(message);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
