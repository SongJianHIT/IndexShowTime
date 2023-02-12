/**
 * @projectName stock_parent
 * @package tech.songjian.stock.config.vo
 * @className tech.songjian.stock.config.vo.TaskThreadPollInfo
 */
package tech.songjian.stock.config.vo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TaskThreadPollInfo
 * @description 任务线程池配置类
 * @author SongJian
 * @date 2023/2/12 11:48
 * @version
 */
@ConfigurationProperties(prefix = "task.pool")
@Data
public class TaskThreadPollInfo {
    /**
     * 核心线程数
     */
    private Integer corePoolSize;
    /**
     * 最大线程数
     */
    private Integer maxPoolSize;
    /**
     * 空闲线程最大存活时间
     */
    private Integer keepAliveSeconds;
    /**
     * 线程任务队列
     */
    private Integer queueCapacity;
}

