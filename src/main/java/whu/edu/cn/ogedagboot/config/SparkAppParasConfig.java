package whu.edu.cn.ogedagboot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Submit parameters for spark application.
 */
@Component
@ConfigurationProperties(prefix = "sparkappparas")
public class SparkAppParasConfig {
    private String sparkHome;
    private String master;
    private Map<String, String> mainClass;
    private String driverMemory;
    private String executorMemory;
    private String totalExecutorCores;
    private String executorCores;
    private Map<String, String> jarPath;
    private String deployMode;
    private String rpcMessageMaxSize;
    private String kryoserializerBufferMax;
    private String sparkDriverMaxResultSize;

    public String getSparkDriverMaxResultSize() {
        return sparkDriverMaxResultSize;
    }

    public void setSparkDriverMaxResultSize(String sparkDriverMaxResultSize) {
        this.sparkDriverMaxResultSize = sparkDriverMaxResultSize;
    }

    public String getRpcMessageMaxSize() {
        return rpcMessageMaxSize;
    }

    public void setRpcMessageMaxSize(String rpcMessageMaxSize) {
        this.rpcMessageMaxSize = rpcMessageMaxSize;
    }

    public String getKryoserializerBufferMax() {
        return kryoserializerBufferMax;
    }

    public void setKryoserializerBufferMax(String kryoserializerBufferMax) {
        this.kryoserializerBufferMax = kryoserializerBufferMax;
    }

    public String getSparkHome() {
        return sparkHome;
    }

    public void setSparkHome(String sparkHome) {
        this.sparkHome = sparkHome;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public Map<String, String> getMainClass() {
        return mainClass;
    }

    public void setMainClass(Map<String, String> mainClass) {
        this.mainClass = mainClass;
    }

    public String getDriverMemory() {
        return driverMemory;
    }

    public void setDriverMemory(String driverMemory) {
        this.driverMemory = driverMemory;
    }

    public String getExecutorMemory() {
        return executorMemory;
    }

    public void setExecutorMemory(String executorMemory) {
        this.executorMemory = executorMemory;
    }

    public String getTotalExecutorCores() {
        return totalExecutorCores;
    }

    public void setTotalExecutorCores(String totalExecutorCores) {
        this.totalExecutorCores = totalExecutorCores;
    }

    public String getExecutorCores() {
        return executorCores;
    }

    public void setExecutorCores(String executorCores) {
        this.executorCores = executorCores;
    }

    public Map<String, String> getJarPath() {
        return jarPath;
    }

    public void setJarPath(Map<String, String> jarPath) {
        this.jarPath = jarPath;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }

}
