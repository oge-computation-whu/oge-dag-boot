package whu.edu.cn.ogedagboot.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 来路详情
 * @Author: Chenzw
 * @Date: 2024/3/7 17:19
 */
@Data
@AllArgsConstructor
public class ResourceInfo {

    /**
     * 标签
     */
    private String label;

    /**
     * 次数
     */
    private Integer count;
}
