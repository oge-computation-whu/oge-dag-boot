package whu.edu.cn.ogedagboot.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GlobalConstantUtil {
    public static String LIVY_HOST;
    public static String LIVY_PORT;
    public static String LIVY_USER;
    public static String LIVY_PWD;
    public static int LIVY_SESSION_NUM;
    public static String COMPUTATION_JAR_PATH;
    public static String SPARK_DRIVER_EXTRA_CLASS_PATH;
    public static String SPARK_EXECUTOR_EXTRA_CLASS_PATH;
    public static Integer DRIVER_CORES;
    public static String DRIVER_MEMORY;
    public static Integer EXECUTOR_CORES;
    public static String EXECUTOR_MEMORY;

    @Value("${livy.host}")
    public void setLivyHost(String livyHost) {
        GlobalConstantUtil.LIVY_HOST = livyHost;
    }

    @Value("${livy.port}")
    public void setLivyPort(String livyPort) {
        GlobalConstantUtil.LIVY_PORT = livyPort;
    }

    @Value("${livy.user}")
    public void setLivyUser(String livyUser) {
        GlobalConstantUtil.LIVY_USER = livyUser;
    }

    @Value("${livy.pwd}")
    public void setLivyPwd(String livyPwd) {
        GlobalConstantUtil.LIVY_PWD = livyPwd;
    }

    @Value("${livy.sessionNum}")
    public void setSessionNum(Integer sessionNum) {
        GlobalConstantUtil.LIVY_SESSION_NUM = sessionNum;
    }

    @Value("${ogc.computationJarPath}")
    public void setComputationJarPath(String computationJarPath) {
        GlobalConstantUtil.COMPUTATION_JAR_PATH = computationJarPath;
    }

    @Value("${ogc.sparkDriverExtraClassPath}")
    public void setSparkDriverExtraClassPath(String sparkDriverExtraClassPath) {
        GlobalConstantUtil.SPARK_DRIVER_EXTRA_CLASS_PATH = sparkDriverExtraClassPath;
    }

    @Value("${ogc.sparkExecutorExtraClassPath}")
    public void setSparkExecutorExtraClassPath(String sparkExecutorExtraClassPath) {
        GlobalConstantUtil.SPARK_EXECUTOR_EXTRA_CLASS_PATH = sparkExecutorExtraClassPath;
    }

    @Value("${ogc.driverCores}")
    public void setDriverCores(Integer driverCores) {
        GlobalConstantUtil.DRIVER_CORES = driverCores;
    }

    @Value("${ogc.driverMemory}")
    public void setDriverMemory(String driverMemory) {
        GlobalConstantUtil.DRIVER_MEMORY = driverMemory;
    }

    @Value("${ogc.executorCores}")
    public void setExecutorCores(Integer executorCores) {
        GlobalConstantUtil.EXECUTOR_CORES = executorCores;
    }

    @Value("${ogc.executorMemory}")
    public void setExecutorMemory(String executorMemory) {
        GlobalConstantUtil.EXECUTOR_MEMORY = executorMemory;
    }

}
