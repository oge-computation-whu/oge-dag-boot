package whu.edu.cn.ogedagboot.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class RedisUtil {
    /**
     * redis 连接池
     */
    @Resource
    private JedisPool jedisPool;

    /**
     * 保存键值对
     *
     * @param key   key
     * @param value value
     * @return true false
     */
    public boolean saveKeyValue(String key, String value) {
        try {
            Jedis jedis = jedisPool.getResource();
            jedis.set(key, value);
            jedis.close();
            return true;
        } catch (Exception e) {
            log.error("存储key:" + key + "失败");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 保存键值对
     *
     * @param key     key
     * @param value   value
     * @param timeout 超时时间 seconds
     * @return true false
     */
    public boolean saveKeyValue(String key, String value, int timeout) {
        try {
            Jedis jedis = jedisPool.getResource();
            jedis.set(key, value);
            jedis.expire(key, timeout);
            jedis.close();
            return true;
        } catch (Exception e) {
            log.error("存储key:" + key + "失败");
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveKeyList(String key, List list) {
        try {
            Jedis jedis = jedisPool.getResource();
            list.forEach((l) -> jedis.rpush(key, String.valueOf(l)));
            jedis.close();
            return true;
        } catch (Exception e) {
            log.error("存储key:" + key + "失败");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取key
     *
     * @param key 键值
     * @return value
     */
    public String getValueByKey(String key) {
        String value = null;
        try {
            Jedis jedis = jedisPool.getResource();
            value = jedis.get(key);
            jedis.close();
        } catch (Exception e) {
            log.error("取出key:" + key + "失败");
            e.printStackTrace();
        }
        return value;
    }

    public List<String> getListByKey(String key, int start, int end) {
        List<String> list = null;
        try {
            Jedis jedis = jedisPool.getResource();
            list = jedis.lrange(key, start, end);
            jedis.close();
        } catch (Exception e) {
            log.error("取出key:" + key + "失败");
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 删除key
     *
     * @param key 删除key
     * @return true false
     */
    public boolean deleteKey(String key) {
        try {
            Jedis jedis = jedisPool.getResource();
            jedis.del(key);
            jedis.close();
            return true;
        } catch (Exception e) {
            log.error("删除key:" + key + "失败");
            e.printStackTrace();
            return false;
        }
    }
}
