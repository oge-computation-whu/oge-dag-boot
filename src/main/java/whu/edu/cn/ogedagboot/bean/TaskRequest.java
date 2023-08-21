package whu.edu.cn.ogedagboot.bean;

import io.swagger.annotations.ApiModelProperty;

public class TaskRequest {
    @ApiModelProperty(value = "前端关联的DAG编号")
    private String id;
    @ApiModelProperty(value = "任务名称")
    private String taskName;
    @ApiModelProperty(value = "坐标系")
    private String crs;
    @ApiModelProperty(value = "尺度")
    private String scale;
    @ApiModelProperty(value = "用户名称")
    private String userName;
    @ApiModelProperty(value = "输出文件格式")
    private String format;
    @ApiModelProperty(value = "输出资源文件夹路径")
    private String folder;
    @ApiModelProperty(value = "输出文件名")
    private String filename;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
}
