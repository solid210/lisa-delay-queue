package org.lisa.delayqueue.base.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.List;

/**
 * @description:
 * @author: wuxu
 * @createDate: 2022/10/8
 */
@Slf4j
public class RedisScriptUtils {

    public static DefaultRedisScript<String> getRedisScript(String resourceName, String luaName) {
        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
        //resource目录下的scripts文件下的.lua文件
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(resourceName)));
        redisScript.setResultType(String.class);
        loadRedisScript(redisScript, luaName);
        return redisScript;
    }

    public static <T> DefaultRedisScript<T> getRedisScript(String resourceName, String luaName, Class<T> clazz) {
        DefaultRedisScript<T> redisScript = new DefaultRedisScript<>();
        //resource目录下的scripts文件下的.lua文件
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(resourceName)));
        redisScript.setResultType(clazz);
        loadRedisScript(redisScript, luaName);
        return redisScript;
    }

    /**
     * 加载lua脚本到redis服务器
     *
     * @param redisScript
     * @param luaName
     */
    public static void loadRedisScript(DefaultRedisScript<?> redisScript, String luaName) {
        try {
            String sha1 = redisScript.getSha1();
            log.debug("luaName -> {}, script sha1 -> {}", luaName, sha1);
            List<Boolean> results = SpringContextUtil.getBean(StringRedisTemplate.class).getConnectionFactory().getConnection().scriptExists(sha1);
            if (Boolean.FALSE.equals(results.get(0))) {
                String sha = SpringContextUtil.getBean(StringRedisTemplate.class).getConnectionFactory().getConnection().scriptLoad(scriptBytes(redisScript));
                log.info("预加载lua脚本成功：{}, sha -> {}", luaName, sha);
            }
        } catch (Exception e) {
            log.error("预加载lua脚本异常：{}", luaName, e);
        }
    }

    /**
     * 序列化lua脚本
     *
     * @param script
     * @return
     */
    public static byte[] scriptBytes(RedisScript<?> script) {
        return SpringContextUtil.getBean(StringRedisTemplate.class).getStringSerializer().serialize(script.getScriptAsString());
    }
}
