package whu.edu.cn.ogedagboot.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateRequest {
    private String state;
    private String batchSessionId;
    private String dagId;
}
