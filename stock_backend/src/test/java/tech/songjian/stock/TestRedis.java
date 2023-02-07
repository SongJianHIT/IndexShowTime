/**
 * @projectName stock_parent
 * @package tech.songjian.stock
 * @className tech.songjian.stock.TestRedis
 */
package tech.songjian.stock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * TestRedis
 * @description
 * @author SongJian
 * @date 2023/2/7 16:33
 * @version
 */
@SpringBootTest
public class TestRedis {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test() {
        redisTemplate.opsForValue().set("name", "zhangsan");
        System.out.println(redisTemplate.opsForValue().get("name"));
    }
}

