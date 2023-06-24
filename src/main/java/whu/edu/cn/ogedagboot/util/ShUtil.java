package whu.edu.cn.ogedagboot.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import whu.edu.cn.ogedagboot.ResponseBody.OGEScriptExecuteResponse;
import whu.edu.cn.ogedagboot.util.entity.MatchResult;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ShUtil {

    @Value(value = "${python.execute-sh}")
    String executeSh;

    @Value(value = "${python.store-dir}")
    String storeDir;

    /**
     *  call the shell
     * @param builder the command
     * @return if success
     */
    public boolean callShell(ProcessBuilder builder){
        String command = String.join(" ", builder.command());
        try {
            log.info("调用脚本程序" + command);
            // merge the error stream into formal stream
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
            p.waitFor();
            log.info("脚本程序执行完毕");
            return true;
        } catch (Exception e){
            e.printStackTrace();
            log.error(command + " execute fail");
            return false;
        }
    }

    /**
     *  call the shell
     * @param builder the command
     * @return if success
     */
    public String callShellForValue(ProcessBuilder builder){
        String command = String.join(" ", builder.command());
        StringBuilder output = new StringBuilder();
        try {
            log.info("调用脚本程序" + command);
            // merge the error stream into formal stream
            builder.redirectErrorStream(true);
            Process p = builder.start();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = reader.readLine()) != null) {
                output.append(line);
                output.append("\n");
                log.info(line);
            }
            p.waitFor();
            log.info("脚本程序执行完毕");
            return output.toString();
        } catch (Exception e){
            e.printStackTrace();
            log.error(command + " execute fail");
            return output.toString();
        }
    }

    /**
     * Execute the python file
     * @param code the code of python file
     * @return DAG params
     */
    public OGEScriptExecuteResponse executeOGEScript(String code){
        String pythonFilePath = formPythonFile(code);
        //String output = code;
        String output = callShellForValue((new ProcessBuilder("bash", executeSh, pythonFilePath)));
        log.info(output);
        // get dagList, maybe many dags
        MatchResult matchResult1 = extractValueBetweenAngleBrackets(output, Pattern.compile("dag=<<(.*?)>>\n"));
        List<String> dagList = matchResult1.getMatchList();
        output = matchResult1.getModifiedStr();
        JSONArray dagArray = new JSONArray();
        for(String dag : dagList){
            dagArray.add(JSONObject.parseObject(dag));
        }
        // get spaceParamsList, only get last one
        MatchResult matchResult2 = extractValueBetweenAngleBrackets(output, Pattern.compile("spaceParams=<<(.*?)>>\n"));
        List<String> spaceParamsList = matchResult2.getMatchList();
        output = matchResult2.getModifiedStr();
        JSONObject spaceObj = new JSONObject();
        // if the length of spaceParamsList don't equal 0
        if(spaceParamsList.size() != 0){
            spaceObj = JSONObject.parseObject(spaceParamsList.get(spaceParamsList.size() - 1));
        }
        OGEScriptExecuteResponse ogeScriptExecuteResponse = new OGEScriptExecuteResponse();
        ogeScriptExecuteResponse.setLog(output);
        ogeScriptExecuteResponse.setSpaceParams(spaceObj);
        ogeScriptExecuteResponse.setDagList(dagArray);
        return ogeScriptExecuteResponse;
    }


    /**
     * extract value from the output string of python file
     * @param input input string
     * @param pattern the match pattern
     * @return extracted value
     */
    public MatchResult extractValueBetweenAngleBrackets(String input, Pattern pattern) {
        MatchResult matchResult = new MatchResult();
        List<String> matches = new ArrayList<>();
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        input = matcher.replaceAll("");
        matchResult.setMatchList(matches);
        matchResult.setModifiedStr(input);
        return matchResult;
    }

    /**
     * form the python file path
     * @param pythonCode the input python code
     * @return python file path
     */
    public String formPythonFile(String pythonCode){
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        // 定义时间戳格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        // 格式化时间戳
        String timestamp = currentTime.format(formatter);
        String pythonFilePath = storeDir + timestamp + ".py";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pythonFilePath))) {
            writer.write(pythonCode);
            Process process = Runtime.getRuntime().exec("chmod 777 "+ pythonFilePath);
            process.waitFor();
            return pythonFilePath;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String [] args) throws IOException {
        ShUtil shUtil = new ShUtil();
//        try {
////            String codeStr = "import oge.mapclient\\n# 初始化\\noge.initialize()\\nservice = oge.Service.initialize()\\n# Modis数据集获取\\nmodisCollection1 = service.getCoverageCollection(productID=\"MOD13Q1_061\", bbox=[73.62, 18.19, 134.7601467382, 53.54],\\n                                                    datetime=[\"2022-03-06 00:00:00\", \"2022-03-06 00:00:00\"])\\nmodisCollection2 = modisCollection1.subCollection(\\n    filter=oge.Filter([oge.Filter.equals(\"crs\", \"EPSG:4326\"), oge.Filter.equals(\"measurementName\", \"NDVI\")]))\\n# 二值化\\nbinary_Collection = service.getProcess(\"CoverageCollection.binarization\").execute(modisCollection2, 220)\\n# 地图可视化\\nvis_params = {'min': 0, 'max': 255, 'method': \"timeseries\", 'palette': \"green\"}\\noge.mapclient.centerMap(113.5, 24.5, 5)\\nbinary_Collection.styles(vis_params).getMap()\\n   ";
////            String decodeStr = URLDecoder.decode("import oge.mapclient\\n# 初始化\\noge.initialize()\\nservice = oge.Service.initialize()\\n# Modis数据集获取\\nmodisCollection1 = service.getCoverageCollection(productID=\"MOD13Q1_061\", bbox=[73.62, 18.19, 134.7601467382, 53.54],\\n                                                    datetime=[\"2022-03-06 00:00:00\", \"2022-03-06 00:00:00\"])\\nmodisCollection2 = modisCollection1.subCollection(\\n    filter=oge.Filter([oge.Filter.equals(\"crs\", \"EPSG:4326\"), oge.Filter.equals(\"measurementName\", \"NDVI\")]))\\n# 二值化\\nbinary_Collection = service.getProcess(\"CoverageCollection.binarization\").execute(modisCollection2, 220)\\n# 地图可视化\\nvis_params = {'min': 0, 'max': 255, 'method': \"timeseries\", 'palette': \"green\"}\\noge.mapclient.centerMap(113.5, 24.5, 5)\\nbinary_Collection.styles(vis_params).getMap()\\n   ", StandardCharsets.UTF_8.toString());
//            String codeStr = "import%20oge.mapclient%5Cn%23%20%E5%88%9D%E5%A7%8B%E5%8C%96%5Cnoge.initialize()%5Cnservice%20%3D%20oge.Service.initialize()%5Cn%23%20Modis%E6%95%B0%E6%8D%AE%E9%9B%86%E8%8E%B7%E5%8F%96%5CnmodisCollection1%20%3D%20service.getCoverageCollection(productID%3D%22MOD13Q1_061%22%2C%20bbox%3D%5B73.62%2C%2018.19%2C%20134.7601467382%2C%2053.54%5D%2C%5Cn%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20datetime%3D%5B%222022-03-06%2000%3A00%3A00%22%2C%20%222022-03-06%2000%3A00%3A00%22%5D)%5CnmodisCollection2%20%3D%20modisCollection1.subCollection(%5Cn%20%20%20%20filter%3Doge.Filter(%5Boge.Filter.equals(%22crs%22%2C%20%22EPSG%3A4326%22)%2C%20oge.Filter.equals(%22measurementName%22%2C%20%22NDVI%22)%5D))%5Cn%23%20%E4%BA%8C%E5%80%BC%E5%8C%96%5Cnbinary_Collection%20%3D%20service.getProcess(%22CoverageCollection.binarization%22).execute(modisCollection2%2C%20220)%5Cn%23%20%E5%9C%B0%E5%9B%BE%E5%8F%AF%E8%A7%86%E5%8C%96%5Cnvis_params%20%3D%20%7B'min'%3A%200%2C%20'max'%3A%20255%2C%20'method'%3A%20%22timeseries%22%2C%20'palette'%3A%20%22green%22%7D%5Cnoge.mapclient.centerMap(113.5%2C%2024.5%2C%205)%5Cnbinary_Collection.styles(vis_params).getMap()%5Cn%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20%20";
//            String decodedCode = URLDecoder.decode(codeStr, "UTF-8");
////            String code = decodedCode.replace("%0A", "\n");
////            String replacedStr = codeStr.replaceAll("\n", System.lineSeparator());
//            String pythonFilePath = "E:/LaoK/data2/test.py";
////            System.out.println(code);
//            try (BufferedWriter writer = new BufferedWriter(new FileWriter(pythonFilePath))) {
//                writer.write(decodedCode.replaceAll("\n", "\r\n"));
//            }
//        } catch (UnsupportedEncodingException ex){
//            throw new RuntimeException(ex.getCause());
//        }
        // JSONObject resultObj = shUtil.executeOGEScript("spaceParams=<<{'lng': 101.2275, 'lat': 25.1425, 'zoom': 15}>>dag=<<{'dag': '{\"0\": {\"functionInvocationValue\": {\"functionName\": \"Coverage.addStyles\", \"arguments\": {\"input\": {\"functionInvocationValue\": {\"functionName\": \"Coverage.croplandDetection\", \"arguments\": {\"input\": {\"functionInvocationValue\": {\"functionName\": \"Service.getCoverage\", \"arguments\": {\"baseUrl\": {\"constantValue\": \"http://localhost\"}, \"coverageID\": {\"constantValue\": \"BJ2002F8CVI_00220211229C10_COG\"}}}}}}}, \"max\": {\"constantValue\": 1}, \"min\": {\"constantValue\": 0}}}}}', 'layerName': 'croplandDetection'}>>dag=<<{'dag': '{\"0\": {\"functionInvocationValue\": {\"functionName\": \"Coverage.addStyles\", \"arguments\": {\"input\": {\"functionInvocationValue\": {\"functionName\": \"Coverage.croplandDetection\", \"arguments\": {\"input\": {\"functionInvocationValue\": {\"functionName\": \"Service.getCoverage\", \"arguments\": {\"baseUrl\": {\"constantValue\": \"http://localhost\"}, \"coverageID\": {\"constantValue\": \"BJ2002F8CVI_00220211229C10_COG\"}}}}}}}, \"max\": {\"constantValue\": 1}, \"min\": {\"constantValue\": 0}}}}}', 'layerName': 'croplandDetection2'}>>\n");
        String a = "s";
        OGEScriptExecuteResponse ogeScriptExecuteResponse = shUtil.executeOGEScript("hellow wkx \n spaceParams=<<{'lng': 101.2275, 'lat': 25.1425, 'zoom': 15}>>\n" +
                "dag=<<{'dag': '{\"0\": {\"functionInvocationValue\": {\"functionName\": \"Coverage.addStyles\", \"arguments\": {\"input\": {\"functionInvocationValue\": {\"functionName\": \"Coverage.croplandDetection\", \"arguments\": {\"input\": {\"functionInvocationValue\": {\"functionName\": \"Service.getCoverage\", \"arguments\": {\"baseUrl\": {\"constantValue\": \"http://localhost\"}, \"coverageID\": {\"constantValue\": \"BJ2002F8CVI_00220211229C10_COG\"}}}}}}}, \"max\": {\"constantValue\": 1}, \"min\": {\"constantValue\": 0}}}}}', 'layerName': 'croplandDetection'}>>\n");
       String log = ogeScriptExecuteResponse.getLog();
        System.out.println(log);
        // String a = "s";
    }
}
