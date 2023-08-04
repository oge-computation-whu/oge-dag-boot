package whu.edu.cn.ogedagboot.RequestBody;

import lombok.Data;

@Data
public class OGEModelExecuteBody {

    private String modelString;

    private String userId;

    private SpatialParam spatialParam;
}

@Data
class SpatialParam {

    private double lat;

    private double lng;

    private int zoom;
}
