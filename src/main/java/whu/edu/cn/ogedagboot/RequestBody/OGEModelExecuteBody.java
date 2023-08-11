package whu.edu.cn.ogedagboot.RequestBody;

import lombok.Data;

@Data
public class OGEModelExecuteBody {

    private String modelString;

    private String userId;

    private SpaceParams spaceParams;
}

@Data
class SpaceParams {

    private double lat;

    private double lng;

    private int zoom;
}
