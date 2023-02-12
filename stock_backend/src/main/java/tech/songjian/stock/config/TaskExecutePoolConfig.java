/**
 * @projectName stock_parent
 * @package tech.songjian.stock.config
 * @className tech.songjian.stock.config.TaskExecutePoolConfig
 */
package tech.songjian.stock.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import tech.songjian.stock.config.vo.TaskThreadPollInfo;

/**
 * TaskExecutePoolConfig
 * @description 配置线程池对象
 * @author SongJian
 * @date 2023/2/12 11:54
 * @version
 */
@Configuration
@Slf4j
public class TaskExecutePoolConfig {

    @Autowired
    private TaskThreadPollInfo taskThreadPollInfo;

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        // 1、构建线程池对象
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 2、设置参数
        executor.setCorePoolSize(taskThreadPollInfo.getCorePoolSize());
        executor.setMaxPoolSize(taskThreadPollInfo.getMaxPoolSize());
        executor.setKeepAliveSeconds(taskThreadPollInfo.getKeepAliveSeconds());
        executor.setQueueCapacity(taskThreadPollInfo.getQueueCapacity());
        executor.setRejectedExecutionHandler(null);
        // 3、参数初始化
        executor.initialize();
        // 4、返回
        return executor;
    }
}
