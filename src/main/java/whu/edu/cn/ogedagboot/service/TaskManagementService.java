package whu.edu.cn.ogedagboot.service;

import whu.edu.cn.ogedagboot.bean.Task;

import java.sql.Timestamp;

public interface TaskManagementService {

    /**
     * 根据user_name获取user_id
     */
    String getUserIdByUserName(String userName);

    /**
     * 根据前端DagId获取task全部信息
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
     * 根据dagId更新任务状态
     */
    String updateStateByDagId(String state, String DagId);

    /**
     * 根据batchSessionId更新任务状态
     */
    String updateStateByBatchSessionId(String state, String batchSessionId);


    /**
     * 根据任务状态，返回对应任务列表以及详细信息
     */
    String getTaskRecordByState(String state);

    /**
     * 根据任务状态和用户名，返回个人对应任务列表以及详细信息
     */
    String getTaskRecordByStateAndUsername(String state, String userName);

    /**
     * 记录任务运行时间
     */
    boolean updateTaskRecordOfEndTimeAndRunTime(Timestamp endTime, Double runTime, String dagId);

    /**
     * 返回管理员任务面板3个参数:任务待办、本周任务平均处理时间、本周完成任务数
     */
    String getTaskPanelOfAdmin();

    /**
     * 返回人员任务面板3个参数:任务待办、本周任务平均处理时间、本周完成任务数
     */
    String getTaskPanelOfUser(String userName);

    /**
     * 轮询任务状态
     */
    String pollTask(int batchSessionId, String DagId) throws InterruptedException;
}
