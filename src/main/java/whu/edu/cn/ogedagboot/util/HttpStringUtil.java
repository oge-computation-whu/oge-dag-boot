package whu.edu.cn.ogedagboot.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class HttpStringUtil {

    public String responseRender(HttpStatusUtil httpStatusUtil, String msg) {
        StringBuilder response = new StringBuilder("{\"code\":");
        response.append(httpStatusUtil.getCode())
                .append(",\"msg\":\"")
                .append(msg)
                .append("\"}");
        return response.toString();
    }

    public String responseRender(HttpStatusUtil httpStatusUtil, String msg, Object data) {
        StringBuilder response = new StringBuilder("{\"code\":");
        response.append(httpStatusUtil.getCode())
                .append(",\"msg\":\"")
                .append(msg)
                .append("\",\"data\":")
                .append(JSON.toJSONString(data))
                .append("}");
        return response.toString();
    }

    public void responseRender(ServletResponse servletResponse, HttpStatusUtil status, String msg) {
        try {
            servletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
            servletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            servletResponse.getWriter().print(responseRender(status, msg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void responseRender(ServletResponse servletResponse, String jsonString) {
        JSONObject jsonObject = JSON.parseObject(jsonString);
        responseRender(servletResponse,
                HttpStatusUtil.valueOf(jsonObject.getString("code")),
                jsonObject.getString("msg"));
    }

    public String ok(String msg) {
        return responseRender(HttpStatusUtil.OK, msg);
    }

    public String ok(String msg, String data) {
        return responseRender(HttpStatusUtil.OK, msg, data);
    }

    public String ok(String msg, Object data) {
        return responseRender(HttpStatusUtil.OK, msg, data);
    }

    public String failure(String msg) {
        return responseRender(HttpStatusUtil.INTERNAL_SERVER_ERROR, msg);
    }
}
