package whu.edu.cn.ogedagboot.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class OGEScriptExecuteResponse {

    private JSONObject spaceParams;
    private JSONArray dagList;
    private String log;

    public JSONObject getSpaceParams() {
        return spaceParams;
    }

    public void setSpaceParams(JSONObject spaceParams) {
        this.spaceParams = spaceParams;
    }

    public JSONArray getDagList() {
        return dagList;
    }

    public void setDagList(JSONArray dagList) {
        this.dagList = dagList;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
