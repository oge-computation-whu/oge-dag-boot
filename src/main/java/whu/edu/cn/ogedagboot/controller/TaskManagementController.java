package whu.edu.cn.ogedagboot.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import whu.edu.cn.ogedagboot.bean.Task;
import whu.edu.cn.ogedagboot.service.TaskManagementService;
import whu.edu.cn.ogedagboot.util.LivyUtil;
import whu.edu.cn.ogedagboot.util.RedisUtil;


/**
 * 任务管理
 */
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@Api(tags = "任务管理接口")
public class TaskManagementController {

    @Autowired
    private TaskManagementService taskManagementService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 运行并新增任务记录
     *
     * @param
     */
    @PostMapping(value = "/addTaskRecord")
    @ApiOperation("运行并新增任务记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "DAG的编号", required = true),
            @ApiImplicitParam(name = "taskName", value = "任务名称", required = true),
            @ApiImplicitParam(name = "crs", value = "坐标系", required = true),
            @ApiImplicitParam(name = "scale", value = "尺度", required = true),
            @ApiImplicitParam(name = "userName", value = "用户名称", required = true),
            @ApiImplicitParam(name = "description", value = "任务描述", required = true),
            @ApiImplicitParam(name = "folder", value = "输出资源文件夹路径", required = true),
            @ApiImplicitParam(name = "filename", value = "输出文件名", required = true),
            @ApiImplicitParam(name = "format", value = "输出文件格式", required = true)
    })
    public Task addTaskRecord(@RequestParam("id") String DagId,
                              @RequestParam("taskName") String task_name,
                              @RequestParam("crs") String crs,
                              @RequestParam("scale") Double scale,
                              @RequestParam("userName") String userName,
                              @RequestParam("description") String description,
                              @RequestParam("folder") String folder,
                              @RequestParam("filename") String filename,
                              @RequestParam("format") String format) {


        String userId = taskManagementService.getUserIdByUserName(userName);

//        定义并获取task的属性值
        Task task = new Task();
        task.setDagId(DagId);
        task.setTaskName(task_name);
        task.setCrs(crs);
        task.setScale(String.valueOf(scale));
        task.setUserId(userId);
        task.setUserName(userName);
        task.setDescription(description);
        task.setFolder(folder);
        task.setFilename(filename);
        task.setFormat(format);

//        根据batchId从redis中获取workTaskJSON
        String workTaskJSON = redisUtil.getValueByKey(DagId);
//        将workTaskJSON和batchId输入,运行batch,获取返回的sessionId和state
        JSONObject workTaskJsonObj = JSON.parseObject(workTaskJSON);
        String dagStr = workTaskJsonObj.getString("dag").replace("{", " { ").replace("}", " } ");
        JSONObject result = LivyUtil.runBatch(dagStr, DagId, userName, crs, String.valueOf(scale), folder,
                filename, format);
        int batchSessionId = result.getInteger("batchSessionId");
        task.setBatchSessionId(String.valueOf(batchSessionId));
        String state = LivyUtil.getBatchesState(batchSessionId);
        task.setState(state);

        boolean flag = taskManagementService.addTaskRecord(task);
        if (flag) {
            System.out.println("新增一条任务记录：" + task.toString());
            return task;
        } else {
            return null;
        }
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
    public Task updateTaskRecordOfstate(@RequestParam("id") String DagId) {
        Task task = taskManagementService.getTaskInfoByDagId(DagId);
        String batchSessionId = task.getBatchSessionId();
        String state = LivyUtil.getBatchesState(Integer.parseInt(batchSessionId));

        boolean flag = taskManagementService.updateTaskRecordOfstate(state);
        if (flag) {
            System.out.println("更新一条任务记录：" + task.getId());
            return task;
        } else {
            return null;
        }
    }


}
