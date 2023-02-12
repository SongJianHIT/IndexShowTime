/**
 * @projectName stock_parent
 * @package tech.songjian.stock
 * @className tech.songjian.stock.TestThread
 */
package tech.songjian.stock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.TimeUnit;

/**
 * TestThread
 * @description
 * @author SongJian
 * @date 2023/2/12 14:56
 * @version
 */
@SpringBootTest
public class TestThread {

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Test
    private void test() throws InterruptedException {
        // 获取线程池中当前线程数
        int poolSize = threadPoolTaskExecutor.getThreadPoolExecutor().getPoolSize();
        System.out.println("当前线程数量" + poolSize);

        // 当前压入队列的任务数
        int size = threadPoolTaskExecutor.getThreadPoolExecutor().getQueue().size();
        System.out.println("当前压入等待队列的线程数量" + size);

        // 获取当前的活跃线程数（正在处理任务线程）
        int activeCount = threadPoolTaskExecutor.getThreadPoolExecutor().getActiveCount();
        System.out.println("当前活跃线程数" + activeCount);

        // 获取当前完成的任务数
        long completedTaskCount = threadPoolTaskExecutor.getThreadPoolExecutor().getCompletedTaskCount();
        System.out.println("完成任务数" + completedTaskCount);

        // 获取总任务数
        long taskCount = threadPoolTaskExecutor.getThreadPoolExecutor().getTaskCount();
        System.out.println("总任务数" + taskCount);

        // 先并发 3 个任务
        for (int i = 0; i < 3; ++i) {
            threadPoolTaskExecutor.execute(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // 执行完毕后，核心线程数达到 3
        TimeUnit.SECONDS.sleep(2);

        for (int i = 0; i < 2; ++i) {
            threadPoolTaskExecutor.execute(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // 此时核心线程数是 5
        while(true) {
            // 获取线程池中当前线程数
            int poolSize2 = threadPoolTaskExecutor.getThreadPoolExecutor().getPoolSize();
            System.out.println("当前线程数量" + poolSize2);

            // 当前压入队列的任务数
            int size2 = threadPoolTaskExecutor.getThreadPoolExecutor().getQueue().size();
            System.out.println("当前压入等待队列的线程数量" + size2);

            // 获取当前的活跃线程数（正在处理任务线程）
            int activeCount2 = threadPoolTaskExecutor.getThreadPoolExecutor().getActiveCount();
            System.out.println("当前活跃线程数" + activeCount2);

            // 获取当前完成的任务数
            long completedTaskCount2 = threadPoolTaskExecutor.getThreadPoolExecutor().getCompletedTaskCount();
            System.out.println("完成任务数" + completedTaskCount2);

            // 获取总任务数
            long taskCount2 = threadPoolTaskExecutor.getThreadPoolExecutor().getTaskCount();
            System.out.println("总任务数" + taskCount2);
        }
    }
}

