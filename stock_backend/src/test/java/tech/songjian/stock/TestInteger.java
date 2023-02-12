/**
 * @projectName stock_parent
 * @package tech.songjian.stock
 * @className tech.songjian.stock.TestInteger
 */
package tech.songjian.stock;

import org.junit.jupiter.api.Test;

/**
 * TestInteger
 * @description
 * @author SongJian
 * @date 2023/2/11 19:39
 * @version
 */
public class TestInteger {

    @Test
    public void test1() {
        Integer i1 = 100;
        Integer i2 = 100;
        System.out.println(i1 == i2);   // true

        Integer i3 = new Integer(100);
        Integer i4 = new Integer(100);
        System.out.println(i3 == i4);   // false

    }
    @Test
    public void test2() {
        // 使用 常量 直接赋值
        String a1 = "123";
        String a2 = "123";
        System.out.println(a1 == a2);

        // 使用 new 创建字符串
        String a3 = new String("123");
        String a4 = new String("123");
        System.out.println(a3 == a4);
    }
}

