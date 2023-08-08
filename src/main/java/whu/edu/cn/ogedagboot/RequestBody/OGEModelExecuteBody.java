package whu.edu.cn.ogedagboot.RequestBody;

import lombok.Data;

@Data
public class OGEModelExecuteBody {

    private String modelString;

    private String userId;

    private SpaceParam spaceParams;
}

@Data
class SpaceParam {

    private double lat;

    private double lon;

    private int zoom;
}
