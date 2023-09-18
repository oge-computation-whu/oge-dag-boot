package whu.edu.cn.ogedagboot.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.sql.Timestamp;

@ApiModel(description = "任务实体类")
public class Task {
    @ApiModelProperty(value = "任务唯一编号")
    private String id;
    @ApiModelProperty(value = "前端关联的DAG编号")
    private String DagId;
    @ApiModelProperty(value = "计算端livy关联的batchSession编号")
    private String batchSessionId;
    @ApiModelProperty(value = "任务状态")
    private String state;
    @ApiModelProperty(value = "任务开始时间")
    private Timestamp startTime;
    @ApiModelProperty(value = "任务结束时间")
    private Timestamp endTime;
    @ApiModelProperty(value = "任务运行时间")
    private Double runTime;
    @ApiModelProperty(value = "任务名称")
    private String taskName;
    @ApiModelProperty(value = "坐标系")
    private String crs;
    @ApiModelProperty(value = "尺度")
    private String scale;
    @ApiModelProperty(value = "用户编号")
    private String userId;
    @ApiModelProperty(value = "用户名称")
    private String userName;
    @ApiModelProperty(value = "任务描述")
    private String description;
    @ApiModelProperty(value = "报错信息")
    private String error;
    @ApiModelProperty(value = "输出文件格式")
    private String format;
    @ApiModelProperty(value = "输出资源文件夹路径")
    private String folder;
    @ApiModelProperty(value = "输出文件名")
    private String filename;
    @ApiModelProperty(value = "代码内容")
    private String script;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDagId() {
        return DagId;
    }

    public void setDagId(String dagId) {
        DagId = dagId;
    }

    public String getBatchSessionId() {
        return batchSessionId;
    }

    public void setBatchSessionId(String batchSessionId) {
        this.batchSessionId = batchSessionId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Double getRunTime() {
        return runTime;
    }

    public void setRunTime(Double runTime) {
        this.runTime = runTime;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getErorr() {
        return error;
    }

    public void setErorr(String error) {
        this.error = error;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
