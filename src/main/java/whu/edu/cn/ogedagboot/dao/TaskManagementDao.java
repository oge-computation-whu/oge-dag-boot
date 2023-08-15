package whu.edu.cn.ogedagboot.dao;

import org.springframework.stereotype.Service;
import org.apache.ibatis.annotations.*;
import whu.edu.cn.ogedagboot.bean.Task;

@Service
@Mapper
public interface TaskManagementDao {

    /**
     * 检查数据中taskId是否存在
     */
    @Select("SELECT COUNT(*) FROM oge_task_management WHERE id = #{taskId};")
    int existsById(@Param("taskId") String taskId);

    /**
     * 根据前端DAG_id获取task全部信息
     */
    @Select("SELECT * FROM oge_task_management WHERE DAG_id = #{DagId};")
    Task getTaskInfoByDagId(@Param("DagId") String DagId);

    /**
     * 插入一条任务记录
     */
    @Insert("insert into oge_task_management (" +
            "id,\n" +
            "dag_id,\n" +
            "batch_session_id,\n" +
            "state,\n" +
            "task_name,\n" +
            "crs,\n" +
            "scale,\n" +
            "user_id,\n" +
            "user_name,\n" +
            "description,\n" +
            "format,\n" +
            "folder,\n" +
            "filename\n" +
            ") values (#{id},#{DagId},#{batchSessionId},#{state},#{taskName}," +
            "#{crs},#{scale},#{userId},#{userName},#{description},#{format}," +
            "#{folder},#{filename})")
    void addTaskRecord(Task task);

    /**
     * 删除一条任务记录
     */
    @Delete("delete from oge_task_management where dag_id=#{DagId}")
    void deteleTaskRecord(@Param("DagId") String DagId);


    /**
     * 更新任务状态
     */
    @Update("update oge_task_management set state=#{state}")
    void updateTaskRecordOfstate(@Param("state") String state);

    /**
     * 根据user_name获取user_id
     */
    @Select("select uuid from oge_sys_user where username = #{userName}")
    String getUserIdByUserName(@Param("userName") String userName);
}
