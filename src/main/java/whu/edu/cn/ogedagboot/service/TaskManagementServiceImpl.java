package whu.edu.cn.ogedagboot.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cn.ogedagboot.bean.Task;
import whu.edu.cn.ogedagboot.dao.TaskManagementDao;
import whu.edu.cn.ogedagboot.util.HttpStringUtil;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;


@Service
public class TaskManagementServiceImpl implements TaskManagementService {
    @Autowired
    private TaskManagementDao taskManagementDao;
    @Autowired
    private HttpStringUtil httpStringUtil;

    @Override
    public String addTaskRecord(Task task) {
        String taskId = UUID.randomUUID().toString();
        while (taskManagementDao.existsById(taskId) != 0) {
            taskId = UUID.randomUUID().toString();
        }
        task.setId(taskId);
        boolean flag = taskManagementDao.addTaskRecord(task);

        if (flag) {
            return httpStringUtil.ok("运行并添加一条任务记录", task);
        } else {
            return httpStringUtil.failure("添加任务记录失败");
        }
    }

    @Override
    public boolean deteleTaskRecord(String DagId) {
        boolean flag = false;
        try {
            taskManagementDao.deteleTaskRecord(DagId);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public String updateTaskRecordOfstate(String state, String DagId) {

        boolean flag = taskManagementDao.updateTaskRecordOfstate(state, DagId);
        if (flag) {
            return httpStringUtil.ok("成功更新任务状态", state);
        } else {
            return httpStringUtil.failure("更新任务状态失败");
        }
    }


    @Override
    public String getTaskRecordByState(String state) {
        List<Task> tasks = taskManagementDao.getTaskRecordByState(state);
        return httpStringUtil.ok("根据任务状态，返回对应任务列表以及详细信息", tasks);
    }

    @Override
    public String getTaskRecordByStateAndUsername(String state, String userName) {
        List<Task> tasks = taskManagementDao.getTaskRecordByStateAndUsername(state, userName);
        return httpStringUtil.ok("根据任务状态和用户名，返回个人对应任务列表以及详细信息", tasks);
    }

    @Override
    public boolean updateTaskRecordOfRunTime(Double runTime, String dagId) {
        boolean flag = false;
        try {
            taskManagementDao.updateTaskRecordOfRunTime(runTime, dagId);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public String getTaskPanelOfAdmin() {
        JSONObject result = new JSONObject();

        // 获取任务待办的数量
        List<Task> startingTasks = taskManagementDao.getTaskRecordByState("starting");
        int numberOfStartingTasks = startingTasks.size();
        result.put("NumberOfWaitingTask", numberOfStartingTasks);

        // 计算本周任务平均处理时间
        List<Task> allTasks = taskManagementDao.getTaskRecordByState("");
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        // 获取本周的起始日期和结束日期
        LocalDate startOfWeek = currentDate.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = currentDate.with(java.time.DayOfWeek.SUNDAY);

        // 计算非空 runTime 字段在本周的平均值（毫秒）
        long totalRunTimeMillis = 0;
        int numberOfNonEmptyRunTimes = 0;

        for (Task task : allTasks) {
            Double runTime = task.getRunTime();
            Timestamp startTime = task.getStartTime();

            // 如果 runTime 不为空，且 startTime 在本周范围内
            if (runTime != null && isWithinRange(startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), startOfWeek, endOfWeek)) {
                totalRunTimeMillis += runTime;
                numberOfNonEmptyRunTimes++;
            }
        }
        double averageRunTimeSeconds = numberOfNonEmptyRunTimes > 0 ? (double) totalRunTimeMillis / numberOfNonEmptyRunTimes : 0.0;

        result.put("AverageTaskRuningTimeWeekly", averageRunTimeSeconds);

        // 计算本周完成任务数
        List<Task> doneTasks = taskManagementDao.getTaskRecordByState("success");
        int numberOfDoneTasks = 0;
        for (Task task : doneTasks) {
            Timestamp startTime = task.getStartTime();

            // startTime 在本周范围内
            if (isWithinRange(startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), startOfWeek, endOfWeek)) {
                numberOfDoneTasks++;
            }
        }
        result.put("NumberOfDoneTaskWeekly", numberOfDoneTasks);

        return httpStringUtil.ok("返回管理员任务面板3个参数:任务待办、本周任务平均处理时间、本周完成任务数", result);
    }

    @Override
    public String getTaskPanelOfUser(String userName) {
        JSONObject result = new JSONObject();

        // 获取任务待办的数量
        List<Task> startingTasks = taskManagementDao.getTaskRecordByStateAndUsername("starting", userName);
        int numberOfStartingTasks = startingTasks.size();
        result.put("NumberOfWaitingTask", numberOfStartingTasks);

        // 计算本周任务平均处理时间
        List<Task> allTasks = taskManagementDao.getTaskRecordByStateAndUsername("", userName);
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        // 获取本周的起始日期和结束日期
        LocalDate startOfWeek = currentDate.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = currentDate.with(java.time.DayOfWeek.SUNDAY);

        // 计算非空 runTime 字段在本周的平均值（毫秒）
        long totalRunTimeMillis = 0;
        int numberOfNonEmptyRunTimes = 0;

        for (Task task : allTasks) {
            Double runTime = task.getRunTime();
            Timestamp startTime = task.getStartTime();

            // 如果 runTime 不为空，且 startTime 在本周范围内
            if (runTime != null && isWithinRange(startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), startOfWeek, endOfWeek)) {
                totalRunTimeMillis += runTime;
                numberOfNonEmptyRunTimes++;
            }
        }
        double averageRunTimeSeconds = numberOfNonEmptyRunTimes > 0 ? (double) totalRunTimeMillis / numberOfNonEmptyRunTimes : 0.0;

        result.put("AverageTaskRuningTimeWeekly", averageRunTimeSeconds);

        // 计算本周完成任务数
        List<Task> doneTasks = taskManagementDao.getTaskRecordByStateAndUsername("success", userName);
        int numberOfDoneTasks = 0;
        for (Task task : doneTasks) {
            Timestamp startTime = task.getStartTime();

            // startTime 在本周范围内
            if (isWithinRange(startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), startOfWeek, endOfWeek)) {
                numberOfDoneTasks++;
            }
        }
        result.put("NumberOfDoneTaskWeekly", numberOfDoneTasks);

        return httpStringUtil.ok("返回管理员任务面板3个参数:任务待办、本周任务平均处理时间、本周完成任务数", result);
    }


    @Override
    public String getUserIdByUserName(String userName) {
        return taskManagementDao.getUserIdByUserName(userName);
    }

    @Override
    public Task getTaskInfoByDagId(String DagId) {
        return taskManagementDao.getTaskInfoByDagId(DagId);
    }

    // 判断日期是否在某个范围内
    private static boolean isWithinRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }


}
