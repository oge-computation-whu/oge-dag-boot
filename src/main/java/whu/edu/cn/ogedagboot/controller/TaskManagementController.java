package whu.edu.cn.ogedagboot.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import whu.edu.cn.ogedagboot.bean.Task;
import whu.edu.cn.ogedagboot.bean.TaskRequest;
import whu.edu.cn.ogedagboot.service.TaskManagementService;
import whu.edu.cn.ogedagboot.util.HttpStringUtil;
import whu.edu.cn.ogedagboot.util.LivyUtil;
import whu.edu.cn.ogedagboot.util.RedisUtil;

import java.sql.Timestamp;


/**
 * 任务管理
 */
@RestController
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@Api(tags = "任务管理接口")
public class TaskManagementController {

    @Autowired
    private TaskManagementService taskManagementService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private HttpStringUtil httpStringUtil;

    /**
     * 运行并新增任务记录
     *
     * @param
     */
    @PostMapping(value = "/addTaskRecord")
    @ApiOperation("运行并新增任务记录")
    public String addTaskRecord(@RequestBody TaskRequest taskRequest) {
        String DagId = taskRequest.getId();
        String task_name = taskRequest.getTaskName();
        String crs = taskRequest.getCrs();
        String scale = taskRequest.getScale();
        String userName = taskRequest.getUserName();
        String folder = taskRequest.getFolder();
        String filename = taskRequest.getFilename();
        String format = taskRequest.getFormat();

        String userId = taskManagementService.getUserIdByUserName(userName);

//        定义并获取task的属性值
        Task task = new Task();
        task.setDagId(DagId);
        task.setTaskName(task_name);
        task.setCrs(crs);
        task.setScale(String.valueOf(scale));
        task.setUserId(userId);
        task.setUserName(userName);
        task.setFolder(folder);
        task.setFilename(filename);
        task.setFormat(format);

//        根据batchId从redis中获取workTaskJSON
        String workTaskJSON = redisUtil.getValueByKey(DagId);
//        将workTaskJSON和batchId输入,运行batch,获取返回的sessionId和state
        JSONObject workTaskJsonObj = JSON.parseObject(workTaskJSON);
        JSONObject a = JSON.parseObject(workTaskJsonObj.getString("dag"));
        a.put("isBatch", 1);
        String dagStr = a.toString().replace("{", " { ").replace(
                "}", " } ");
//        String dagStr =
//                JSON.parseObject(workTaskJsonObj.getString("dag")).put("isBatch", 1).toString().replace("{", " { ").replace(
//                        "}", " } ");
//        String dagStr = workTaskJsonObj.getString("dag").replace("{", " { ").replace("}", " } ");
        JSONObject result = LivyUtil.runBatch(dagStr, DagId, userName, crs, String.valueOf(scale), folder,
                filename, format);
        int batchSessionId = result.getInteger("batchSessionId");
        task.setBatchSessionId(String.valueOf(batchSessionId));
        String state = LivyUtil.getBatchesState(batchSessionId);
        task.setState(state);
        System.out.println("新增一条任务记录：" + task.toString());
        String addTask = taskManagementService.addTaskRecord(task);

        //开始轮询任务，获取状态并输入数据库
        try {
            taskManagementService.pollTask(batchSessionId, DagId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return addTask;
    }

    /**
     * 删除任务记录
     *
     * @param
     */
    @DeleteMapping(value = "/deleteTaskRecord")
    @ApiOperation("删除任务记录")
    @ApiImplicitParam(name = "id", value = "DAG的编号", required = true)
    public boolean deleteTaskRecord(@RequestParam("id") String DagId) {
        boolean flag = taskManagementService.deteleTaskRecord(DagId);
        if (flag) {
            System.out.println("删除一条任务记录：" + DagId);
            return flag;
        } else {
            return flag;
        }
    }


    /**
     * 更新任务状态
     */
    @PutMapping(value = "/updateSate")
    @ApiOperation("更新任务状态")
    @ApiImplicitParam(name = "id", value = "DAG的编号", required = true)
    public String updateTaskRecordOfstate(@RequestParam("id") String DagId) {
        Task task = taskManagementService.getTaskInfoByDagId(DagId);
        try {
            System.out.println("更新一条任务记录：" + task.getId());
            return task.getState();

        } catch (Exception e) {
            log.error("Error");
            return httpStringUtil.failure("更新任务状态失败");
        }


    }

    /**
     * 根据任务状态，返回对应任务列表以及详细信息
     */
    @GetMapping(value = "/TaskRecordByState")
    @ApiOperation("根据任务状态，返回对应任务列表以及详细信息")
    @ApiImplicitParam(name = "state", value = "任务状态", required = true)
    public String getTaskRecordByState(@RequestParam("state") String state) {
        return taskManagementService.getTaskRecordByState(state);
    }


    /**
     * 根据任务状态和用户名，返回个人对应任务列表以及详细信息
     */
    @GetMapping(value = "/TaskRecordByStateAndUsername")
    @ApiOperation("根据任务状态和用户名，返回个人对应任务列表以及详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "state", value = "任务状态", required = true),
            @ApiImplicitParam(name = "userName", value = "用户名", required = true)
    })

    public String getTaskRecordByStateAndUsername(@RequestParam("state") String state,
                                                  @RequestParam("userName") String userName) {
        return taskManagementService.getTaskRecordByStateAndUsername(state, userName);
    }

    /**
     * 返回管理员任务面板3个参数:任务待办、本周任务平均处理时间、本周完成任务数
     */
    @GetMapping(value = "/TaskPanelOfAdmin")
    @ApiOperation("返回管理员任务面板3个参数:任务待办数量、本周任务平均处理时间、本周完成任务数")
    public String getTaskPanelOfAdmin() {
        return taskManagementService.getTaskPanelOfAdmin();
    }

    /**
     * 返回个人任务面板3个参数:任务待办、本周任务平均处理时间、本周完成任务数
     */
    @GetMapping(value = "/TaskPanelOfUser")
    @ApiOperation("返回用户个人任务面板3个参数:任务待办数量、本周任务平均处理时间、本周完成任务数")
    @ApiImplicitParam(name = "userName", value = "用户名", required = true)
    public String getTaskPanelOfUser(@RequestParam("userName") String userName) {
        return taskManagementService.getTaskPanelOfUser(userName);
    }

}
