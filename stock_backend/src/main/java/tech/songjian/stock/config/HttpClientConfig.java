/**
 * @projectName stock_parent
 * @package tech.songjian.stock.config
 * @className tech.songjian.stock.config.HttpClientConfig
 */
package tech.songjian.stock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * HttpClientConfig
 * @description 配置HttpClient客户端工具Bean
 * @author SongJian
 * @date 2023/2/11 14:40
 * @version
 */
@Configuration
public class HttpClientConfig {

    /**
     * 定义restTemplate模版bean
     * @return
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

