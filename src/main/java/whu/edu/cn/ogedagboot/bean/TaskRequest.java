package whu.edu.cn.ogedagboot.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "请求任务参数类")
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
    @ApiModelProperty(value = "输出文件名")
    private String filename;
}
