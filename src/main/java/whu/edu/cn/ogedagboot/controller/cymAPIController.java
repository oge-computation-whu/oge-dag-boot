package whu.edu.cn.ogedagboot.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;

import static whu.edu.cn.ogedagboot.util.SparkLauncherUtil.sparkSubmitTrigger;

@RestController
@CrossOrigin(origins = "*",maxAge = 3600)
public class cymAPIController {
    @PostMapping("/slope")
    public String slope(@RequestParam("level") int level, @RequestParam("spatialRange") String spatialRange) {
        File file = new File("/home/geocube/oge/oge-server/dag-boot/cymapi/slope.json");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String ogeDagStr;
        while (true) {
            try {
                if (((ogeDagStr = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject ogeDagJson = JSONObject.parseObject(ogeDagStr);
        String[] spatialRangeList = spatialRange.split(",");
        ArrayList<Float> spatialRangeFloat = new ArrayList<>();
        for (String s : spatialRangeList) {
            spatialRangeFloat.add(Float.parseFloat(s));
        }
        JSONObject mapObject = new JSONObject();
        mapObject.put("level", level);
        mapObject.put("spatialRange", spatialRangeFloat);
        ogeDagJson.put("map", mapObject);
        ogeDagJson.put("oorB", "0");
        String paramStr = ogeDagJson.toJSONString();
        return sparkSubmitTrigger(paramStr);
    }

    @PostMapping("/aspect")
    public String aspect(@RequestParam("level") int level, @RequestParam("spatialRange") String spatialRange) {
        File file = new File("/home/geocube/oge/oge-server/dag-boot/cymapi/aspect.json");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String ogeDagStr;
        while (true) {
            try {
                if (((ogeDagStr = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject ogeDagJson = JSONObject.parseObject(ogeDagStr);
        String[] spatialRangeList = spatialRange.split(",");
        ArrayList<Float> spatialRangeFloat = new ArrayList<>();
        for (String s : spatialRangeList) {
            spatialRangeFloat.add(Float.parseFloat(s));
        }
        JSONObject mapObject = new JSONObject();
        mapObject.put("level", level);
        mapObject.put("spatialRange", spatialRangeFloat);
        ogeDagJson.put("map", mapObject);
        ogeDagJson.put("oorB", "0");
        String paramStr = ogeDagJson.toJSONString();
        return sparkSubmitTrigger(paramStr);
    }

    @PostMapping("/hillShade")
    public String hillShade(@RequestParam("level") int level, @RequestParam("spatialRange") String spatialRange) {
        File file = new File("/home/geocube/oge/oge-server/dag-boot/cymapi/hillShade.json");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String ogeDagStr;
        while (true) {
            try {
                if (((ogeDagStr = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject ogeDagJson = JSONObject.parseObject(ogeDagStr);
        String[] spatialRangeList = spatialRange.split(",");
        ArrayList<Float> spatialRangeFloat = new ArrayList<>();
        for (String s : spatialRangeList) {
            spatialRangeFloat.add(Float.parseFloat(s));
        }
        JSONObject mapObject = new JSONObject();
        mapObject.put("level", level);
        mapObject.put("spatialRange", spatialRangeFloat);
        ogeDagJson.put("map", mapObject);
        ogeDagJson.put("oorB", "0");
        String paramStr = ogeDagJson.toJSONString();
        return sparkSubmitTrigger(paramStr);
    }

    @PostMapping("/relief")
    public String relief(@RequestParam("level") int level, @RequestParam("spatialRange") String spatialRange) {
        File file = new File("/home/geocube/oge/oge-server/dag-boot/cymapi/relief.json");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String ogeDagStr;
        while (true) {
            try {
                if (((ogeDagStr = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject ogeDagJson = JSONObject.parseObject(ogeDagStr);
        String[] spatialRangeList = spatialRange.split(",");
        ArrayList<Float> spatialRangeFloat = new ArrayList<>();
        for (String s : spatialRangeList) {
            spatialRangeFloat.add(Float.parseFloat(s));
        }
        JSONObject mapObject = new JSONObject();
        mapObject.put("level", level);
        mapObject.put("spatialRange", spatialRangeFloat);
        ogeDagJson.put("map", mapObject);
        ogeDagJson.put("oorB", "0");
        String paramStr = ogeDagJson.toJSONString();
        return sparkSubmitTrigger(paramStr);
    }

    @PostMapping("/ruggednessIndex")
    public String ruggednessIndex(@RequestParam("level") int level, @RequestParam("spatialRange") String spatialRange) {
        File file = new File("/home/geocube/oge/oge-server/dag-boot/cymapi/ruggednessIndex.json");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String ogeDagStr;
        while (true) {
            try {
                if (((ogeDagStr = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject ogeDagJson = JSONObject.parseObject(ogeDagStr);
        String[] spatialRangeList = spatialRange.split(",");
        ArrayList<Float> spatialRangeFloat = new ArrayList<>();
        for (String s : spatialRangeList) {
            spatialRangeFloat.add(Float.parseFloat(s));
        }
        JSONObject mapObject = new JSONObject();
        mapObject.put("level", level);
        mapObject.put("spatialRange", spatialRangeFloat);
        ogeDagJson.put("map", mapObject);
        ogeDagJson.put("oorB", "0");
        String paramStr = ogeDagJson.toJSONString();
        return sparkSubmitTrigger(paramStr);
    }

    @PostMapping("/cellBalance")
    public String cellBalance(@RequestParam("level") int level, @RequestParam("spatialRange") String spatialRange) {
        File file = new File("/home/geocube/oge/oge-server/dag-boot/cymapi/cellBalance.json");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String ogeDagStr;
        while (true) {
            try {
                if (((ogeDagStr = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject ogeDagJson = JSONObject.parseObject(ogeDagStr);
        String[] spatialRangeList = spatialRange.split(",");
        ArrayList<Float> spatialRangeFloat = new ArrayList<>();
        for (String s : spatialRangeList) {
            spatialRangeFloat.add(Float.parseFloat(s));
        }
        JSONObject mapObject = new JSONObject();
        mapObject.put("level", level);
        mapObject.put("spatialRange", spatialRangeFloat);
        ogeDagJson.put("map", mapObject);
        ogeDagJson.put("oorB", "0");
        String paramStr = ogeDagJson.toJSONString();
        return sparkSubmitTrigger(paramStr);
    }

    @PostMapping("/flowAccumulationTD")
    public String flowAccumulationTD(@RequestParam("level") int level, @RequestParam("spatialRange") String spatialRange) {
        File file = new File("/home/geocube/oge/oge-server/dag-boot/cymapi/flowAccumulationTD.json");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String ogeDagStr;
        while (true) {
            try {
                if (((ogeDagStr = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject ogeDagJson = JSONObject.parseObject(ogeDagStr);
        String[] spatialRangeList = spatialRange.split(",");
        ArrayList<Float> spatialRangeFloat = new ArrayList<>();
        for (String s : spatialRangeList) {
            spatialRangeFloat.add(Float.parseFloat(s));
        }
        JSONObject mapObject = new JSONObject();
        mapObject.put("level", level);
        mapObject.put("spatialRange", spatialRangeFloat);
        ogeDagJson.put("map", mapObject);
        ogeDagJson.put("oorB", "0");
        String paramStr = ogeDagJson.toJSONString();
        return sparkSubmitTrigger(paramStr);
    }

    @PostMapping("/flowPathLength")
    public String flowPathLength(@RequestParam("level") int level, @RequestParam("spatialRange") String spatialRange) {
        File file = new File("/home/geocube/oge/oge-server/dag-boot/cymapi/flowPathLength.json");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String ogeDagStr;
        while (true) {
            try {
                if (((ogeDagStr = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject ogeDagJson = JSONObject.parseObject(ogeDagStr);
        String[] spatialRangeList = spatialRange.split(",");
        ArrayList<Float> spatialRangeFloat = new ArrayList<>();
        for (String s : spatialRangeList) {
            spatialRangeFloat.add(Float.parseFloat(s));
        }
        JSONObject mapObject = new JSONObject();
        mapObject.put("level", level);
        mapObject.put("spatialRange", spatialRangeFloat);
        ogeDagJson.put("map", mapObject);
        ogeDagJson.put("oorB", "0");
        String paramStr = ogeDagJson.toJSONString();
        return sparkSubmitTrigger(paramStr);
    }

    @PostMapping("/slopeLength")
    public String slopeLength(@RequestParam("level") int level, @RequestParam("spatialRange") String spatialRange) {
        File file = new File("/home/geocube/oge/oge-server/dag-boot/cymapi/slopeLength.json");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String ogeDagStr;
        while (true) {
            try {
                if (((ogeDagStr = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject ogeDagJson = JSONObject.parseObject(ogeDagStr);
        String[] spatialRangeList = spatialRange.split(",");
        ArrayList<Float> spatialRangeFloat = new ArrayList<>();
        for (String s : spatialRangeList) {
            spatialRangeFloat.add(Float.parseFloat(s));
        }
        JSONObject mapObject = new JSONObject();
        mapObject.put("level", level);
        mapObject.put("spatialRange", spatialRangeFloat);
        ogeDagJson.put("map", mapObject);
        ogeDagJson.put("oorB", "0");
        String paramStr = ogeDagJson.toJSONString();
        return sparkSubmitTrigger(paramStr);
    }
}
