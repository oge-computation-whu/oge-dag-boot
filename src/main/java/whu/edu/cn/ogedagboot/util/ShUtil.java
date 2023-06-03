package whu.edu.cn.ogedagboot.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
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
    public JSONObject executeOGEScript(String code){
        String pythonFilePath = formPythonFile(code);
        //String output = code;
        String output = callShellForValue((new ProcessBuilder("bash", executeSh, pythonFilePath)));
        log.info(output);
        // get dagList, maybe many dags
        List<String> dagList = extractValueBetweenAngleBrackets(output, Pattern.compile("dag=<<(.*?)>>"));
        JSONArray dagArray = new JSONArray();
        for(String dag : dagList){
            dagArray.add(JSONObject.parseObject(dag));
        }
        // get spaceParamsList, only get last one
        List<String> spaceParamsList = extractValueBetweenAngleBrackets(output, Pattern.compile("spaceParams=<<(.*?)>>"));
        JSONObject spaceObj = new JSONObject();
        // if the length of spaceParamsList don't equal 0
        if(spaceParamsList.size() != 0){
            spaceObj = JSONObject.parseObject(spaceParamsList.get(spaceParamsList.size() - 1));
        }
        JSONObject resultObj = new JSONObject();
        resultObj.put("spaceParams", spaceObj);
        resultObj.put("dagList", dagArray);
        return resultObj;
    }


    /**
     * extract value from the output string of python file
     * @param input input string
     * @param pattern the match pattern
     * @return extracted value
     */
    public List<String> extractValueBetweenAngleBrackets(String input, Pattern pattern) {
        List<String> matches = new ArrayList<>();
//        Pattern pattern = Pattern.compile("<<(.*?)>>");
        Matcher matcher = pattern.matcher(input);
       while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        return matches;
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

    public static void main(String [] args){
        ShUtil shUtil = new ShUtil();
        JSONObject resultObj = shUtil.executeOGEScript("spaceParams=<<{'lng': 101.2275, 'lat': 25.1425, 'zoom': 15}>>dag=<<{'dag': '{\"0\": {\"functionInvocationValue\": {\"functionName\": \"Coverage.addStyles\", \"arguments\": {\"input\": {\"functionInvocationValue\": {\"functionName\": \"Coverage.croplandDetection\", \"arguments\": {\"input\": {\"functionInvocationValue\": {\"functionName\": \"Service.getCoverage\", \"arguments\": {\"baseUrl\": {\"constantValue\": \"http://localhost\"}, \"coverageID\": {\"constantValue\": \"BJ2002F8CVI_00220211229C10_COG\"}}}}}}}, \"max\": {\"constantValue\": 1}, \"min\": {\"constantValue\": 0}}}}}', 'layerName': 'croplandDetection'}>>dag=<<{'dag': '{\"0\": {\"functionInvocationValue\": {\"functionName\": \"Coverage.addStyles\", \"arguments\": {\"input\": {\"functionInvocationValue\": {\"functionName\": \"Coverage.croplandDetection\", \"arguments\": {\"input\": {\"functionInvocationValue\": {\"functionName\": \"Service.getCoverage\", \"arguments\": {\"baseUrl\": {\"constantValue\": \"http://localhost\"}, \"coverageID\": {\"constantValue\": \"BJ2002F8CVI_00220211229C10_COG\"}}}}}}}, \"max\": {\"constantValue\": 1}, \"min\": {\"constantValue\": 0}}}}}', 'layerName': 'croplandDetection2'}>>\n");
        String a = "s";
//        JSONObject resultObj = shUtil.executeOGEScript("spaceParams=<<{'lng': 101.2275, 'lat': 25.1425, 'zoom': 15}>>\n" +
//                "dag=<<{'dag': '{\"0\": {\"functionInvocationValue\": {\"functionName\": \"Coverage.addStyles\", \"arguments\": {\"input\": {\"functionInvocationValue\": {\"functionName\": \"Coverage.croplandDetection\", \"arguments\": {\"input\": {\"functionInvocationValue\": {\"functionName\": \"Service.getCoverage\", \"arguments\": {\"baseUrl\": {\"constantValue\": \"http://localhost\"}, \"coverageID\": {\"constantValue\": \"BJ2002F8CVI_00220211229C10_COG\"}}}}}}}, \"max\": {\"constantValue\": 1}, \"min\": {\"constantValue\": 0}}}}}', 'layerName': 'croplandDetection'}>>\n");
//        String a = "s";
    }
}
