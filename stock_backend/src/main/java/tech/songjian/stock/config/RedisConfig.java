/**
 * @projectName stock_parent
 * @package tech.songjian.stock.config
 * @className tech.songjian.stock.config.RedisConfig
 */
package tech.songjian.stock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisConfig
 * @description Redis配置类
 * @author SongJian
 * @date 2023/2/7 16:36
 * @version
 */
@Configuration
public class RedisConfig {

    /**
     * 自定义 key 的序列化
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 自定义 key 序列化工具对象
        // 设置 redis 中 key 的序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 设置 hash 中 field 序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}

