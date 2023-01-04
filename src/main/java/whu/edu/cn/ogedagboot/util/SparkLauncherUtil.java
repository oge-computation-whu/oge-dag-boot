package whu.edu.cn.ogedagboot.util;

import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.springframework.web.bind.annotation.RequestBody;
import whu.edu.cn.ogedagboot.confg.SparkAppParasConfig;

import java.io.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import static whu.edu.cn.ogedagboot.util.SSHClientUtil.runCmd;
import static whu.edu.cn.ogedagboot.util.SSHClientUtil.versouSshUtil;

public class SparkLauncherUtil {
    public static String sparkLauncherTrigger(String param) throws IOException {
        HashMap<String, String> env = new HashMap<>();
        env.put("HADOOP_CONF_DIR", "/home/geocube/hadoop/etc/hadoop");
        env.put("JAVA_HOME", "/home/geocube/jdk1.8.0_131/");
        env.put("SPARK_HOME", "/home/geocube/spark");
        env.put("SPARK_PRINT_LAUNCH_COMMAND", "1");
        CountDownLatch countDownLatch = new CountDownLatch(1);

        SparkAppHandle handle = new SparkLauncher(env)
                .setAppResource("/home/geocube/oge/oge-server/dag-boot/oge-computation_ogc_on_the_fly.jar")
                .setMainClass("whu.edu.cn.application.oge.Trigger")
                .addAppArgs(param)
                .setMaster("spark://125.220.153.26:7077")
                .setConf("spark.driver.memory", "30g")
                .setConf("spark.executor.memory", "10g")
                .setConf("spark.executor.cores", "30")
                .setConf("spark.driver.maxResultSize", "4g")
                .setVerbose(true).startApplication(new SparkAppHandle.Listener() {
                    @Override
                    public void stateChanged(SparkAppHandle sparkAppHandle) {
                        if (sparkAppHandle.getState().isFinal()) {
                            countDownLatch.countDown();
                        }
                        System.out.println("state:" + sparkAppHandle.getState().toString());
                    }

                    @Override
                    public void infoChanged(SparkAppHandle sparkAppHandle) {
                        System.out.println("Info:" + sparkAppHandle.getState().toString());
                    }
                });
        String appID = handle.getAppId();

        //读取output.txt
        File file = new File("/mnt/storage/oge/on-the-fly/output_" + appID + ".txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String st;
        while (true) {
            try {
                if (((st = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return st;
    }

    public static void main(String[] args) {
        sparkSubmitTrigger("aaaa");
    }

    public static String sparkSubmitTrigger(String param) {
        String jsonString = param;
        Long time = System.currentTimeMillis();
        String fileNameJson = "/home/geocube/oge/oge-server/dag-boot/on-the-fly/outputjson_" + time + ".txt";
        String fileName = "/home/geocube/oge/oge-server/dag-boot/on-the-fly/output_" + time + ".txt";
        File writeFile = new File(fileNameJson);
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(writeFile));
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            versouSshUtil("125.220.153.26", "geocube", "ypfamily608", 22);
            String st = "kill -9 $(lsof -i:4040 -t)" + "\n" + "kill -9 $(lsof -i:4041 -t)" + "\n" + "kill -9 $(lsof -i:4042 -t)" + "\n" +
                    "cd /home/geocube/oge" + "\n" + "rm -rf on-the-fly" + "\n" + "mkdir on-the-fly" + "\n" +
                    "cd /home/geocube/oge/oge-server/dag-boot" + "\n" + "rm -rf webapi" + "\n" + "mkdir webapi" + "\n" +
                    "cd /home/geocube/tomcat8/apache-tomcat-8.5.57/webapps/" + "\n" + "rm -rf webapi" + "\n" + "mkdir webapi" + "\n" +
                    "/home/geocube/spark/bin/spark-submit --master local[2] --class whu.edu.cn.application.oge.Trigger --driver-memory 30G --executor-memory 10G --conf spark.driver.maxResultSize=4G /home/geocube/oge/oge-server/dag-boot/oge-computation_ogc_on_the_fly.jar " + fileNameJson + " " + fileName + "\n";
            System.out.println("st = " + st);
            runCmd(st, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //读取output.txt
        File file = new File(fileName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String stout;
        while (true) {
            try {
                if (((stout = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stout;
    }
    public static String sparkSubmitTriggerBatch(String param) {
        String jsonString = param;
        Long time = System.currentTimeMillis();
        String fileNameJson = "/home/geocube/oge/oge-server/dag-boot/batch/outputjson_" + time + ".txt";
        String fileName = "/home/geocube/oge/oge-server/dag-boot/batch/output_" + time + ".txt";
        File writeFile = new File(fileNameJson);
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(writeFile));
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            versouSshUtil("125.220.153.26", "geocube", "ypfamily608", 22);
            String st = "cd /home/geocube/tomcat8/apache-tomcat-8.5.57/webapps/" + "\n" + "rm -rf ogeoutput" + "\n" + "mkdir ogeoutput" + "\n" +
                    "cd /home/geocube/oge" + "\n" + "rm -rf on-the-fly" + "\n" + "mkdir on-the-fly" + "\n" +
                    "cd /home/geocube/oge/oge-server/dag-boot" + "\n" + "rm -rf webapi" + "\n" + "mkdir webapi" + "\n" +
                    "cd /home/geocube/tomcat8/apache-tomcat-8.5.57/webapps/" + "\n" + "rm -rf webapi" + "\n" + "mkdir webapi" + "\n" +
                    "/home/geocube/spark/bin/spark-submit --master spark://125.220.153.26:7077 --class whu.edu.cn.application.oge.Trigger --driver-memory 30G --executor-memory 10G --total-executor-cores 30 --conf spark.driver.maxResultSize=4G /home/geocube/oge/oge-server/dag-boot/oge-computation_ogc_on_the_fly.jar " + fileNameJson + " " + fileName + "\n";
            System.out.println("st = " + st);
            runCmd(st, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //读取output.txt
        File file = new File(fileName);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String stout;
        while (true) {
            try {
                if (((stout = br.readLine()) != null)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stout;
    }
}
