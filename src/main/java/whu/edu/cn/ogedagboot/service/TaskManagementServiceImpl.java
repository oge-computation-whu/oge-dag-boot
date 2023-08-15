package whu.edu.cn.ogedagboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cn.ogedagboot.bean.Task;
import whu.edu.cn.ogedagboot.dao.TaskManagementDao;

import java.util.UUID;


@Service
public class TaskManagementServiceImpl implements TaskManagementService {
    @Autowired
    private TaskManagementDao taskManagementDao;


    @Override
    public boolean addTaskRecord(Task task) {
        boolean flag = false;
        try {
            String taskId = UUID.randomUUID().toString();
            while (taskManagementDao.existsById(taskId) != 0) {
                taskId = UUID.randomUUID().toString();
            }
            task.setId(taskId);
            taskManagementDao.addTaskRecord(task);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
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
    public boolean updateTaskRecordOfstate(String state) {
        boolean flag = false;
        try {
            taskManagementDao.updateTaskRecordOfstate(state);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }


    @Override
    public String getUserIdByUserName(String userName) {
        return taskManagementDao.getUserIdByUserName(userName);
    }

    @Override
    public Task getTaskInfoByDagId(String DagId) {
        return taskManagementDao.getTaskInfoByDagId(DagId);
    }


}
