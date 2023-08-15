package whu.edu.cn.ogedagboot.service;

import com.alibaba.fastjson.JSONObject;
import whu.edu.cn.ogedagboot.bean.Task;

import java.util.List;

public interface TaskManagementService {

    /**
     * 根据user_name获取user_id
     */
    String getUserIdByUserName(String userName);

    /**
     * 根据前端DAG_id获取task全部信息
     */
    Task getTaskInfoByDagId(String DagId);

    /**
     * 插入一条任务记录
     */
    boolean addTaskRecord(Task task);

    /**
     * 删除一条任务记录
     */
    boolean deteleTaskRecord(String DagId);

    /**
     * 更新任务状态
     */
    boolean updateTaskRecordOfstate(String state);
}
