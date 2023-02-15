# 股票后端功能开发

## 一、登入功能开发

### 需求分析

#### 1、页面原型

![image-20230202220343729](https://p.ipic.vip/6obymr.png)

#### 2、相关表结构

![image-20230202220643022](https://p.ipic.vip/379qay.png)

#### 3、访问接口定义

```
请求接口：/api/login
请求方式：POST
请求数据示例：
	 {
       username:'zhangsan',//用户名
       password:'666',//密码
       code:'1234' //校验码
    }
响应数据：
    {
        "code": 1,  //成功1 失败0
        "data": {
        "id":"1237365636208922624",
        "username":"zhangsan",
        "nickName":"xiaozhang",
        "phone":"1886702304"
        }
    }
```

#### 4、封装请求vo和响应vo

请求vo封装：

```java
@Data
public class LoginReqVo {
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 验证码
     */
    private String code;
}
```

响应vo封装：

```java
package tech.songjian.stock.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LoginRespVo
 * @description 用户登入响应vo
 * @author SongJian
 * @date 2023/2/2 22:21
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRespVo {
    /**
     * 用户ID
     */
    private String id;
    /**
     * 电话
     */
    private String phone;
    /**
     * 用户名
     */
    private String username;
    /**
     * 昵称
     */
    private String nickName;
}
```

### 功能实现

#### 1、导入依赖包

```xml
<!--apache工具包-->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>
<!--密码加密和校验工具包-->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
<!--工具包-->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
</dependency>
```

#### 2、配置密码加密服务

```java
/**
 * CommonConfig
 * @description 定义公共配置类
 * @author SongJian
 * @date 2023/2/2 22:30
 * @version
 */
@Configuration
public class CommonConfig {

    /**
     * 定义密码加密和解密器
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

#### 3、controller接口

```java
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录功能实现
     * @param vo
     * @return
     */
    @PostMapping("/login")
    public R<LoginRespVo> login(@RequestBody LoginReqVo vo){
        R<LoginRespVo> r= this.userService.login(vo);
        return r;
    }
}
```

#### 4、Service层接口以及实现类

```java
public interface UserService {
    /**
     * 用户登录功能实现
     * @param vo
     * @return
     */
    R<LoginRespVo> login(LoginReqVo vo);
}
```

实现类：

```java
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public R<LoginRespVo> login(LoginReqVo vo) {
        // 1、判断 vo 是否存在 或者 用户名是否存在 或者 密码是否存在
        if (vo == null || Strings.isNullOrEmpty(vo.getUsername()) || Strings.isNullOrEmpty(vo.getPassword())) {
            return R.error(ResponseCode.DATA_ERROR.getMessage());
        }
        // 2、根据用户名查询用户是否存在
        SysUser userInfo = userMapper.findUserInfoByUserName(vo.getUsername());
        if (userInfo == null) {
            return R.error(ResponseCode.DATA_ERROR.getMessage());
        }
        // 3、判断密码是否一致
        // 这个是 Spirng-security 中的
        if (!passwordEncoder.matches(userInfo.getPassword(), vo.getPassword())) {
            return R.error(ResponseCode.SYSTEM_PASSWORD_ERROR.getMessage());
        }
        // 4、封装响应vo，属性复制
        // BeanUtils 属性复制：需要保证两个类属性名称一致
        LoginRespVo loginRespVo = new LoginRespVo();
        BeanUtils.copyProperties(userInfo, loginRespVo);
        return R.ok(loginRespVo);
    }
}
```

#### 5、Mapper方法

在SysUserMapper下定义接口方法：

~~~java
    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    SysUser findByUserName(@Param("username") String username);
~~~

绑定xml：

~~~xml
<select id="findUserInfoByUserName" resultMap="BaseResultMap">
        select
        <include refid="Base_Column_List" />
        from sys_user
        where  username=#{username}
</select>
~~~

#### 6、测试

![image-20230207105758580](https://p.ipic.vip/e2pspt.png)

## 二、验证码登入功能开发

### 需求分析

#### 单体架构与前后端分离架构区别

在单体架构中，不存在跨域问题。

<img src="https://p.ipic.vip/8cklc9.png" alt="image-20230207110454254" style="zoom:50%;" />

在前后端分离架构中，前后端的请求存在跨域问题，导致请求无法携带和服务器对应的 cookie，导致 session 失效。

我们可以借助 redis 来模拟 session 机制（**相当于在集群中做了一个公共的缓存区域**），实现验证码的生成和校验功能。

<img src="https://p.ipic.vip/8jof8n.png" alt="image-20230207110803300" style="zoom: 50%;" />

> Q：存储 redis 中验证码的 key 又是什么呢？
>
> A：模拟 sessionId，我们可以借助工具类生成 **全局唯一ID**。

#### 验证码生成接口说明

```
请求路径：/api/captcha
请求参数：无
响应数据格式：
    {
        "code": 1,
        "data": {
            "code": "5411", //响应的验证码
            "rkey": "1479063316897845248" //保存在redis中验证码对应的key，模拟sessioinId
        }
    }
```

### 功能实现

#### 1、redis环境集成

```xml
<!--redis场景依赖-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<!-- redis创建连接池，默认不会创建连接池 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
<!--apache工具包，提供验证随机码工具类-->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>
```

#### 2、yml配置redis

```yml
spring:
  # 配置缓存
  redis:
    host: 192.168.188.130
    port: 6379
    database: 0 #Redis数据库索引（默认为0）
    lettuce:
      pool:
        max-active: 8 # 连接池最大连接数（使用负值表示没有限制）
        max-wait: -1ms # 连接池最大阻塞等待时间（使用负值表示没有限制）
        max-idle: 8 # 连接池中的最大空闲连接
        min-idle: 1  # 连接池中的最小空闲连接
    timeout: PT10S # 连接超时时间（毫秒）
```

#### 3、测试redis连接

```java
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
```

![image-20230207164340332](https://p.ipic.vip/9grwg8.png)

#### 4、设置全局唯一id生成器

在 utils 包下导入 id 生成器工具类，使用雪花算法生成。

加入了 `时间戳+机房ID+机器ID+序列号` 保证生成的 id 绝对唯一，并且加锁保证绝对的安全。

![image-20230207164613482](https://p.ipic.vip/ncd69r.png)

#### 5、公共配置类中添加bean配置

以便直接从 IOC 容器中获取 bean

```java
package tech.songjian.stock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tech.songjian.stock.utils.IdWorker;

/**
 * @author by itheima
 * @Date 2022/3/13
 * @Description 定义公共配置类
 */
@Configuration
public class CommonConfig {

    /**
     * 定义密码加密和解密器 bean
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * id 生成器 bean
     * @return
     */
    @Bean
    public IdWorker idWorker() {
        return new IdWorker();
    }
}
```

#### 6、定义web接口

在 `UserController` 中定义 web 访问接口

```java
/**
 * 生成验证码
 *  map结构：
 *      code： xxx,
 *      rkey: xxx
 * @return
 */
@GetMapping("/captcha")
public R<Map> genCapchaCode() {
	return userService.genCapchaCode();
}
```

#### 7、Service服务接口与实现

在 `UserService` 中定义服务接口：

```java
		/**
     * 生成验证码
     * @return
     */
    R<Map> genCapchaCode();
```

在实现类中完成代码逻辑：

```java
		@Override
    public R<Map> genCapchaCode() {
        // 1、生成随机校验码，长度为 4
        String checkCode = RandomStringUtils.randomNumeric(4);
        // 2、生成类似与 sessionID 的 id 作为 key，保存在 redis，设置有效期 60s
        long rkey = idWorker.nextId();
        // 建议：往 redis 中保存的数据以 String 格式为主！
        String sessionId = String.valueOf(rkey);
        redisTemplate.opsForValue().set(sessionId, checkCode, 60, TimeUnit.SECONDS);
        // 3、组装响应的 map 对象
        Map<String, String> map = new HashMap<>();
        map.put("code", checkCode);
        map.put("rkey", sessionId);
        // 4、返回数据
        return R.ok(map);
    }
```

#### 8、测试

![image-20230207170953192](https://p.ipic.vip/1y64n4.png)

#### 9、在`LoginReqVo`中添加rkey属性

```java
		/**
     * 保存redis随机码的key
     */
    private String rkey;
```

#### 10、完善登入验证逻辑

在 `UserServiceImpl` 中：

```java
package tech.songjian.stock.service.impl.impl;

import com.google.common.base.Strings;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.songjian.stock.mapper.SysUserMapper;
import tech.songjian.stock.pojo.SysUser;
import tech.songjian.stock.service.impl.UserService;
import tech.songjian.stock.utils.IdWorker;
import tech.songjian.stock.vo.req.LoginReqVo;
import tech.songjian.stock.vo.resp.LoginRespVo;
import tech.songjian.stock.vo.resp.R;
import tech.songjian.stock.vo.resp.ResponseCode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author by itheima
 * @Date 2022/3/13
 * @Description 用户服务实现
 */
@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public R<LoginRespVo> login(LoginReqVo vo) {
        // 1.判断vo是否存在 或者 用户名是否存在 或者 密码是否存在 或者 验证码是否存在 或者 rkey 是否存在
        if (vo == null || Strings.isNullOrEmpty(vo.getUsername()) || Strings.isNullOrEmpty(vo.getPassword())
            || Strings.isNullOrEmpty(vo.getCode()) || Strings.isNullOrEmpty(vo.getRkey())) {
            return R.error(ResponseCode.DATA_ERROR.getMessage());
        }

        // 1.1 获取校验验证码
        String redisCode = (String) redisTemplate.opsForValue().get(vo.getRkey());
        // 1.2 校验
        if (redisCode == null || !redisCode.equals(vo.getCode())) {
            return R.error(ResponseCode.DATA_ERROR.getMessage());
        }
        // 1.3 redis删除key
        redisTemplate.delete(vo.getRkey());

        // 2.根据用户名用户是否存在
        SysUser userInfo= sysUserMapper.findUserInfoByUserName(vo.getUsername());
        if (userInfo==null) {
            return R.error(ResponseCode.DATA_ERROR.getMessage());
        }

        // 3.判断密码,不匹配
        if (!passwordEncoder.matches(vo.getPassword(),userInfo.getPassword())) {
            return R.error(ResponseCode.SYSTEM_PASSWORD_ERROR.getMessage());
        }

        // 4.属性赋值 两个类之间属性名称一致
        LoginRespVo respVo = new LoginRespVo();
        BeanUtils.copyProperties(userInfo,respVo);

        return R.ok(respVo);
    }

    @Override
    public R<Map> genCapchaCode() {
        // 1、生成随机校验码，长度为 4
        String checkCode = RandomStringUtils.randomNumeric(4);
        // 2、生成类似与 sessionID 的 id 作为 key，保存在 redis，设置有效期 60s
        long rkey = idWorker.nextId();
        // 建议：往 redis 中保存的数据以 String 格式为主！
        String sessionId = String.valueOf(rkey);
        redisTemplate.opsForValue().set(sessionId, checkCode, 60, TimeUnit.SECONDS);
        // 3、组装响应的 map 对象
        Map<String, String> map = new HashMap<>();
        map.put("code", checkCode);
        map.put("rkey", sessionId);
        // 4、返回数据
        return R.ok(map);
    }
}
```

## 三、国内大盘指数功能开发

### 业务分析

#### 页面原型

大盘展示需要的字段：

<img src="https://p.ipic.vip/fn5zge.png" alt="image-20230207173329050" style="zoom: 67%;" />

国内大盘数据包含：

~~~tex
大盘代码、名称、前收盘价、开盘价、最新价、涨幅、总金额、总手、当前日期
~~~

#### 表结构分析

大盘指数包含国内和国外的大盘数据，我们先完成国内大盘数据的展示功能。

股票大盘数据详情表（`stock_market_index_info`）设计如下：

<img src="https://p.ipic.vip/6m42ge.png" alt="image-20230207173516674" style="zoom:67%;" />

相关的开盘与收盘流水表（`stock_market_log_price`）设计如下：

<img src="https://p.ipic.vip/t8op4i.png" alt="image-20230207173554107"  />

开盘和收盘流水表与股票大盘数据详情表通过 `market_code` 进行业务关联，同时一个大盘每天只产生一条开盘与收盘流水数据，该数据，后期通过定时任务统计获取。

要获取上述数据，需要大盘表和价格流水表的联合查询。

> 注意：
>
> 1. 如果当前时间没有最新的大盘数据，则 **显示最近有效的大盘数据信息**
>      比如：今天是周1上午九点，则显示上周五收盘时的大盘数据信息。
> 2. 当前大盘的数据采集频率为 **一分钟一次**
> 3. 将相关功能封装工具类 `DateTimeUtil`

#### 接口说明

```
功能说明：获取最新国内A股大盘信息
				如果不在股票交易时间内，则显示最近时间点的交易信息
请求路径：/api/quot/index/all
请求方式：GET
参数：无
```

响应数据格式：

~~~json
{
    "code": 1,
    "data": [
        {
            "tradeAmt": 235158296,//交易量
            "preClosePrice": 78.9,//前收盘价格
            "code": "s_sz399001",//大盘编码
            "name": "深证成指",//大盘名称
            "curDate": "202112261056",// 当前日期
            "openPrice": 79.2,//开盘价
            "tradeVol": 32434490,//交易金额
            "upDown": -0.89,//涨幅
            "tradePrice": -131.52//当前价格
        },
        {
            "tradeAmt": 1627113,
            "code": "s_sh000001",
            "name": "上证指数",
            "curDate": "202112261056",
            "tradeVol": 21549808,
            "upDown": -0.56,
            "tradePrice": -20.26
        }
    ]
}
~~~

### 功能实现

#### 1、DateTimeUtil实现

相关功能包括：

- 获取指定日期内股票的上一个有效交易日时间
- 判断是否是工作日
- 获取上一天日期
- 日期转String
- 获取股票日期格式字符串
- 获取指定日期的收盘日期
- 获取指定日期的开盘日期
- 获取最近的股票有效时间，精确到分钟
- 判断当前时间是否在大盘的中午休盘时间段

```java
package tech.songjian.stock.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * @author by itheima
 * @Date 2021/12/31
 * @Description 日期时间工具类
 */
public class DateTimeUtil {
    /**
     * 获取指定日期下股票的上一个有效交易日时间
     * @return
     */
    public static DateTime getPreviousTradingDay(DateTime dateTime){
        //获取指定日期对应的工作日
        int weekNum = dateTime.dayOfWeek().get();
        //判断所属工作日
        DateTime preDateTime=null;
        //周一，那么T-1就是周五
        if (weekNum==1){
            //日期后退3天
          preDateTime=dateTime.minusDays(3);
        }
        //周末，那么T-1就是周五
        else if (weekNum==7){
            preDateTime=dateTime.minusDays(2);
        }
        else {
            preDateTime=dateTime.minusDays(1);
        }
        return getDateTimeWithoutSecond(preDateTime);
    }


    /**
     * 判断是否是工作日
     * @return true：在工作日 false:不在工作日
     */
    public static boolean isWorkDay(DateTime dateTime){
        //获取工作日
        int weekNum = dateTime.dayOfWeek().get();
        return  weekNum>=1 && weekNum<=5;
    }

    /**
     * 获取上一天日期
     * @param dateTime
     * @return
     */
    public static DateTime getPreDateTime(DateTime dateTime){
        return dateTime.minusDays(1);
    }

    /**
     * 日期转String
     * @param dateTime 日期
     * @param pattern 日期正则格式
     * @return
     */
    public static String parseToString(DateTime dateTime,String pattern){
       return  dateTime.toString(DateTimeFormat.forPattern(pattern));
    }

    /**
     * 获取股票日期格式字符串
     * @param dateTime
     * @return
     */
    public static String parseToString4Stock(DateTime dateTime){
        return parseToString(dateTime,"yyyyMMddHHmmss");
    }


    /**
     * 获取指定日期的收盘日期
     * @param dateTime
     * @return
     */
    public static DateTime getCloseDate(DateTime dateTime){
       return dateTime.withHourOfDay(14).withMinuteOfHour(58).withSecondOfMinute(0).withMillisOfSecond(0);
    }

    /**
     * 获取指定日期的开盘日期
     * @param dateTime
     * @return
     */
    public static DateTime getOpenDate(DateTime dateTime){
       return dateTime.withHourOfDay(9).withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
    }

    /**
     * 获取最近的股票有效时间，精确到分钟
     * @param target
     * @return
     */
    public static String getLastDateString4Stock(DateTime target){
        DateTime dateTime = getLastDate4Stock(target);
        dateTime=getDateTimeWithoutSecond(dateTime);
        return parseToString4Stock(dateTime);
    }
    /**
     * 获取最近的股票有效时间，精确到分钟
     * @param target
     * @return
     */
    public static DateTime getLastDate4Stock(DateTime target){
        //判断是否是工作日
        if (isWorkDay(target)) {
            //当前日期开盘前
            if (target.isBefore(getOpenDate(target))) {
                target=getCloseDate(getPreviousTradingDay(target));
            }else if (isMarketOffTime(target)){
                target=target.withHourOfDay(11).withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
            }else if (target.isAfter(getCloseDate(target))){
                //当前日期收盘后
                target=getCloseDate(target);
            }
        }else{
            //非工作日
            target=getCloseDate(getPreviousTradingDay(target));
        }
        target = getDateTimeWithoutSecond(target);
        return target;
    }

    /**
     * 判断当前时间是否在大盘的中午休盘时间段
     * @return
     */
    public static boolean isMarketOffTime(DateTime target){
        //上午休盘开始时间
        DateTime start = target.withHourOfDay(11).withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
        //下午开盘时间
        DateTime end = target.withHourOfDay(13).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        if (target.isAfter(start) && target.isBefore(end)) {
            return true;
        }
        return false;
    }

    /**
     * 将秒时归零
     * @param dateTime 指定日期
     * @return
     */
    public static DateTime getDateTimeWithoutSecond(DateTime dateTime){
        DateTime newDate = dateTime.withSecondOfMinute(0).withMillisOfSecond(0).withMillisOfSecond(0);
        return newDate;
    }


    /**
     * 将秒时归零
     * @param dateTime 指定日期字符串，格式必须是：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static DateTime getDateTimeWithoutSecond(String dateTime){
        DateTime parse = DateTime.parse(dateTime, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        return getDateTimeWithoutSecond(parse);
    }
}
```

#### 2、domain封装

> 【NOTE】数据层对象：
>
> - POJO对象（概念比较大）
>
>   POJO = plain ordinary JAVA object = 普通 java 对象，一般只有 **属性 + get/set 方法**
>
> - PO对象
>
>   Presistent object 持久对象，通常是与数据库中的表相映射的对象
>
> - VO对象
>
>   Value Object 业务层之间数据传递的对象
>
> - Domain对象
>
>   领域对象，通常是多个表中的数据进行整合，放到一个对象内就使用 domain 对象
>
> - Entity对象
>
>   实体对象，严格与数据库中的表相对应。因此在操作表时，直接操作这个类即可。
>
> - DTO对象
>
>   DTO = data transfer object = 数据传输对象

在业务分析中，完成此业务需要整合多表的信息，因此使用 domain 对象。

```java
/**
 * @author by itheima
 * @Date 2022/1/9
 * @Description 定义封装多内大盘数据的实体类
 */
@Data
public class InnerMarketDomain {
    /*
      jdbc:bigint--->java:long
     */
    private Long tradeAmt;
    /*
        jdbc:decimal --->java:BigDecimal
     */
    private BigDecimal preClosePrice;
    private String code;
    private String name;
    private String curDate;
    private BigDecimal openPrice;
    private Long tradeVol;
    private BigDecimal upDown;
    private BigDecimal tradePrice;
}
```

#### 3、常量数据封装

在 yml 配置文件中：

```yml
# 配置股票相关的参数
stock:
  inner: # A股
    - s_sh000001 # 上证ID
    - s_sz399001 #  深证ID
  outer: # 外盘
    - int_dji # 道琼斯
    - int_nasdaq # 纳斯达克
    - int_hangseng # 恒生
    - int_nikkei # 日经指数
    - b_TWSE # 台湾加权
    - b_FSSTI # 新加坡
```

为了方便获取，我们创建了一个实体类对象：

这里注意，需要在运行类中添加注解 ` @EnableConfigurationProperties(StockInfoConfig.class)`

```java
@ConfigurationProperties(prefix = "stock")
@Data
public class StockInfoConfig {
    //a股大盘ID集合
    private List<String> inner;
    //外盘ID集合
    private List<String> outer;
}
```

#### 4、写sql

从设计角度看，大盘价格流水表 和 大盘实时流水表 没有必然的联系，对于价格流水表仅仅记录当天的开盘价和前一个交易日的收盘价，也就是一个交易日仅产生一条数据，而大盘的实时流水则会产生 N 条数据。

所以我们采取 **先查询大盘实时流水主表信息(将数据压扁)，然后再关联价格日流水表进行查询。**

本业务在两张独立的表上进行 sql 查询：

1. 先查询主表信息
2. 再关联日统计表进行查询

```sql
# 获取最新的国内大盘数据信息
# 上证、深证，2021-12-28 09:31:00

## 1、先查询主表信息
select 
	smi.trade_account as tradeAmt,
	smi.market_Id as code,
	smi.market_name as name,
	date_format(smi.cur_time, '%Y%m%d%H%i') as curDate,
	smi.trade_volume as tradeVolm,
	smi.updown_rate as upDown,
	smi.current_price as tradePrice
from stock_market_index_info as smi 
where smi.market_Id in ('s_sh000001', 's_sz399001') 
and smi.cur_time='2021-12-28 09:31:00';

## 2、关联日统计表进行查询
select * from () as tmp
left join stock_market_log_price as sml
where tmp.code=sml.market_code 
and date_format(sml.cur_date, '%Y%m%d')=date_format('2021-12-28 09:31:00', '%Y%m%d')

# 汇总
select 
	tmp.*,
	sml.open_price as openPrice,
	sml.pre_close_price as perClosePrice
from (select 
	smi.trade_account as tradeAmt,
	smi.market_Id as code,
	smi.market_name as name,
	date_format(smi.cur_time, '%Y%m%d%H%i') as curDate,
	smi.trade_volume as tradeVolm,
	smi.updown_rate as upDown,
	smi.current_price as tradePrice
from stock_market_index_info as smi 
where smi.market_Id in ('s_sh000001', 's_sz399001') 
and smi.cur_time='2021-12-28 09:31:00';) as tmp
left join stock_market_log_price as sml
on tmp.code=sml.market_code 
and date_format(sml.cur_date, '%Y%m%d')=date_format('2021-12-28 09:31:00', '%Y%m%d')
```

#### 5、编写web服务接口

```java
@RestController
@RequestMapping("/api/quot")
public class StockController {

    @Autowired
    private StockService stockService;

	//其它省略.....
    /**
     * 获取国内最新大盘指数
     * @return
     */
    @GetMapping("/index/all")
    public R<List<InnerMarketDomain>> innerIndexAll(){
        return stockService.innerIndexAll();
    }
}
```

#### 6、编写service接口和实现类

```java
public interface StockService {
    /**
     * 获取国内大盘的实时数据
     * @return
     */
    R<List<InnerMarketDomain>> innerIndexAll();
}
```

实现类：

```java
@Service("stockService")
public class StockServiceImpl implements StockService {

    @Autowired
    private StockMarketIndexInfoMapper stockMarketIndexInfoMapper;

    @Autowired
    private StockInfoConfig stockInfoConfig;

    /**
     * 获取最新的 A 股大盘信息
     * 如果当前不在股票交易日或交易时间，则显示最近最新的交易数据信息
     * @return
     */
    @Override
    public R<List<InnerMarketDomain>> getNewAMarketInfo() {
        // 1、获取国内 A 股大盘 id 集合
        List<String> inners = stockInfoConfig.getInner();

        // 2、获取最近股票交易日期
        DateTime lastDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());

        // 3、转java中的Date
        Date date = lastDateTime.toDate();
        //TODO mock测试数据，后期数据通过第三方接口动态获取试试数据
        date = DateTime.parse("2022-01-03 11:15:00").toDate();
        // 4、将获取的 java Date 传入接口
        List<InnerMarketDomain> list = stockMarketIndexInfoMapper.getMarketInfo(inners, date);

        // 5、返回查询结果
        return R.ok(list);
    }
}
```

#### 7、定义mapper接口方法和xml

mapper接口：

```java
		/**
     * 根据注定的id集合和日期查询大盘数据
     * @param ids 大盘id集合
     * @param lastDate 对应日期
     * @return
     */
List<InnerMarketDomain> getMarketInfo(@Param("marketIds") List<String> marketIds, @Param("timePoint") Date timePoint);
```

Xml：

```xml
<select id="getMarketInfo" resultType="tech.songjian.stock.common.domain.InnerMarketDomain">
        SELECT
        tmp.mark_Id AS code,
        tmp.mark_name AS name,
        sml.pre_close_price AS preClosePrice,
        sml.open_price AS openPrice,
        tmp.current_price AS tradePrice,
        tmp.updown_rate AS upDown,
        tmp.trade_account AS tradeAmt,
        tmp.trade_volume AS tradeVol,
        DATE_FORMAT( tmp.cur_time, '%Y%m%d') AS curDate
        FROM
        (
        SELECT	* 	FROM	stock_market_index_info AS smi
        WHERE smi.cur_time =#{timePoint}
        AND smi.mark_Id IN
        <foreach collection="marketIds" item="id" open="(" close=")" separator=",">
            #{id}
        </foreach>
        ) AS tmp
        LEFT JOIN stock_market_log_price AS sml ON tmp.mark_Id=sml.market_code
        AND DATE_FORMAT( sml.cur_date, '%Y%m%d' )= DATE_FORMAT(#{timePoint},'%Y%m%d' )
    </select>
```

#### 8、测试

postman:http://127.0.0.1:8080/api/quot/index/all

<img src="https://p.ipic.vip/dv24id.png" alt="image-20230209210518796" style="zoom: 50%;" />

## 四、板块指数功能开发

### 业务分析

#### 产品原型

<img src="https://p.ipic.vip/d46g7w.png" alt="image-20230209213223911" style="zoom:50%;" />

#### 板块数据表分析

`stock_block_rt_info` 板块表涵盖了业务所需的所有字段数据。

![image-20230209213404330](https://p.ipic.vip/321agj.png)

#### 国内板块接口说明

~~~tex
需求说明: 沪深两市板块分时行情数据查询，以交易时间和交易总金额降序查询，取前10条数据
请求URL: /api/quot/sector/all
请求方式: GET
请求参数: 无
~~~

接口响应数据格式：

~~~json
{
    "code": 1,
    "data": [
        {
            "companyNum": 247,//公司数量
            "tradeAmt": 5065110316,//交易量
            "code": "new_dzxx",//板块编码
            "avgPrice": 14.571,//平均价格
            "name": "电子信息",//板块名称
            "curDate": "20211230",//当前日期
            "tradeVol": 60511659145,//交易总金额
            "updownRate": 0.196//涨幅
        },
        {
            "companyNum": 155,
            "tradeAmt": 4281655990,
            "code": "new_swzz",
            "avgPrice": 22.346,
            "name": "生物制药",
            "curDate": "20211230",
            "tradeVol": 52026876373,
            "updownRate": -0.068
        }
    ]
}
~~~

#### DO封装

```java
/**
 * 股票板块详情信息表
 * @TableName stock_block_rt_info
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockBlockRtInfo implements Serializable {
    /**
     * 板块主键ID（业务无关）
     */
    private String id;

    /**
     * 表示，如：new_blhy-玻璃行业
     */
    private String label;

    /**
     * 板块名称
     */
    private String blockName;

    /**
     * 公司数量
     */
    private Integer companyNum;

    /**
     * 平均价格
     */
    private BigDecimal avgPrice;

    /**
     * 涨跌幅
     */
    private BigDecimal updownRate;

    /**
     * 交易量
     */
    private Long tradeAmount;

    /**
     * 交易金额
     */
    private BigDecimal tradeVolume;

    /**
     * 当前日期（精确到秒）
     */
    private Date curTime;

    private static final long serialVersionUID = 1L;
}
```

### 功能实现

#### 1、编写web访问接口

```java
		/**
     * 查询板块信息
     * 沪深两市板块分时行情数据查询，以交易时间和交易总金额降序查询，取前10条数据
     * @return
     */
    @GetMapping("/sector/all")
    public R<List<StockBlockRtInfo>> sectorAll() {
        return stockService.sectorAllLimit();
    }
```

#### 2、编写service服务层接口以及实现类

```java
		@Autowired
    private StockBlockRtInfoMapper stockBlockRtInfoMapper;

		/**
     * 查询板块信息
     * 沪深两市板块分时行情数据查询，以交易时间和交易总金额降序查询，取前10条数据
     * @return
     */
    @Override
    public R<List<StockBlockRtInfo>> sectorAllLimit() {
        //1.调用 mapper 接口获取数据 TODO 优化 避免全表查询 根据时间范围查询，提高查询效率
        List<StockBlockRtInfo> infos = stockBlockRtInfoMapper.sectorAllLimit();
        //2.组装数据
        if (CollectionUtils.isEmpty(infos)) {
            return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
        }
        return R.ok(infos);
    }
```

#### 3、编写mapper接口以及xml文件

```java
		/**
     * 沪深两市板块分时行情数据查询，以交易时间和交易总金额降序查询，取前10条数据
     * @return
     */
    List<StockBlockRtInfo> sectorAllLimit();
```

xml：

```xml
<select id="sectorAllLimit" resultType="tech.songjian.stock.pojo.StockBlockRtInfo">
        select
            sbr.company_num  as companyNum,
            sbr.trade_amount as tradeAmount,
            sbr.label        as label,
            sbr.avg_price    as avgPrice,
            sbr.block_name   as blockName,
            date_format(sbr.cur_time,'%Y%m%d') as curTime,
            sbr.trade_volume as tradeVolume,
            sbr.updown_rate  as updownRate
        from stock_block_rt_info as sbr
        order by sbr.cur_time desc,sbr.trade_volume desc
            limit 10
    </select>
```

#### 4、web接口测试

postman：http://127.0.0.1:8080/api/quot/sector/all

<img src="https://p.ipic.vip/31y59p.png" alt="image-20230209221219646" style="zoom:50%;" />

前端显示：

![image-20230209221401944](https://p.ipic.vip/ui1bg3.png)

## 五、涨幅榜功能开发

### 需求分析

#### 1、业务原型

![image-20230210120611441](https://p.ipic.vip/ehyfit.png)

#### 2、股票表结构分析

表名：`stock_rt_info`

![image-20230210110120213](https://p.ipic.vip/w44l9a.png)

#### 3、涨幅榜功能分析

```tex
功能描述：统计沪深两个城市的最新交易数据，并按涨幅降序排序，查询前十条数据
服务路径：/api/quot/stock/increase
服务方法：GET
请求频率：每分钟
请求参数：无
```

响应数据格式：

```json
{
    "code": 1,
    "data": [
        {
            "tradeAmt": 10573382,
            "preClosePrice": 8.97,
            "amplitude": 0.024526,
            "code": "600011",
            "name": "华能国际",
            "curDate": "20211230",
            "tradeVol": 95788391.000,
            "upDown": 0.13,
            "increase": 0.014493,
            "tradePrice": 9.10
        },
        {
            "tradeAmt": 2449263,
            "preClosePrice": 19.53,
            "amplitude": 0.009729,
            "code": "000002",
            "name": "万 科Ａ",
            "curDate": "20211230",
            "tradeVol": 48022049.980,
            "upDown": 0.09,
            "increase": 0.004608,
            "tradePrice": 19.62
        },
      	....
}
```

#### 4、DO封装

```java
/**
 * StockUpdownDomain
 * @description 股票涨跌信息
 * @author SongJian
 * @date 2023/2/10 11:11
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockUpdownDomain {
    /**
     * 交易量
     */
    private Long tradeAmt;
    /**
     * 前收盘价
     */
    private BigDecimal preClosePrice;
    /**
     * 振幅
     */
    private BigDecimal amplitude;
    /**
     * 股票编码
     */
    private String code;
    /**
     * 股票名称
     */
    private String name;
    /**
     * 日期
     */
    private String curDate;
    /**
     * 交易金额
     */
    private BigDecimal tradeVol;
    /**
     * 涨跌
     */
    private BigDecimal increase;
  	/**
     * 涨跌幅
     */
    private BigDecimal upDown;
    /**
     * 当前价格
     */
    private BigDecimal tradePrice;
}
```

### 功能实现

#### 1、定义访问接口

在 `StockController` 中

```java
		/**
     * 统计沪深两个城市的最新交易数据
     * 并按涨幅降序排序，查询前十条数据
     * @return
     */
    @GetMapping("/stock/increase")
    public R<List<StockUpdownDomain>> getStockRtInfoLimit() {
        return stockService.getStockRtInfoLimit();
    }
```

#### 2、定义服务接口方法与实现

在 `StockService` 中

```java
		/**
     * 统计沪深两个城市的最新交易数据
     * 并按涨幅降序排序，查询前十条数据
     * @return
     */
    R<List<StockUpdownDomain>> getStockRtInfoLimit();
```

在 `StockServiceImpl` 中

```java
		/**
     * 统计沪深两个城市的最新交易数据
     * 并按涨幅降序排序，查询前十条数据
     * @return
     */
		
		@Autowired
    private StockRtInfoMapper stockRtInfoMapper;

    @Override
    public R<List<StockUpdownDomain>> getStockRtInfoLimit() {
        // 1、获取最近最新股票有效交易时间点，精确到分钟
        Date lastDate = DateTimeUtil.getLastDate4Stock(DateTime.now()).toDate();
        // TODO mock 数据
        lastDate = DateTime.parse("2021-12-30 09:32:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 2、调用mapper进行查询
        List<StockUpdownDomain> list = stockRtInfoMapper.getStockRtInfoLimit(lastDate);
        if (CollectionUtils.isEmpty(list)) {
            return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
        }
        return R.ok(list);
    }
```

#### 3、定义mapper接口方法与xml

编写 sql 语句：

```sql
# 分析：统计国内 A 股股票的最新数据，根据涨跌幅进行排序，取前十
# 方式一：根据日期降序排序取
SELECT 
	sri.trade_amount as tradeAmt,
	sri.pre_close_price as preClosePrice,
	(sri.max_price - sri.min_price)/sri.pre_close_price as amplitude,
	sri.stock_code as code,
	sri.stock_name as name,
	DATE_FORMAT(sri.cur_time, "%Y%m%d") as curDate,
	sri.trade_volume as tradeVol,
	(sri.cur_price - sri.pre_close_price) as increase,
	(sri.cur_price - sri.pre_close_price)/sri.pre_close_price as upDown,
	sri.cur_price as tradePrice
FROM stock_rt_info as sri 
ORDER BY sri.cur_time DESC, upDown DESC
LIMIT 10;
# 方式一存在的问题：国内A股股票的最新数据等价于最近最新的股票交易时间点下的数据，
# 同时排序时是全表排序计算的，然后排序完毕以后，再筛选出前10条数据。
# ---------》 大表全表排序，取前十

# 优化：我们先根据股票时间点过滤，过滤出较小的结果集，在此结果集的基础上做排序和筛选
## 方式二
SELECT 
	sri.trade_amount as tradeAmt,
	sri.pre_close_price as preClosePrice,
	(sri.max_price - sri.min_price)/sri.pre_close_price as amplitude,
	sri.stock_code as code,
	sri.stock_name as name,
	DATE_FORMAT(sri.cur_time, "%Y%m%d") as curDate,
	sri.trade_volume as tradeVol,
	(sri.cur_price - sri.pre_close_price) as increase,
	(sri.cur_price - sri.pre_close_price)/sri.pre_close_price as upDown,
	sri.cur_price as tradePrice
FROM stock_rt_info as sri 
WHERE sri.cur_time = '2021-12-30 09:32:00'
ORDER BY sri.cur_time DESC, upDown DESC
LIMIT 10;
```

mapper 接口，在 `StockRtInfoMapper` ：

```java
		/**
     * 查询指定时间点下数据，根据涨幅取前十
     * @param timePoint 时间点，分钟级别
     * @return
     */
    List<StockUpdownDomain> getStockRtInfoLimit(@Param("timePoint") Date timePoint);
```

Xml:

```xml
<select id="getStockRtInfoLimit" resultType="tech.songjian.stock.common.domain.StockUpdownDomain">
        SELECT
            sri.trade_amount as tradeAmt,
            sri.pre_close_price as preClosePrice,
            (sri.max_price - sri.min_price)/sri.pre_close_price as amplitude,
            sri.stock_code as code,
            sri.stock_name as name,
            DATE_FORMAT(sri.cur_time, "%Y%m%d") as curDate,
            sri.trade_volume as tradeVol,
            (sri.cur_price - sri.pre_close_price) as upDown,
            (sri.cur_price - sri.pre_close_price)/sri.pre_close_price as increase,
            sri.cur_price as tradePrice
        FROM stock_rt_info as sri
        WHERE sri.cur_time = #{timePoint}
        ORDER BY sri.cur_time DESC, upDown DESC
            LIMIT 10
    </select>
```

#### 4、接口测试

<img src="https://p.ipic.vip/mk0p19.png" alt="image-20230210120600947"  />

![image-20230210120611441](https://p.ipic.vip/55fnhc.png)

## 六、涨幅榜更多功能开发

### 业务分析

#### 1、业务原型

![image-20230210143605377](https://p.ipic.vip/84zdna.png)

#### 2、涨幅榜分页功能分析

```json
功能描述：沪深两市个股行情列表查询，以时间顺序和涨幅分页查询
服务路径：/api/quot/stock/all
服务方法：GET
请求频率：每分钟
请求参数：
	{
			page:2 //当前页
			pageSize:20  //每页大小
	}
```

响应数据格式：

```json
{
    "code": 1,
    "data": {
        "totalRows": 148635,	// 总行数
        "totalPages": 14864,	//总页数
        "pageNum": 2,		// 当前页
        "pageSize": 10,		// 每页大小
        "size": 10,		// 当前页大小
        "rows": [	// 当前页数据信息
            {
                "tradeAmt": 12135246,
                "preClosePrice": 49.28,
                "amplitude": 0.030032,
                "code": "600009",
                "name": "\"上海机场",
                "curDate": "20220114",
                "tradeVol": 593417795.000,
                "upDown": -0.011161,
                "increase": -0.55,
                "tradePrice": 48.73
            },
            ...
          ]
     }
}
```

#### 3、pageResult封装

```java
/**
 * PageResult
 * @description 分页查询封装
 * @author SongJian
 * @date 2023/2/10 13:59
 * @version
 */
@Data
public class PageResult<T> implements Serializable {
    /**
     * 总记录数
     */
    private Long totalRows;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 当前第几页
     */
    private Integer pageNum;

    /**
     * 每页记录数
     */
    private Integer pageSize;

    /**
     * 当前页记录数
     */
    private Integer size;

    /**
     * 结果集
     */
    private List<T> rows;

    /**
     * 分页数据组装
     * @param pageInfo
     */
    public PageResult(PageInfo<T> pageInfo) {
        totalRows = pageInfo.getTotal();
        totalPages = pageInfo.getPages();
        pageNum = pageInfo.getPageNum();
        pageSize = pageInfo.getPageSize();
        size = pageInfo.getSize();
        rows = pageInfo.getList();
    }
}
```

### 功能实现

#### 1、分页插件PageHelper导入

```xml
				<dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper-spring-boot-starter</artifactId>
        </dependency>
```

在 yml 中配置 `PageHelper` 分页查询相关信息：

```yml
# pagehelper配置
pagehelper:
  helper-dialect: mysql #指定分页数据库类型（方言）
  reasonable: true #合理查询超过最大也，则查询最后一页
  support-methods-arguments: true # 支持通过Mapper接口参数来传递分页参数，默认false
  params: pacount=countSql # POJO或者Map中发现了countSql属性，就会作为count参数使用
  returnPageInfo: check # always总是返回PageInfo类型,check检查返回类型是否为PageInfo,none返回Page
```

#### 2、编写访问接口

```java
		/**
     * 查询沪深两市的全部涨幅榜数据
     * 按照时间顺序和涨幅分页查询
     * @param page 当前页
     * @param pageSize 当前页大小
     * @return
     */
    @GetMapping("/stock/all")
    public R<PageResult<StockUpdownDomain>> getStockRtInfo4Page(Integer page, Integer pageSize) {
        return stockService.getStockRtInfo4Page(page, pageSize);
    }
```

#### 3、定义服务接口方法及实现

```java
R<PageResult<StockUpdownDomain>> getStockRtInfo4Page(Integer page, Integer pageSize);
```

实现类：

```java
		@Override
    public R<PageResult<StockUpdownDomain>> getStockRtInfo4Page(Integer page, Integer pageSize) {
        // 1、设置分页参数
        PageHelper.startPage(page, pageSize);
        // 2、查询
        List<StockUpdownDomain> pages = stockRtInfoMapper.getStockRtInfo4All();
        if (CollectionUtils.isEmpty(pages)) {
            return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
        }
        // 3、组装 pageInfo 对象，他封装了一切的分页信息
        PageInfo<StockUpdownDomain> pageInfo = new PageInfo<>(pages);
        PageResult<StockUpdownDomain> pageResult = new PageResult<>(pageInfo);
        return R.ok(pageResult);
    }
```

#### 4、定义mapper接口以及xml

sql语句：

```sql
SELECT 
	sri.trade_amount as tradeAmt,
	sri.pre_close_price as preClosePrice,
	(sri.max_price - sri.min_price)/sri.pre_close_price as amplitude,
	sri.stock_code as code,
	sri.stock_name as name,
	DATE_FORMAT(sri.cur_time, "%Y%m%d") as curDate,
	sri.trade_volume as tradeVol,
	(sri.cur_price - sri.pre_close_price) as increase,
	(sri.cur_price - sri.pre_close_price)/sri.pre_close_price as upDown,
	sri.cur_price as tradePrice
FROM stock_rt_info as sri 
ORDER BY sri.cur_time DESC, upDown DESC
```

后续分页使用 pagehelper 动态追加（AOP思想）。

```java
List<StockUpdownDomain> getStockRtInfo4All();
```

XML：

```xml
		<select id="getStockRtInfo4All" resultType="tech.songjian.stock.common.domain.StockUpdownDomain">
        SELECT
            sri.trade_amount as tradeAmt,
            sri.pre_close_price as preClosePrice,
            (sri.max_price - sri.min_price)/sri.pre_close_price as amplitude,
            sri.stock_code as code,
            sri.stock_name as name,
            DATE_FORMAT(sri.cur_time, "%Y%m%d") as curDate,
            sri.trade_volume as tradeVol,
            (sri.cur_price - sri.pre_close_price) as increase,
            (sri.cur_price - sri.pre_close_price)/sri.pre_close_price as upDown,
            sri.cur_price as tradePrice
        FROM stock_rt_info as sri
        ORDER BY sri.cur_time DESC, upDown DESC
    </select>
```

#### 5、测试

![image-20230210143500394](https://p.ipic.vip/ryrcl1.png)

![image-20230210143557736](https://p.ipic.vip/3s9ayd.png)

## 七、涨跌停数据统计功能开发

### 业务分析

#### 1、业务原型

![image-20230210161330969](https://p.ipic.vip/t44q18.png)

#### 2、接口说明

```json
功能描述：统计沪深两市 T 日（当期股票交易日）每分钟的涨跌停数据
				注意：如果不在股票交易日内，那么就统计最近的股票交易日下的数据
服务路径：/api/quot/stock/updown/count
服务方法：GET
请求频率：每分钟
请求参数：无
```

响应数据格式：

```json
{
    "code": 1,
    "data": {
        "upList": [
            {
                "count": 2,
                "time": "202201071401"
            }
        ],
        "downList": [
            {
                "count": 1,
                "time": "202201070901"
            },
            {
                "count": 1,
                "time": "202201070901"
            },
						....
        ]
    }
}
```

### 功能开发

#### 1、sql编写

涨停数据查询Sql:

> 在内存中走二次过滤的关键字：`having`。

```sql
# 第一步：统计指定日期下每分钟股票的涨停数据功能
-- 分析：先计算涨幅数据，然后在选取大于等于 0.1 的数据过滤
SELECT 
	sri.cur_time,
	(sri.cur_price - sri.pre_close_price) / sri.pre_close_price AS increase
FROM
	stock_rt_info AS sri
WHERE
	DATE_FORMAT('2021-12-30 09:32:00', '%Y%m%d') = DATE_FORMAT(sri.cur_time, '%Y%m%d')
HAVING
	increase >= 0.1;
	
# 第二步：将步骤一的查询结果作为一张表，然后根据时间分组，统计数量集合
SELECT
	tmp.cur_time AS time,
	COUNT(*) AS count
FROM (
		SELECT 
		sri.cur_time,
		(sri.cur_price - sri.pre_close_price) / sri.pre_close_price AS increase
		FROM
			stock_rt_info AS sri
		WHERE
			DATE_FORMAT('2021-12-30 09:32:00', '%Y%m%d') = DATE_FORMAT(sri.cur_time, '%Y%m%d')
		HAVING
			increase >= 0.1
) AS tmp
GROUP BY tmp.cur_time
ORDER BY tmp.cur_time ASC;
```

但是这样不合理！因为：**查询中，我们使用函数（如 `date_format`）处理了索引字段，会导致索引失效，也就意味着当前查询不会走索引，而是走全表查询！**

因此，**查询中最好不要对索引字段使用函数处理！**

最终方案：将查询转换为范围查询，避免日期函数左右在索引字段下

```sql
SELECT
	tmp.cur_time AS time,
	COUNT(*) AS count
FROM (
		SELECT 
		sri.cur_time,
		(sri.cur_price - sri.pre_close_price) / sri.pre_close_price AS increase
		FROM
			stock_rt_info AS sri
		WHERE
			sri.cur_time BETWEEN '2022-01-07 09:30:00' AND '2022-01-07 15:00:00'
		HAVING
			increase >= 0.1
) AS tmp
GROUP BY tmp.cur_time
ORDER BY tmp.cur_time ASC;
```

![image-20230210153206449](https://p.ipic.vip/8fbasc.png)

仅在 600 多条数据下就有明显差异了。

类似，跌停统计：

```sql
SELECT
	tmp.cur_time AS time,
	COUNT(*) AS count
FROM (
		SELECT 
		sri.cur_time,
		(sri.cur_price - sri.pre_close_price) / sri.pre_close_price AS increase
		FROM
			stock_rt_info AS sri
		WHERE
			sri.cur_time BETWEEN '2022-01-07 09:30:00' AND '2022-01-07 15:00:00'
		HAVING
			increase <= -0.1
) AS tmp
GROUP BY tmp.cur_time
ORDER BY tmp.cur_time ASC;
```

#### 2、定义访问接口

```java
		/**
     * 统计T日（最近一次股票交易日）的涨停跌停的分时统计
     * @return
     */
    @GetMapping("/stock/updown/count")
    public R<Map> getStockUpdownCount() {
        return stockService.getStockUpdownCount();
    }
```

#### 3、定义服务接口及实现

```java
		/**
     * 统计T日（最近一次股票交易日）的涨停跌停的分时统计
     * @return
     */
    R<Map> getStockUpdownCount();
```

实现：

```java
		@Override
    public R<Map> getStockUpdownCount() {
        // 1、获取最近的股票交易日的开盘和收盘时间
        // 1.1 获取最近的交易时间
        DateTime avaliableTimePoint = DateTimeUtil.getLastDate4Stock(DateTime.now());
        // 1.2 根据有效的时间点，获取对应日期的开盘和收盘日期
        Date openTime = DateTimeUtil.getOpenDate(avaliableTimePoint).toDate();
        Date closeTime = DateTimeUtil.getCloseDate(avaliableTimePoint).toDate();
        // TODO mock数据
        openTime = DateTime.parse("2022-01-07 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        closeTime = DateTime.parse("2022-01-07 15:00:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();

        // 2、查询涨停的统计数据
        List<Map> upList = stockRtInfoMapper.getStockUpdownCount(openTime, closeTime, 1);
        // 3、查询跌停的统计数据
        List<Map> downList = stockRtInfoMapper.getStockUpdownCount(openTime, closeTime, 0);
        // 4、组装 map，将涨停数据和跌停数据组装
        HashMap<String, List> map = new HashMap<>();
        map.put("upList", upList);
        map.put("downList", downList);
        // 5、返回结果
        return R.ok(map);
    }
```

#### 4、定义mapper接口及xml

mapper接口：

```java
		/**
     * 根据指定日期时间范围，统计对应范围内每分钟的涨停或者跌停数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param flag 标识。 1：涨停   0：跌停
     * @return
     */
    List<Map> getStockUpdownCount(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("flag") int flag);
```

Xml:

```xml
		<select id="getStockUpdownCount" resultType="java.util.Map">
        SELECT
            DATE_FORMAT(tmp.cur_time, '%Y%m%d%H%m') AS time,
	        	COUNT(*) AS count
        FROM (
            SELECT
            sri.cur_time,
            (sri.cur_price - sri.pre_close_price) / sri.pre_close_price AS increase
            FROM
            stock_rt_info AS sri
            WHERE
            sri.cur_time BETWEEN #{startTime} AND #{endTime}
            <if test="flag==1">
                HAVING
                increase >= 0.1
            </if>
            <if test="flag==0">
                HAVING
                increase &lt;= -0.1
            </if>
        ) AS tmp
        GROUP BY tmp.cur_time
        ORDER BY tmp.cur_time ASC;
    </select>
```

#### 5、测试

![image-20230210160606407](https://p.ipic.vip/vxxa5l.png)



![image-20230210161327465](https://p.ipic.vip/bf03lr.png)

## 八、涨幅榜导出数据功能开发

### EasyExcel入门

传统操作 Excel 大多都是利用 Apache POI 进行操作，使用过程较为繁琐。阿里开源出一款易上手、节省内存的 Excel 操作框架：EasyExcel。注意：**EasyExcel 的底层是用 POI 实现的**。

https://easyexcel.opensource.alibaba.com/

```xml
<dependency>
		<groupId>com.alibaba</groupId>
		<artifactId>easyexcel</artifactId>
</dependency>
```

### 业务分析

#### 1、接口说明

```json
功能说明：将指定页数据导出到 excel 表下
请求地址：/api/quot/stock/export
请求方式：GET
请求参数:
	{
			page:2 // 当前页
			pageSize:20	// 每页大小
	}
响应：excel文件
```

#### 2、excel实体类

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockExcelDomain {

    /**
     * 股票编码
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "股票编码"}, index = 0)
    private String code;
    /**
     * 股票名称
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "股票名称"}, index = 1)
    private String name;
    /**
     * 前收盘价
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "前收盘价格"}, index = 2)
    private BigDecimal preClosePrice;
    /**
     * 当前价格
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "当前价格"}, index = 3)
    private BigDecimal tradePrice;
    /**
     * 涨跌
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "涨跌"}, index = 4)
    private BigDecimal increase;
    /**
     * 涨跌幅度
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "涨跌幅度"}, index = 5)
    private BigDecimal upDown;
    /**
     * 振幅
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "振幅"}, index = 6)
    private BigDecimal amplitude;
    /**
     * 交易量
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "交易总量"}, index = 7)
    private Long tradeAmt;
    /**
     * 交易金额
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "交易总金额"}, index = 8)
    private BigDecimal tradeVol;
    /**
     * 日期
     */
    @ExcelProperty(value = {"股票涨幅信息统计表", "日期"}, index = 9)
    private String curDate;
}
```

### 功能开发

#### 1、定义访问接口

```java
		/**
     * 导出股票信息到excel下
     * @param response http的响应对象，可获取写出流对象
     * @param page 当前页
     * @param pageSize 每页大小
     */
    @GetMapping("/stock/export")
    public void stockExport(HttpResponse response, Integer page, Integer pageSize) {
        return stockService.stockExport(response, page, pageSize);
    }
```

#### 2、定义服务接口以及实现

接口：

```java
		/**
     * 导出股票信息到excel下
     * @param response http的响应对象，可获取写出流对象
     * @param page 当前页
     * @param pageSize 每页大小
     */
    void stockExport(HttpServletResponse response, Integer page, Integer pageSize) throws IOException;
```

实现类：

```java
		@Override
    public void stockExport(HttpServletResponse response, Integer page, Integer pageSize) throws IOException {
        // 1、设置响应数据类型：excel
        response.setContentType("application/vnd.ms-excel");
        // 2、设置响应数据编码格式
        response.setCharacterEncoding("utf-8");
        // 3、设置默认的文件名称
        // 此处的 URLEncoder.encode 可以防止中文乱码，与 easyExcel 无关
        String fileName = URLEncoder.encode("stockRt", "UTF-8");
        // 设置默认文件名称
        response.setHeader("content-disposition", "attachment;filename=" + fileName + ".xlsx");

        // 读取导出的数据集合
        // 1、设置分页参数
        PageHelper.startPage(page, pageSize);
        // 2、查询
        List<StockUpdownDomain> pages = stockRtInfoMapper.getStockRtInfo4All();
        if (CollectionUtils.isEmpty(pages)) {
            R<String> error = R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
            // 将错误信息转化为 json 格式字符串响应前段
            String jsonData = new Gson().toJson(error);
            response.getWriter().write(jsonData);
            return;
        }

        // 将 List<StockUpdownDomain> 转化为 List<ExcelDomain>
        List<StockExcelDomain> domains = pages.stream().map(item -> {
            StockExcelDomain stockExcelDomain = new StockExcelDomain();
            BeanUtils.copyProperties(item, stockExcelDomain);
            return stockExcelDomain;
        }).collect(Collectors.toList());

        // 数据导出
        EasyExcel
                .write(response.getOutputStream(), StockExcelDomain.class)
                .sheet("stockInfo")
                .doWrite(domains);
    }
```

#### 3、测试

![image-20230210180417332](https://p.ipic.vip/9oglfd.png)

![image-20230210180345976](https://p.ipic.vip/0pov44.png)

## 九、股票成交量对比功能开发

### 业务分析

#### 1、产品原型

需要 T-1 日与 T 日的时间线对齐。

<img src="https://p.ipic.vip/rzqf0y.png" alt="image-20230210210231559" style="zoom: 50%;" />

#### 2、相关表结构

我们关注的是 `stock_market_info` 大盘表。

![image-20230210210449908](https://p.ipic.vip/r89al1.png)

#### 3、接口分析

```json
功能描述：统计国内A股大盘T日和T-1日成交量对比功能（成交量为沪市和深市成交量之和）
服务路径：/api/quot/stock/tradevol
服务方法：GET
请求频率：每分钟
请求参数：无
```

注意：如果当前日期不在股票交易日，则按照前一个有效股票交易日作为T日查询。

返回数据格式：

```json
{
    "code": 1,
    "data": {
        "volList": [{"count": 3926392,"time": "202112310930"},......], //T日每分钟成交量信息
        "yesVolList":[{"count": 3926392,"time": "202112310930"},......] //T-1日每分钟成交量信息 
		}
}
```

### 功能开发

#### 1、sql分析

分析：计算国内大盘（上证、深证）交易量的统计，同时，需要获取的是 T 日和 T-1 日的数据。可能需要关注的字段：`market_id`, `cur_time`, `trade_volume`

```sql
-- 步骤一：统计 T 日的数据
SELECT
	DATE_FORMAT(smi.cur_time, '%Y%m%d%H%m') AS time,
	SUM(smi.trade_volume) AS count
FROM
	stock_market_index_info AS smi
WHERE
	smi.mark_Id IN ('s_sh000001', 's_sz399001')
AND
	smi.cur_time BETWEEN '2021-12-27 09:30:00' AND '2021-12-27 14:40:00'
GROUP BY smi.cur_time
ORDER BY smi.cur_time ASC;

-- 步骤二：统计 T-1 日的数据
SELECT
	DATE_FORMAT(smi.cur_time, '%Y%m%d%H%m') AS time,
	SUM(smi.trade_volume) AS count
FROM
	stock_market_index_info AS smi
WHERE
	smi.mark_Id IN ('s_sh000001', 's_sz399001')
AND
	smi.cur_time BETWEEN '2021-12-26 09:30:00' AND '2021-12-26 14:40:00'
GROUP BY smi.cur_time
ORDER BY smi.cur_time ASC;
```

#### 2、定义web接口方法

```java
		/**
     * 统计国内A股大盘T日和T-1日成交量对比功能（成交量为沪市和深市成交量之和）
     * @return
     */
    @GetMapping("/stock/tradevol")
    public R<Map> getStockTradeVol4Comparison() {
        return stockService.getStockTradeVol4Comparison();
    }
```

#### 3、定义service服务接口以及方法

```java
		/**
     * 统计国内A股大盘T日和T-1日成交量对比功能（成交量为沪市和深市成交量之和）
     * @return
     */
    R<Map> getStockTradeVol4Comparison();
```

实现类：

```java
@Override
    public R<Map> getStockTradeVol4Comparison() {
        // 1、获取 T 日和 T-1 日的开始时间和结束时间
        // 1.1 获取最近有效交易时间点----T日
        DateTime lastDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());
        // 1.2 获取对应时间的的开盘日期
        DateTime openDateTime = DateTimeUtil.getOpenDate(lastDateTime);
        // 转换成 java 中的 date
        Date startTime4T = openDateTime.toDate();
        Date endTime4T = lastDateTime.toDate();
        // TODO mock数据
        startTime4T = DateTime.parse("2021-12-27 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        endTime4T = DateTime.parse("2021-12-27 14:40:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 获取 T-1 日的区间范围
        DateTime preLastDateTime = DateTimeUtil.getPreviousTradingDay(lastDateTime);
        DateTime preOpenDateTime = DateTimeUtil.getOpenDate(preLastDateTime);
        Date startTime4PreT = preLastDateTime.toDate();
        Date endTime4PreT = preOpenDateTime.toDate();
        // TODO mock数据
        startTime4PreT = DateTime.parse("2021-12-26 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        endTime4PreT = DateTime.parse("2021-12-26 14:40:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 2、获取上证和深证的 market_id
        List<String> marketIds = stockInfoConfig.getInner();
        // 3、分别查询 T 日和 T-1 日的交易量数据，得到两个 List
        // 3.1 查询 T 日
        List<Map> volList = stockMarketIndexInfoMapper.getStockTradeVol(marketIds, startTime4T, endTime4T);
        // 3.2 查询 T-1 日
        List<Map> yesVolList = stockMarketIndexInfoMapper.getStockTradeVol(marketIds, startTime4PreT, endTime4PreT);
        // 4、组装map
        Map<String, List> map = new HashMap<>();
        map.put("volList", volList);
        map.put("yesVolList", yesVolList);
        // 5、返回数据
        return R.ok(map);
    }
```

#### 4、定义mapper接口与xml

```java
		/**
     * 根据指定的大盘id集合和时间范围，统计每分钟的交易量
     * @param marketIds 大盘id集合
     * @param startTime 交易开始时间
     * @param endTime 结束时间
     * @return
     */
    List<Map> getStockTradeVol(@Param("marketIds") List<String> marketIds, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
```

Xml:

```xml
<select id="getStockTradeVol" resultType="java.util.Map">
        SELECT
            DATE_FORMAT(smi.cur_time, '%Y%m%d%H%m') AS time,
	        SUM(smi.trade_volume) AS count
        FROM
            stock_market_index_info AS smi
        WHERE
            smi.mark_Id IN
            <foreach collection="marketIds" item="marketId" open="(" separator="," close=")">
                #{marketId}
            </foreach>
          AND
            smi.cur_time BETWEEN #{startTime} AND #{endTime}
        GROUP BY smi.cur_time
        ORDER BY smi.cur_time ASC
    </select>
```

#### 5、测试

![image-20230210232732055](https://p.ipic.vip/kj4aj0.png)

![image-20230210232917274](https://p.ipic.vip/k3w862.png)

## 十、个股分时涨跌幅度开发

### 业务分析

#### 1、业务原型

<img src="https://p.ipic.vip/zf3on3.png" alt="image-20230210233651826" style="zoom:50%;" />

#### 2、接口分析

```json
功能描述：统计当前时间下（精确到分钟），股票在各个涨跌区间的数量
服务路径：/api/quot/stock/updown
服务方法：GET
请求频率：每分钟
请求参数：无
注意事项：如果当前不在股票有效时间内，则以前一个有效股票交易日作为查询时间点
```

响应数据格式：

```json
{
    "code": 1,
    "data": {
        "time": "2021-12-31 14:58:00",
        "infos": [
            {
                "count": 17,
                "title": "-3~0%"
            },
            {
                "count": 2,
                "title": "-5~-3%"
            },
            {
                "count": 1,
                "title": "-7~-5%"
            },
            {
                "count": 16,
                "title": "0~3%"
            },
            {
                "count": 1,
                "title": "3~5%"
            }
        ]
    }
}
```

### 功能实现

#### 1、编写sql语句

```sql
-- 需求：统计当前时间下（精确到分钟），股票在各个涨跌区间的数量
-- 涨幅计算公式：(当前价格 - 前一日收盘价格)/前一日收盘价格
-- 2022-01-07 14:50:00

-- 第一步：统计指定时间点下股票的涨幅值
SELECT
	(sri.cur_price - sri.pre_close_price) / sri.pre_close_price AS updown
FROM
	stock_rt_info AS sri
WHERE
	sri.cur_time = '2022-01-07 14:50:00'

-- 步骤二：将步骤一的涨幅结果转化成涨幅区间名称
SELECT
	CASE
				WHEN tmp.rate > 0.07 THEN  '>7%'
				WHEN tmp.rate > 0.05 AND tmp.rate <= 0.07 THEN '5~7%'
				WHEN tmp.rate > 0.03 AND tmp.rate <= 0.05 THEN '3~5%'
				WHEN tmp.rate > 0 AND tmp.rate <= 0.03 THEN '0~3%'
				WHEN tmp.rate > -0.03 AND tmp.rate <= 0 THEN '-3~0%'
				WHEN tmp.rate > -0.05 AND tmp.rate <= -0.03 THEN '-5~-3%'
				WHEN tmp.rate > -0.07 AND tmp.rate <= -0.05 THEN '-7~-5%'
				ELSE '<-7%'
	END 'title'
FROM (
	SELECT
		(sri.cur_price - sri.pre_close_price) / sri.pre_close_price AS rate
	FROM
		stock_rt_info AS sri
	WHERE
		sri.cur_time = '2022-01-07 14:50:00'
)	AS tmp

-- 步骤三：只需将步骤二的结果根据区间名称分组，聚合统计数量即可
SELECT
	tmp2.title,
	COUNT(*) as count
FROM (
	SELECT
	CASE
				WHEN tmp.rate > 0.07 THEN  '>7%'
				WHEN tmp.rate > 0.05 AND tmp.rate <= 0.07 THEN '5~7%'
				WHEN tmp.rate > 0.03 AND tmp.rate <= 0.05 THEN '3~5%'
				WHEN tmp.rate > 0 AND tmp.rate <= 0.03 THEN '0~3%'
				WHEN tmp.rate > -0.03 AND tmp.rate <= 0 THEN '-3~0%'
				WHEN tmp.rate > -0.05 AND tmp.rate <= -0.03 THEN '-5~-3%'
				WHEN tmp.rate > -0.07 AND tmp.rate <= -0.05 THEN '-7~-5%'
				ELSE '<-7%'
	END 'title'
	FROM (
		SELECT
			(sri.cur_price - sri.pre_close_price) / sri.pre_close_price AS rate
		FROM
			stock_rt_info AS sri
		WHERE
			sri.cur_time = '2022-01-07 14:50:00'
	)	AS tmp
) AS tmp2
GROUP BY tmp2.title
```

#### 2、定义web访问接口

```java
		/**
     * 查询当前时间下股票的涨跌幅度区间统计功能
     * 如果当前日期不在有效时间内，则以最近的一个股票交易时间作为查询点
     * @return
     */
    @GetMapping("/stock/updown")
    public R<Map> getStockUpDownRegion(){
        return stockService.getStockUpDownRegion();
    }
```

#### 3、定义service服务接口与实现类

```java
		/**
     * 查询当前时间下股票的涨跌幅度区间统计功能
     * 如果当前日期不在有效时间内，则以最近的一个股票交易时间作为查询点
     * @return
     */
    R<Map> getStockUpDownRegion();
```

实现类：

```java
		/**
     * 查询当前时间下股票的涨跌幅度区间统计功能
     * 如果当前日期不在有效时间内，则以最近的一个股票交易时间作为查询点
     * @return
     */
    @Override
    public R<Map> getStockUpDownRegion() {
        // 1、获取最近有效的交易时间点
        DateTime dateTime4Stock = DateTimeUtil.getLastDate4Stock(DateTime.now());
        Date lastDate = dateTime4Stock.toDate();
        // TODO:mock数据
        lastDate = DateTime.parse("2022-01-07 14:50:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();

        // 2、插入mapper接口获取统计数据
        List<Map> infos = stockRtInfoMapper.getStockUpDownRegion(lastDate);
        // 3、组装数据并响应
        HashMap<String, Object> data = new HashMap<>();
        String stringDataTime = dateTime4Stock.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        data.put("time", stringDataTime);
        data.put("infos", infos);
        return R.ok(data);
    }
```

#### 4、定义mapper接口与xml

mapper接口：

```java
		/**
     * 统计指定时间点下，各个涨跌区间内股票的个数
     * @param timePoint 股票交易时间点
     * @return
     */
    List<Map> getStockUpDownRegion(Date timePoint);
```

xml:

```xml
		<select id="getStockUpDownRegion" resultType="java.util.Map">
        SELECT
            tmp2.title,
            COUNT(*) as count
        FROM (
            SELECT
                CASE
                WHEN tmp.rate > 0.07 THEN  '>7%'
                WHEN tmp.rate > 0.05 AND tmp.rate &lt;= 0.07 THEN '5~7%'
                WHEN tmp.rate > 0.03 AND tmp.rate &lt;= 0.05 THEN '3~5%'
                WHEN tmp.rate > 0 AND tmp.rate &lt;= 0.03 THEN '0~3%'
                WHEN tmp.rate > -0.03 AND tmp.rate &lt;= 0 THEN '-3~0%'
                WHEN tmp.rate > -0.05 AND tmp.rate &lt;= -0.03 THEN '-5~-3%'
                WHEN tmp.rate > -0.07 AND tmp.rate &lt;= -0.05 THEN '-7~-5%'
                ELSE '&lt;-7%'
                END 'title'
            FROM (
                SELECT
                    (sri.cur_price - sri.pre_close_price) / sri.pre_close_price AS rate
                FROM
                    stock_rt_info AS sri
                WHERE
                    sri.cur_time = #{timePoint}
            )	AS tmp
        ) AS tmp2
        GROUP BY tmp2.title
    </select>
```

#### 5、优化

![image-20230211101949072](https://p.ipic.vip/mhz6ni.png)

目前存在的问题：

1. 我们当前在前端查询的数据是 **无序** 展示的，需要后端对数据进行合理排序，这样前端才能顺序展示;
2. **对于不存在数据的区间不显示**，显然不合理，我们可对无数据的区间默认为 0 给前端显示；

##### 5.1 在yml文件中定义股票涨幅范围集合

```yml
# 配置股票相关的参数
stock:
  upDownRange:
    - "<-7%"
    - "-7~-5%"
    - "-5~-3%"
    - "-3~0%"
    - "0~3%"
    - "3~5%"
    - "5~7%"
    - ">7%"
```

##### 5.2 封装实体类

```java
@Data
@ConfigurationProperties(prefix = "stock")
public class StockInfoConfig {
    /**
     * A股code集合
     */
    private List<String> inner;
    /**
     * 外股code集合
     */
    private List<String> outer;
    /**
     * 定义股票涨幅区间的顺序
     */
    private List<String> upDownRange;
}
```

##### 5.2 service实现类修改逻辑

```java
@Override
    public R<Map> getStockUpDownRegion() {
        // 1、获取最近有效的交易时间点
        DateTime dateTime4Stock = DateTimeUtil.getLastDate4Stock(DateTime.now());
        Date lastDate = dateTime4Stock.toDate();
        // TODO:mock数据
        lastDate = DateTime.parse("2022-01-07 14:50:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();

        // 2、插入mapper接口获取统计数据（无序的）
        List<Map> infos = stockRtInfoMapper.getStockUpDownRegion(lastDate);

        // 保证涨幅区间按照从小到大排序，且对于没有数据的涨幅区间默认为0
        // 2.1 获取涨幅区间顺序集合
        List<String> upDownRangeList = stockInfoConfig.getUpDownRange();

        // 2.2 遍历顺序集合，在统计数据（无序）中查找对应结果，没有则设置为0
        List<Map> newList = upDownRangeList.stream().map(item->{
            Optional<Map> optional = infos.stream().filter(map -> map.get("title").equals(item)).findFirst();
            Map tmp = null;
            // 判断结果是否有map
            if (optional.isPresent()) {
                tmp = optional.get();
            } else {
                tmp = new HashMap();
                tmp.put("title", item);
                tmp.put("count", 0);
            }
            return tmp;
        }).collect(Collectors.toList());
        // 3、组装数据并响应
        HashMap<String, Object> data = new HashMap<>();
        String stringDataTime = dateTime4Stock.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        data.put("time", stringDataTime);
        data.put("infos", newList);
        return R.ok(data);
    }
```

#### 6、测试

![image-20230211103906749](https://p.ipic.vip/wn60v7.png)

![image-20230211104018378](https://p.ipic.vip/9g737c.png)

## 十一、个股分时K线图功能开发

### 业务分析

#### 1、页面原型



<img src="https://p.ipic.vip/x6jsag.png" alt="image-20230211104843751" style="zoom:50%;" />

#### 2、接口分析

```
功能描述：查询个股的分时行情数据，也就是统计指定股票T日每分钟的交易数据;
服务路径：/api/quot/stock/screen/time-sharing
服务方法：GET
请求参数：code(股票code)
请求频率：每分钟
响应数据字段：日期 交易量 交易金额 最低价 最高价 前收盘价 公司名称 开盘价 股票code 当前价
注意点：如果当前日期不在有效时间内，则以最近的一个股票交易时间作为查询时间点
```

响应数据格式：

```json
{
    "code": 1,
    "data": [
        {
            "date": "202112310925",//当前时间，精确到分钟
            "tradeAmt": 63263,//当前交易量
            "code": "000021",//股票编码
            "lowPrice": 15.85,//最低价格
            "preClosePrice": 15.85,//前收盘价格
            "name": "深科技",//股票名称
            "highPrice": 15.85,//最高价格
            "openPrice": 15.85,//开盘价
            "tradeVol": 1002718.55,//交易金额
            "tradePrice": 15.85//当前价格（最新价格）
        },
        {
            "date": "202112310930",
            "tradeAmt": 236890,
            "code": "000021",
            "lowPrice": 15.85,
            "preClosePrice": 15.85,
            "name": "深科技",
            "highPrice": 15.97,
            "openPrice": 15.85,
            "tradeVol": 3770610.93,
            "tradePrice": 15.96
        }
          ]
}
```

#### 3、表结构分析

该功能需要的数据只需在 `stock_rt_info` 表中查询即可。

![image-20230211105228047](https://p.ipic.vip/y5ybqy.png)

#### 4、DO封装

```java
/**
 * Stock4MinuteDomain
 * @description 股票详情DO
 * @author SongJian
 * @date 2023/2/11 10:54
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock4MinuteDomain {
    /**
     * 日期，eg:202201280809
     */
    private String date;
    /**
     * 交易金额
     */
    private Long tradeAmt;
    /**
     * 股票编码
     */
    private String code;
    /**
     * 最低价
     */
    private BigDecimal lowPrice;
    /**
     * 前收盘价
     */
    private BigDecimal preClosePrice;
    /**
     * 股票名称
     */
    private String name;
    /**
     * 最高价
     */
    private BigDecimal highPrice;
    /**
     * 开盘价
     */
    private BigDecimal openPrice;

    /**
     * 交易量
     */
    private BigDecimal tradeVol;
    /**
     * 当前价格
     */
    private BigDecimal tradePrice;
}
```

### 功能开发

#### 1、编写sql

分析：查询个股的分时数据详情，即统计指定股票T日每分钟的交易数据

条件：

- 股票编码code
- T日的开盘时间
- T日的当前（或）收盘时间

```sql
SELECT
	DATE_FORMAT(sri.cur_time, '%Y%m%d%H%i') AS date,
	sri.trade_amount AS tradeAmt,
	sri.stock_code AS code,
	sri.min_price AS lowPrice,
	sri.pre_close_price AS preClosePrice,
	sri.stock_name AS name,
	sri.max_price AS highPrice,
	sri.open_price AS openPrice,
	sri.trade_volume AS tradeVol,
	sri.cur_price AS tradePrice
FROM
	stock_rt_info AS sri
WHERE
	sri.stock_code = '000001'
AND
	sri.cur_time BETWEEN '2022-01-07 09:30:00' AND '2022-01-07 14:48:00';
```

#### 2、定义web访问接口

```java
		/**
     * 查询个股的分时行情数据，也就是统计指定股票T日每分钟的交易数据；
     *
     * @param stockCode 股票编码
     * @return
     */
    @GetMapping("/stock/screen/time-sharing")
    public R<List<Stock4MinuteDomain>> stockScreenTimeSharing(@RequestParam("code") String stockCode) {
        return stockService.stockScreenTimeSharing(stockCode);
    }
```

#### 3、定义service接口与实现类

```java
		/**
     * 查询个股的分时行情数据，也就是统计指定股票T日每分钟的交易数据；
     * @param stockCode
     * @return
     */
    R<List<Stock4MinuteDomain>> stockScreenTimeSharing(String stockCode);
```

实现类：

```java
		/**
     * 查询个股的分时行情数据，也就是统计指定股票T日每分钟的交易数据；
     * @param stockCode
     * @return
     */
    @Override
    public R<List<Stock4MinuteDomain>> stockScreenTimeSharing(String stockCode) {
        // 1、获取最近最新时间交易时间点和对应的开盘日期
        DateTime lastDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());
        DateTime openDateTime = DateTimeUtil.getOpenDate(lastDateTime);
        Date endTime = lastDateTime.toDate();
        Date startTime = openDateTime.toDate();
        // TODO:mock数据
        endTime = DateTime.parse("2022-01-07 14:48:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        startTime = DateTime.parse("2022-01-07 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 2、调用mapper接口进行查询
        List<Stock4MinuteDomain> list = stockRtInfoMapper.getStockInfoByCodeAndDate(stockCode, startTime, endTime);
        if (CollectionUtils.isEmpty(list)) {
            list = new ArrayList<>();
        }
        return R.ok(list);
    }
```

#### 4、定义mapper接口与xml

mapper接口：

```java
		/**
     * 查询个股的分时行情数据，也就是统计指定股票T日每分钟的交易数据；
     * @param stockCode 股票编码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    List<Stock4MinuteDomain> getStockInfoByCodeAndDate(@Param("stockCode") String stockCode, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
```

xml:

```xml
		<select id="getStockInfoByCodeAndDate" resultType="tech.songjian.stock.common.domain.Stock4MinuteDomain">
        SELECT
            DATE_FORMAT(sri.cur_time, '%Y%m%d%H%i') AS date,
	        sri.trade_amount AS tradeAmt,
	        sri.stock_code AS code,
	        sri.min_price AS lowPrice,
	        sri.pre_close_price AS preClosePrice,
	        sri.stock_name AS name,
	        sri.max_price AS highPrice,
	        sri.open_price AS openPrice,
	        sri.trade_volume AS tradeVol,
	        sri.cur_price AS tradePrice
        FROM
            stock_rt_info AS sri
        WHERE
            sri.stock_code = #{stockCode}
          AND
            sri.cur_time BETWEEN #{startTime} AND #{endTime}
    </select>
```

#### 5、测试

![image-20230211112631211](https://p.ipic.vip/aqj35d.png)

![image-20230211112650546](https://p.ipic.vip/xa86pk.png)

![image-20230211112704466](https://p.ipic.vip/8lmewe.png)

## 十二、个股日K线图功能开发

### 业务分析

#### 1、业务原型

以天为单位，统计个股在每天的交易数据，数据包含日期、股票编码、名称、最高价、最低价、开盘价、收盘价、当前价格。

![image-20230211113725257](https://p.ipic.vip/py1r1h.png)

#### 2、接口说明

```
功能描述：个股日K数据查询 ，可以根据时间区间查询数日的K线数据
					默认查询历史20天的数据；
服务路径：/api/quot/stock/screen/dkline
服务方法：GET
请求参数：code(股票编码)
请求频率：每分钟
```

响应数据结构：

```json
{
    "code": 1,
    "data": [
        {
            "date": "20211220",//日期
            "tradeAmt": 28284252,//交易量(指收盘时的交易量，如果当天未收盘，则显示最新数据)
            "code": "000021",//股票编码
            "lowPrice": 16,//最低价格（指收盘时记录的最低价，如果当天未收盘，则显示最新数据）
            "name": "深科技",//名称
            "highPrice": 16.83,//最高价（指收盘时记录的最高价，如果当天未收盘，则显示最新数据）
            "openPrice": 16.8,//开盘价
            "tradeVol": 459088567.58,//交易金额（指收盘时记录交易量，如果当天未收盘，则显示最新数据）
            "closePrice": 16.81//当前收盘价（指收盘时的价格，如果当天未收盘，则显示最新cur_price）
            "preClosePrice": 16.81//前收盘价
        },
        //......
    ]
}
```

#### 3、数据库表分析

该功能需要的数据只需在 `stock_rt_info` 表中查询即可，但还差个收盘价，无法直接获取。

![image-20230211114011084](https://p.ipic.vip/8pu7jb.png)

#### 4、DO对象封装

```java
/**
 * Stock4EvrDayDomain
 * @description 以天为单位统计个股数据
 * @author SongJian
 * @date 2023/2/11 11:41
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock4EvrDayDomain {
    /**
     * 日期，eg:202201280809
     */
    private String date;
    /**
     * 交易量
     */
    private Long tradeAmt;
    /**
     * 股票编码
     */
    private String code;
    /**
     * 最低价
     */
    private BigDecimal lowPrice;
    /**
     * 股票名称
     */
    private String name;
    /**
     * 最高价
     */
    private BigDecimal highPrice;
    /**
     * 开盘价
     */
    private BigDecimal openPrice;
    /**
     * 当前交易总金额
     */
    private BigDecimal tradeVol;
    /**
     * 当前收盘价格指收盘时的价格，如果当天未收盘，则显示最新cur_price）
     */
    private BigDecimal closePrice;
    /**
     * 前收盘价
     */
    private BigDecimal preClosePrice;
}
```

### 功能开发

#### 1、编写sql

因为在股票流水中，开盘价、最高价、最低价、当前价等信息在每条记录中都会记录，所以我们更加关注的是每天的 **收盘价格**，业务要求如果当前没有收盘，则以最新价格作为收盘价，所以该业务就可以 **转化成查询每天最大交易时间对应的信息**；

```sql
-- 步骤一：查询指定股票在指定日期范围内每天的最大时间，即以天分组，求每天最大时间
SELECT
	MAX(sri.cur_time) AS closeDate
FROM
	stock_rt_info AS sri
WHERE
	sri.stock_code = '000001'
AND
	sri.cur_time BETWEEN '2021-12-20 09:30:00' AND '2021-12-30 14:28:00'
GROUP BY
	DATE_FORMAT(sri.cur_time, '%Y%m%d')

-- 步骤二：以步骤一的结果为条件，统计指定时间点下，股票的数据信息
SELECT
	date_format(sri2.cur_time,'%Y%m%d') AS date,
  sri2.trade_amount AS tradeAmt,
  sri2.stock_code AS code,
  sri2.min_price AS lowPrice,
  sri2.stock_name AS name,
  sri2.max_price AS highPrice,
  sri2.open_price AS openPrice,
  sri2.trade_volume AS tradeVol,
  sri2.cur_price AS closePrice,
  sri2.pre_close_price AS preClosePrice
FROM
	stock_rt_info AS sri2
WHERE
	sri2.stock_code = '000001'
AND
	sri2.cur_time IN (
		SELECT
			MAX(sri.cur_time) AS closeDate
		FROM
			stock_rt_info AS sri
		WHERE
			sri.stock_code = '000001'
		AND
			sri.cur_time BETWEEN '2021-12-20 09:30:00' AND '2021-12-30 14:28:00'
		GROUP BY
			DATE_FORMAT(sri.cur_time, '%Y%m%d')
	)
ORDER BY sri2.cur_time ASC
```

#### 2、定义web访问接口

```java
		/**
     * 个股日K数据查询 ，可以根据时间区间查询数日的K线数据
     * @param code
     * @return
     */
    @GetMapping("/stock/screen/dkline")
    public R<List<Stock4EvrDayDomain>> stockScreenDKLine(String code) {
        return stockService.stockScreenDKLine(code);
    }
```

#### 3、定义service服务接口及实现类

```java
		/**
     * 个股日K数据查询 ，可以根据时间区间查询数日的K线数据
     * @param code
     * @return
     */
    R<List<Stock4EvrDayDomain>> stockScreenDKLine(String code);
```

实现类：

```java
		/**
     * 个股日K数据查询 ，可以根据时间区间查询数日的K线数据
     * @param code
     * @return
     */
    @Override
    public R<List<Stock4EvrDayDomain>> stockScreenDKLine(String code) {
        // 1、获取查询日期范围
        // 1.1 获取截止时间
        DateTime endDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());
        Date endTime = endDateTime.toDate();
        // 1.2 获取开始时间
        DateTime startDateTime = endDateTime.minusDays(10);
        Date startTime = startDateTime.toDate();
        // TODO:mock数据
        endTime = DateTime.parse("2021-12-30 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        startTime = DateTime.parse("2021-12-20 15:00:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 2、调用mapper接口获取查询结果
        List<Stock4EvrDayDomain> data = stockRtInfoMapper.getStockRtInfo4EvrDat(code, startTime, endTime);
        if (CollectionUtils.isEmpty(data)) {
            data = new ArrayList<>();
        }
        // 3、封装结果返回
        return R.ok(data);
    }
```

#### 4、定义mapper接口及xml

```java
    /**
     * 个股日K数据查询 ，可以根据时间区间查询数日的K线数据
     * @param stockCode
     * @param startTime
     * @param endTime
     * @return
     */
    List<Stock4EvrDayDomain> getStockRtInfo4EvrDat(@Param("stockCode") String stockCode, @Param("startTime") Date startTime, @Param("endTime") Date endTime);
```

xml：

```xml
    <select id="getStockRtInfo4EvrDat" resultType="tech.songjian.stock.common.domain.Stock4EvrDayDomain">
        SELECT
            date_format(sri2.cur_time,'%Y%m%d') AS date,
            sri2.trade_amount AS tradeAmt,
            sri2.stock_code AS code,
            sri2.min_price AS lowPrice,
            sri2.stock_name AS name,
            sri2.max_price AS highPrice,
            sri2.open_price AS openPrice,
            sri2.trade_volume AS tradeVol,
            sri2.cur_price AS closePrice,
            sri2.pre_close_price AS preClosePrice
        FROM
            stock_rt_info AS sri2
        WHERE
            sri2.stock_code = #{stockCode}
          AND
            sri2.cur_time IN (
            SELECT
            MAX(sri.cur_time) AS closeDate
            FROM
            stock_rt_info AS sri
            WHERE
            sri.stock_code = #{stockCode}
          AND
            sri.cur_time BETWEEN #{startTime} AND #{endTime}
            GROUP BY
            DATE_FORMAT(sri.cur_time, '%Y%m%d')
            )
        ORDER BY sri2.cur_time ASC
    </select>
```

#### 5、测试

<img src="https://p.ipic.vip/u1ejov.png" alt="image-20230211142343758"  />

<img src="https://p.ipic.vip/y4fe9v.png" alt="image-20230211142401056" style="zoom:50%;" />

## 十三、金融股票API接口说明

目前市面上有一些正规的API金融接口，可为我们提供实时的股票金融数据，同时也提供有较为完整的开发文档，用起来也会更方便一些，但是大多是付费的；

以下为可用的股票API接口：

| 提供方     | 地址                                                         | 是否收费 |
| ---------- | ------------------------------------------------------------ | -------- |
| 新浪财经   | https://hq.sinajs.cn/list=sh601003,sh601001                  | 否       |
| 腾讯财经   | http://qt.gtimg.cn/q=sh601003,sh601001                       | 否       |
| 雪球       | https://stock.xueqiu.com/v5/stock/realtime/quotec.json?symbol=SH601003,SH601001 | 付费     |
| 聚合数据   | https://www.juhe.cn/                                         | 付费     |
| 阿里云社区 | https://market.aliyun.com/products/56956004/                 | 付费     |

### sina API接口说明

当前以新浪接口为例获取股票实时数据，新浪财经提供了国内大盘、外盘、板块等数据的接口，比如：

```java
获取国内大盘实时数据：
    http://hq.sinajs.cn/list=s_sh000001,s_sz399001
    数据响应格式：
    var hq_str_s_sh000001="上证指数,3639.7754,20.5868,0.57,3296819,43348980";
    var hq_str_s_sz399001="深证成指,14857.35,61.114,0.41,468675226,62673650";
-------------------------------------------------------------------------------------------- 
获取国外大盘实时数据：
	http://hq.sinajs.cn/list=int_dji,int_nasdaq,int_hangseng,int_nikkei,b_TWSE,b_FSSTI
	数据响应格式：
	var hq_str_int_dji="道琼斯,36338.30,-59.78,-0.16";
    var hq_str_int_nasdaq="纳斯达克,15644.97,-96.59,-0.61";
    var hq_str_int_hangseng="恒生指数,23397.67,285.66,1.24";
    var hq_str_int_nikkei="日经指数,28791.71,-115.17,-0.40";
    var hq_str_b_TWSE="台湾台北指数,18218.84,-29.44,-0.16";
    var hq_str_b_FSSTI="富时新加坡海峡时报指数,3123.68,-2.96,-0.09";
-------------------------------------------------------------------------------------------- 
获取国内股票实时数据：
	http://hq.sinajs.cn/list=sz000002,sh600015
	数据响应格式：
	var hq_str_sz000002="万 科Ａ,19.140,19.120,19.760,19.970,19.100,19.750,19.760,115918655,2275602656.000,63100,19.750,101500,19.740,119300,19.730,148300,19.720,163700,19.710,64066,19.760,159900,19.770,201000,19.780,398002,19.790,610600,19.800,2021-12-31,15:00:03,00";
	var hq_str_sh600015="华夏银行,5.590,5.590,5.600,5.610,5.580,5.590,5.600,18110584,101293498.000,1019571,5.590,2017808,5.580,742200,5.570,323100,5.560,158000,5.550,671217,5.600,1811303,5.610,1101018,5.620,515467,5.630,333800,5.640,2021-12-31,15:00:00,00,";
--------------------------------------------------------------------------------------------
获取国内股票板块实时数据：
	http://vip.stock.finance.sina.com.cn/q/view/newSinaHy.php
	数据格式：
	var S_Finance_bankuai_sinaindustry = {
"new_blhy":"new_blhy,玻璃行业,19,19.293684210526,-0.17052631578947,-0.87610188740468,315756250,5258253314,sh600586,3.464,9.260,0.310,金晶科技",
"new_cbzz":"new_cbzz,船舶制造,8,12.15875,0.0125,0.10291242152928,214866817,2282104956,sh600150,0.978,24.790,0.240,中国船舶",
  //........省略.......
}
```

## 十四、国内大盘数据采集&插入

### 开发流程

#### 1、数据采集准备

##### 股票常量数据配置

配置大盘、板块、股票相关的通用参数：

```yml
# 配置股票相关的参数
stock:
  inner: # 国内大盘ID
    - s_sh000001 # 上证ID
    - s_sz399001 #  深证ID
  outer: # 外盘ID
    - int_dji # 道琼斯
    - int_nasdaq # 纳斯达克
    - int_hangseng # 恒生
    - int_nikkei # 日经指数
    - b_TWSE # 台湾加权
    - b_FSSTI # 新加坡
  marketUrl: http://hq.sinajs.cn/list=
  blockUrl: http://vip.stock.finance.sina.com.cn/q/view/newSinaHy.php
```

##### 股票常量数据封装

```java
/**
 * StockInfoConfig
 * @description 股票常量数据封装，从yml中读取
 * @author SongJian
 * @date 2023/2/9 20:11
 * @version
 */
@Data
@ConfigurationProperties(prefix = "stock")
public class StockInfoConfig {
    /**
     * A股code集合
     */
    private List<String> inner;
    /**
     * 外股code集合
     */
    private List<String> outer;
    /**
     * 定义股票涨幅区间的顺序
     */
    private List<String> upDownRange;
    /**
     * 大盘股票数据url地址
     */
    private String marketUrl;
    /**
     * 板块Url地址
     */
    private String blockUrl;
}
```

#### 2、国内大盘响应数据说明

```
var hq_str_s_sh000001="上证指数,3094.668,-128.073,-3.97,436653,5458126";
数据含义--------------->指数名称,当前点数,当前价格,涨跌率,成交量（百手）,成交额（万元）;
```

#### 3、定义数据采集服务接口

```java
/**
 * StockTimerTaskService
 * @description 定时采集股票数据服务接口
 * @author SongJian
 * @date 2023/2/11 16:09
 * @version
 */

public interface StockTimerTaskService {
    /**
     * 获取国内大盘的实时数据信息
     */
    void collectInnerMarketInfo();
}
```

定义服务类接口实现：

```java
		/**
     * 获取国内大盘的实时数据信息
     */
    @Override
    public void collectInnerMarketInfo() {
        // 1、定义采集的url接口，生成完整的url地址
        String marketUrl = stockInfoConfig.getMarketUrl() + String.join(",", stockInfoConfig.getInner());
        // 2、调用 restTemplate 采集数据
        // 2.1、组装请求头
        HttpHeaders headers = new HttpHeaders();
        headers.add("Referer", "https://finance.sina.com.cn/stock/");
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
        // 2.2、组装请求对象
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        // 2.3、发起请求
        String result = restTemplate.postForObject(marketUrl, entity, String.class);
        // log.info("当前采集的数据: {}", result);
        // 3、数据解析
        // 3.1、编写正则表达式
        // var hq_str_s_sh000001="上证指数,3260.6734,-9.7092,-0.30,2606269,34174987";
        // var hq_str_s_sz399001="深证成指,11976.85,-71.419,-0.59,458163546,55112605"
        String reg="var hq_str_(.+)=\"(.+)\";";
        // 3.2、正则匹配
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(result);
        // 3.3、收集大盘封装后后的对象
        List<StockMarketIndexInfo> list = new ArrayList<>();
        while (matcher.find()) {
            // 获取大盘的id
            String marketCode = matcher.group(1);
            // 其它信息
            String other = matcher.group(2);
            String[] others = other.split(",");
            // 大盘名称
            String marketName = others[0];
            // 当前点
            BigDecimal curPoint = new BigDecimal(others[1]);
            // 当前价格
            BigDecimal curPrice = new BigDecimal(others[2]);
            // 涨跌率
            BigDecimal upDownRate = new BigDecimal(others[3]);
            // 成交量
            Long tradeAmount = Long.valueOf(others[4]);
            // 成交金额
            Long tradeVol = Long.valueOf(others[5]);
            // 当前日期
            Date now = DateTimeUtil.getDateTimeWithoutSecond(DateTime.now()).toDate();

            //封装对象
            StockMarketIndexInfo stockMarketIndexInfo = StockMarketIndexInfo.builder()
                    .id(idWorker.nextId()+"")
                    .markName(marketName)
                    .tradeVolume(tradeVol)
                    .tradeAccount(tradeAmount)
                    .updownRate(upDownRate)
                    .curTime(now)
                    .curPoint(curPoint)
                    .currentPrice(curPrice)
                    .markId(marketCode)
                    .build();

            // 添加集合
            list.add(stockMarketIndexInfo);
        }
        log.info("集合长度：{}，内容：{}", list.size(), list);
        if (CollectionUtils.isEmpty(list)) {
            log.info("");
            return;
        }
        String curTime = DateTime.now().toString(DateTimeFormat.forPattern("yyyyMMddHHmmss"));
        log.info("采集的大盘数据：{},当前时间：{}",list,curTime);

        int count = stockMarketIndexInfoMapper.insertBatch(list);
        log.info("批量插入 {} 条大盘数据", count);
    }
```

#### 4、定义mapper接口及xml

mapper接口：

```java
    /**
     * 批量插入大盘数据
     * @param list
     * @return
     */
    int insertBatch(List<StockMarketIndexInfo> infos);
```

Xml:

```xml
		<insert id="insertBatch">
        insert into stock_market_index_info
            (id,mark_Id,cur_time
            ,mark_name,cur_point,current_price
            ,updown_rate,trade_account,trade_volume
            )
        values
            <foreach collection="stockMarketInfoList" item="smi" separator=",">
                (#{smi.id},#{smi.markId,jdbcType=CHAR},#{smi.curTime,jdbcType=TIMESTAMP}
                ,#{smi.markName,jdbcType=VARCHAR},#{smi.curPoint,jdbcType=DECIMAL},#{smi.currentPrice,jdbcType=DECIMAL}
                ,#{smi.updownRate,jdbcType=DECIMAL},#{smi.tradeAccount,jdbcType=BIGINT},#{smi.tradeVolume,jdbcType=BIGINT}
                )
            </foreach>
    </insert>
```

#### 5、测试

![image-20230211170025179](https://p.ipic.vip/omen27.png)

## 十五、股票实时数据采集&插入

### 开发流程

#### 1、采集准备

第三方接口路径：http://hq.sinajs.cn/list=sz000002,sh600015

##### 响应数据结构说明

```java
var hq_str_sh601006="大秦铁路, 27.55, 27.25, 26.91, 27.55, 26.20, 26.91, 26.92,22114263, 589824680, 4695, 26.91, 57590, 26.90, 14700, 26.89, 14300,
26.88, 15100, 26.87, 3100, 26.92, 8900, 26.93, 14230, 26.94, 25150, 26.95, 15220, 26.96, 2008-01-11, 15:05:32";
这个字符串由许多数据拼接在一起，不同含义的数据用逗号隔开了，按照程序员的思路，顺序号从0开始。
0：”大秦铁路”，股票名字；
1：”27.55″，今日开盘价；
2：”27.25″，昨日收盘价；
3：”26.91″，当前价格；
4：”27.55″，今日最高价；
5：”26.20″，今日最低价；
6：”26.91″，竞买价，即“买一”报价；
7：”26.92″，竞卖价，即“卖一”报价；
8：”22114263″，成交的股票数，由于股票交易以一百股为基本单位，所以在使用时，通常把该值除以一百；
9：”589824680″，成交金额，单位为“元”，为了一目了然，通常以“万元”为成交金额的单位，所以通常把该值除以一万；
10：”4695″，“买一”申请4695股，即47手；
11：”26.91″，“买一”报价；
12：”57590″，“买二”
13：”26.90″，“买二”
14：”14700″，“买三”
15：”26.89″，“买三”
16：”14300″，“买四”
17：”26.88″，“买四”
18：”15100″，“买五”
19：”26.87″，“买五”
20：”3100″，“卖一”申报3100股，即31手；
21：”26.92″，“卖一”报价
(22, 23), (24, 25), (26,27), (28, 29)分别为“卖二”至“卖四的情况”
30：”2008-01-11″，日期；
31：”15:05:32″，时间；
```

国内A股上市企业达3000+，一次性批量获取某个时间点下股票的详情数据，显然网络IO开销过大，我们 **可以尝试将 A 股的 id 集合做数据分片，然后再分批次批量获取股票的实时信息**；

#### 2、编写查询所有股票编码的mapper接口及xml

Mapper:

```java
    /**
     * 获取所有股票的编码
     * @return
     */
    List<String> getStockCodeList();
```

Xml:

```xml
    <select id="getStockCodeList" resultType="java.lang.String">
        select sec_code from stock_business
    </select>
```

#### 3、定义Service接口及实现类

```java
		/**
     * 采集国内 A 股 股票详情信息
     */
    void collectAShareInfo();
```

实现类：

```java
    /**
     * 采集国内 A 股 股票详情信息
     */
    @Override
    public void collectAShareInfo() {
        // 1、获取所有股票code集合（3700+）
        List<String> stockCodeList = stockBusinessMapper.getStockCodeList();
        // 转化集合中股票编码，添加前缀
        stockCodeList = stockCodeList.stream().map(id -> {
            return id.startsWith("6") ? "sh" + id : "sz" + id;
        }).collect(Collectors.toList());
        // 设置请求头对象
        HttpHeaders headers = new HttpHeaders();
        headers.add("Referer","https://finance.sina.com.cn/stock/");
        headers.add("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        // 2、将股票code集合分片处理，进行均等分
        Lists.partition(stockCodeList, 20).forEach(list->{
            //拼接股票url地址
            String stockUrl=stockInfoConfig.getMarketUrl()+String.join(",",list);
            //获取响应数据
            String result = restTemplate.postForObject(stockUrl, entity, String.class);
            // 解析处理, 3:表示A股股票
            List<StockRtInfo> infos = parserStockInfoUtil.parser4StockOrMarketInfo(result, 3);
            log.info("数据量：{}",infos.size());

            // 批量插入
            int inserts = stockRtInfoMapper.insertBatch(infos);
            log.info("插入股票详细数据 {} 条", inserts);
        });
    }
```

#### 4、定义批量保存mapper接口及xml

mapper接口：

```java
		/**
     * 批量保存股票详情数据
     * @param infos
     * @return
     */
    int insertBatch(List<StockRtInfo> infos);
```

xml：

```xml
		<insert id="insertBatch">
        insert into stock_rt_info
        ( id,stock_code,cur_time
        ,stock_name,open_price,pre_close_price
        ,cur_price,max_price,min_price
        ,trade_amount,trade_volume)
        values
        <foreach collection="stockRtInfoList" item="si" separator=",">
            (#{si.id,jdbcType=BIGINT},#{si.stockCode,jdbcType=CHAR},#{si.curTime,jdbcType=TIMESTAMP}
            ,#{si.stockName,jdbcType=VARCHAR},#{si.openPrice,jdbcType=DECIMAL},#{si.preClosePrice,jdbcType=DECIMAL}
            ,#{si.curPrice,jdbcType=DECIMAL},#{si.maxPrice,jdbcType=DECIMAL},#{si.minPrice,jdbcType=DECIMAL}
            ,#{si.tradeAmount,jdbcType=BIGINT},#{si.tradeVolume,jdbcType=DECIMAL})
        </foreach>
    </insert>
```

#### 5、测试

![image-20230211175710469](https://p.ipic.vip/mz2laq.png)

![image-20230211180402493](https://p.ipic.vip/bvkfgw.png)

## 十六、股票大盘数据采集&插入

### 开发步骤

#### 1、数据采集准备

新浪内部接口：

```
http://vip.stock.finance.sina.com.cn/q/view/newSinaHy.php
```

响应数据格式：

```js
var S_Finance_bankuai_sinaindustry = {
"new_blhy":"new_blhy,玻璃行业,19,19.293684210526,-0.17052631578947,-0.87610188740468,315756250,5258253314,sh600586,3.464,9.260,0.310,金晶科技",
"new_cbzz":"new_cbzz,船舶制造,8,12.15875,0.0125,0.10291242152928,214866817,2282104956,sh600150,0.978,24.790,0.240,中国船舶",
  //........省略.......
}
```

思路：在使用 `RestTemplate` 拉取数据后，使用正则切割，得到标准的 `json` 数据格式。

```
"new_blhy,玻璃行业,19,19.293684210526,-0.17052631578947,-0.87610188740468,315756250,5258253314,sh600586,3.464,9.260,0.310,金晶科技"
参数语义：
['0.板块编码',1.'板块',2.'公司家数',3.'平均价格',4.'涨跌额',5.'涨跌幅',6.'总成交量',7.'总成交金额',8.'领涨股代码',9.'涨跌幅',10.'当前价',11.'涨跌额',12.'领涨股名称']
```

#### 2、定义数据采集服务接口及实现

接口：

```java
		/**
     * 获取板块数据
     */
    void getStockSectorRtIndex();
```

实现类：

```java
		/**
     * 获取板块数据
     */
    @Override
    public void getStockSectorRtIndex() {
        // 1、发送板块数据请求
        String result = restTemplate.getForObject(stockInfoConfig.getBlockUrl(), String.class);
        // 2、将响应数据转化为集合数据
        List<StockBlockRtInfo> infos = parserStockInfoUtil.parse4StockBlock(result);
        // 3、批量保存集合数据
        int insert = stockBlockRtInfoMapper.insertBatch(infos);
        if (insert > 0) {
            log.info("插入板块数据 {} 条", insert);
        }
    }
```

#### 3、定义mapper接口与xml绑定

Mapper:

```java
		/**
     * 板块信息批量插入
     * @param list
     * @return
     */
    int insertBatch(@Param("blockInfoList") List<StockBlockRtInfo> list);
```

Xml:

```xml
		<insert id="insertBatch">
        insert into stock_block_rt_info
        ( id,label,block_name
        ,company_num,avg_price,updown_rate
        ,trade_amount,trade_volume,cur_time
        )
        values
            <foreach collection="blockInfoList" item="list" separator=",">
                (#{list.id,jdbcType=VARCHAR},#{list.label,jdbcType=VARCHAR},#{list.blockName,jdbcType=VARCHAR}
                ,#{list.companyNum,jdbcType=INTEGER},#{list.avgPrice,jdbcType=DECIMAL},#{list.updownRate,jdbcType=DECIMAL}
                ,#{list.tradeAmount,jdbcType=BIGINT},#{list.tradeVolume,jdbcType=DECIMAL},#{list.curTime,jdbcType=TIMESTAMP}
                )
            </foreach>
    </insert>
```

#### 4、测试

![image-20230211193342879](https://p.ipic.vip/wypy3m.png)

![image-20230211193419655](https://p.ipic.vip/vw5g21.png)

### 数据采集存在的问题

1. 目前只是完成了瞬时的数据采集工作，需要进一步扩展到定时定量的采集任务上
2. 目前数据库批量插入数据是串行执行的，I/O成本高，需要引入多线程并发插入，提高效率
3. 线程复用和挤压问题：当前项目是单体架构，股票数据采集线程和主业务线程共享，可能会因为股票采集线程长时间占用，导致主业务线程无法正常提供有效的服务

## 十七、整合定时任务XXL-JOB框架

### 整合步骤

#### 1、导入admin工程

需要修改 `stock_parent` 的子模块，才能够识别这个 admin 工程。

![image-20230212100757693](https://p.ipic.vip/o2fck5.png)

#### 2、引入xxl-job的核心依赖

在 `stock_backend` 工程中引入依赖。

![image-20230212101017304](https://p.ipic.vip/2h8zfy.png)

#### 3、配置

先完成配置类

![image-20230212101159674](https://p.ipic.vip/ev58ep.png)

```java
/**
 * xxl-job config
 *
 * @author xuxueli 2017-04-28
 */
@Configuration
public class XxlJobConfig {
    private Logger logger = LoggerFactory.getLogger(XxlJobConfig.class);

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Value("${xxl.job.executor.appname}")
    private String appname;

    @Value("${xxl.job.executor.address}")
    private String address;

    @Value("${xxl.job.executor.ip}")
    private String ip;

    @Value("${xxl.job.executor.port}")
    private int port;

    @Value("${xxl.job.executor.logpath}")
    private String logPath;

    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;


    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        logger.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setAddress(address);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);

        return xxlJobSpringExecutor;
    }
}
```

然后在 `yml` 中配置：

```yml
# 配置 xxl-job
xxl:
  job:
    accessToken:
    admin:
      addresses: http://127.0.0.1:8082/songjian-job-admin
    executor:
      appname: songjian-stock-job-executor
      address:
      ip:
      port: 6666
      logpath: ./data/applogs/xxl-job/jobhandler
      logretentiondays: 30
```

#### 4、启动平台和任务

定义一个任务：

```java
/**
 * @projectName stock_parent
 * @package tech.songjian.stock.job
 * @className tech.songjian.stock.job.StockTimerJob
 */
package tech.songjian.stock.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

/**
 * StockTimerJob
 * @description 配置xxljob执行任务的bean对象
 * @author SongJian
 * @date 2023/2/12 10:19
 * @version
 */
@Component
public class StockTimerJob {

    @XxlJob("testXxlJob")
    public void testXxlJob() {
        System.out.println("testXxlJob Run .....");
    }
}
```

在管理平台上注册执行器和新增任务：

![image-20230212103131066](https://p.ipic.vip/is1vqy.png)

![image-20230212103020549](https://p.ipic.vip/sxwmtf.png)

启动任务测试：

![image-20230212103949996](https://p.ipic.vip/a6v3ty.png)

### 定义大盘数据采集任务

前面已经完成了大盘数据采集的功能开发，现在使用xxljob定时任务调用。

#### 1、定义任务

```java
/**
 * StockTimerJob
 * @description 配置xxljob执行任务的bean对象
 * @author SongJian
 * @date 2023/2/12 10:19
 * @version
 */
@Component
public class StockTimerJob {

    @Autowired
    private StockTimerTaskService stockTimerTaskServicel;

    /**
     * 测试
     */
    @XxlJob("testXxlJob")
    public void testXxlJob() {
        System.out.println("testXxlJob Run .....");
    }

    /**
     * 获取国内大盘数据
     * 周一至周五，上午的9：30到11：30，下午的1：00-3：00
     */
    @XxlJob("getInnerMarketInfo")
    public void getInnerMarketInfo() {
        stockTimerTaskServicel.collectInnerMarketInfo();
    }
}
```

#### 2、定义cron表达式

该任务比较复杂，我们将其拆为两个任务：

- 前半小时：每周一到周五的（10、11、13、14）时的前半小时，每间隔一分钟执行一次
- 后半小时：每周一到周五的（9、10、13、14）时的后半小时，每件歌一分钟执行一次

##### 前半小时任务

![image-20230212110526339](https://p.ipic.vip/qk93b3.png)

##### 后半小时任务

![image-20230212110507509](https://p.ipic.vip/ylync4.png)

#### 3、测试

![image-20230212111047095](https://p.ipic.vip/t5rfv7.png)

### 定义A股数据采集定时任务

同样，与上面过程相同。

### 定义大盘数据采集定时任务

与上述过程相同。

## 十八、整合线程池

在这种任务中，属于串行执行任务。数据库I/O和网络I/O成本较高。

当循环中有一个任务阻塞，后续任务都只能等待。

```java
				// 2、将股票code集合分片处理，进行均等分
        Lists.partition(stockCodeList,20).forEach(list->{
            //拼接股票url地址
            String stockUrl=stockInfoConfig.getMarketUrl()+String.join(",",list);
            //获取响应数据
            String result = restTemplate.postForObject(stockUrl, entity, String.class);
            // 解析处理, 3:表示A股股票
            List<StockRtInfo> infos = parserStockInfoUtil.parser4StockOrMarketInfo(result, 3);
            log.info("数据量：{}",infos.size());

            // 批量插入
            int inserts = stockRtInfoMapper.insertBatch(infos);
            if (inserts > 0) {
                log.info("插入股票详细数据 {} 条", inserts);
            }
        });
```

因此需要进一步优化，让其异步执行。引入多线程提高操作效率，但随之而来有两个问题：

- 当前项目是单体架构，股票数据采集线程与主业务线程共享。如果股票采集线程长时间占用CPU，**会造成主业务线程无法正常提供有效的服务（线程挤压问题）**，因此，我们需要通过线程池与主业务线程进行隔离；
- 线程频繁的常见和销毁会带来非常大的性能开销，**需要尽量提高线程的复用性**。

### 开发步骤

#### 1、配置yml

```yml
## 设置定时任务线程池基本参数
task:
  pool:
    corePoolSize: 5 # 核心线程数
    maxPoolSize: 20 # 最大线程数
    keepAliveSeconds: 300 # 空闲线程活跃时间
    queueCapacity: 100 # 设置队列容量
```

#### 2、封装实体对象

```java
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
    private Integer corePoolSize;
    private Integer maxPoolSize;
    private Integer keepAliveSeconds;
    private Integer queueCapacity;
}
```

#### 3、配置线程池

```java
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
```

#### 4、股票数据异步采集

在 `StockTimerTaskServiceImpl` 注入线程池bean：

```java
		@Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
```

修改串行代码：

```java
				// 多线程，异步执行
        Lists.partition(stockCodeList,20).forEach(list->{
            // 加入线程池后，异步多线程处理数据采集
            // 提高操作效率，但数据库IO会增加
            threadPoolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    //拼接股票url地址
                    String stockUrl=stockInfoConfig.getMarketUrl()+String.join(",",list);
                    //获取响应数据
                    String result = restTemplate.postForObject(stockUrl, entity, String.class);
                    // 解析处理, 3:表示A股股票
                    List<StockRtInfo> infos = parserStockInfoUtil.parser4StockOrMarketInfo(result, 3);
                    log.info("数据量：{}",infos.size());

                    // 批量插入
                    int inserts = stockRtInfoMapper.insertBatch(infos);
                    if (inserts > 0) {
                        log.info("插入股票详细数据 {} 条", inserts);
                    }
                }
            });
        });
```

## 十九、外盘指数显示功能开发

### 业务分析

#### 1、业务原型

<img src="https://p.ipic.vip/7vulaq.png" alt="image-20230212211748953" style="zoom:50%;" />

#### 2、接口说明

```
功能描述：外盘指数行情数据查询，根据时间和大盘点数降序排序取前4 
服务路径：/api/quot/external/index
服务方法：GET
请求参数：无
```

响应数据格式：

```json
{
    "code": 1,
    "data": [
        {
            "curPoint": 36302.38,//当前大盘点
            "curTime": "20211231",//当前日期
            "name": "道琼斯",//大盘名称
            "tradePrice": 351.82,//当前交易金额
            "updownRate": 0.98//涨幅
        },
        {
            "curPoint": 28956.65,
            "curTime": "20211231",
            "name": "日经指数",
            "tradePrice": 280.19,
            "updownRate": 0.98
        }
    ]
}
```

### 功能开发

#### 1、定义DO对象

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockExternalIndexDomain {
    /**
     * 当前点数
     */
    private BigDecimal curPoint;
    /**
     * 当前日期
     */
    private Date curTime;
    /**
     * 大盘名称
     */
    private String name;
    /**
     * 交易金额
     */
    private BigDecimal tradePrice;
    /**
     * 涨幅
     */
    private BigDecimal updownRate;
}
```

#### 2、定义web访问接口

```java
		@GetMapping("/external/index")
    public R<List<StockExternalIndexDomain>> getExternalIndexInfo() {
        return stockService.getExternalIndexInfo();
    }
```

#### 3、定义服务接口及实现类

服务接口：

```java
		/**
     * 外盘指数行情数据查询，根据时间和大盘点数降序排序取前4
     * @return
     */
    R<List<StockExternalIndexDomain>> getExternalIndexInfo();
```

实现类：

```java
		@Override
    public R<List<StockExternalIndexDomain>> getExternalIndexInfo() {
        // 1、获取国外大盘market_id
        List<String> marketIds = stockInfoConfig.getOuter();
        // 2、获取最近交易日期
        DateTime lastDate = DateTimeUtil.getLastDate4Stock(DateTime.now());
        Date date = lastDate.toDate();
        //TODO mock测试数据，后期数据通过第三方接口动态获取试试数据
        date = DateTime.parse("2022-01-02 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 3、调用 mapper 接口获取数据
        List<StockExternalIndexDomain> list = stockMarketIndexInfoMapper.getExternalIndexInfoTop4(marketIds, date);
        if (CollectionUtils.isEmpty(list)) {
            list = new ArrayList<>();
        }
        // 4、返回
        return R.ok(list);
    }
```

#### 4、定义mapper接口及xml

sql：

```sql
SELECT
	smii.cur_point AS curPoint,
	DATE_FORMAT(smii.cur_time,'%Y%m%d') AS curTime,
	smii.mark_name AS name,
	smii.current_price AS tradePrice,
	smii.updown_rate AS updownRate
FROM
	stock_market_index_info AS smii
WHERE
	smii.mark_Id IN ('int_dji', 'int_nasdaq', 'int_hangseng', 'int_nikkei', 'b_TWSE','b_FSSTI')
AND
	DATE_FORMAT('2022-01-02 12:00:00','%Y%m%d') = DATE_FORMAT(smii.cur_time,'%Y%m%d')
ORDER BY
	smii.cur_time DESC,
	smii.cur_point DESC
LIMIT 4;
```

mapper：

```java
    /**
     * 外盘指数行情数据查询，根据时间和大盘点数降序排序取前4
     * @param marketIds
     * @param date
     * @return
     */
    List<StockExternalIndexDomain> getExternalIndexInfoTop4(@Param("marketIds") List<String> marketIds, @Param("timePoint") Date timePoint);
```

Xml:

```java
		<select id="getExternalIndexInfoTop4"
            resultType="tech.songjian.stock.common.domain.StockExternalIndexDomain">
        SELECT
            smii.cur_point AS curPoint,
            DATE_FORMAT(smii.cur_time,'%Y%m%d') AS curTime,
            smii.mark_name AS name,
            smii.current_price AS tradePrice,
            smii.updown_rate AS updownRate
        FROM
            stock_market_index_info AS smii
        WHERE
            smii.mark_Id IN
            <foreach collection="marketIds" item="marketId" open="(" separator="," close=")">
                #{marketId}
            </foreach>
        AND
            DATE_FORMAT(#{timePoint},'%Y%m%d') = DATE_FORMAT(smii.cur_time,'%Y%m%d')
        ORDER BY
            smii.cur_time DESC,
            smii.cur_point DESC
            LIMIT 4
    </select>
```

#### 5、测试

![image-20230212225619260](https://p.ipic.vip/9yyv4m.png)

## 二十、股票Code联想功能开发

### 业务分析

#### 1、业务原型

<img src="https://p.ipic.vip/t5wbj9.png" alt="image-20230212230633453" style="zoom:50%;" />

输入框输入股票编码后，显示关联的股票信息;

#### 2、接口说明

接口说明：

~~~tex
功能描述：根据输入的个股代码，进行模糊查询，返回证券代码和证券名称
服务路径：/quot/stock/search
服务方法：GET
请求参数：searchStr （只接受代码模糊查询，不支持文字查询）  
~~~

响应数据格式：

~~~json
{
    "code": 1,
    "data": [
        {
            "code": "600000",//股票编码
            "name": "浦发银行" //股票名称
        },
        {
            "code": "600004",
            "name": "白云机场"
        }
    ]
}
~~~

### 功能开发

#### 1、定义访问接口

```java
		/**
     * 根据个股代码进行模糊查询
     * @param searchStr
     * @return
     */
    @GetMapping("/stock/search")
    public R<List> burSearchByCode(String searchStr) {
        return stockService.burSearchByCode(searchStr);
    }
```

#### 2、定义服务接口与实现类

```java
		/**
     * 根据个股代码进行模糊查询
     * @param searchStr
     * @return
     */
    R<List> burSearchByCode(String searchStr);
```

实现类：

```java
		@Override
    public R<List> burSearchByCode(String searchStr) {
        // 1、拼接模糊查询字符串
        searchStr = '%' + searchStr + '%';
        Date date = DateTimeUtil.getLastDate4Stock(DateTime.now()).toDate();
        // TODO mock数据
        date = DateTime.parse("2022-01-02 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        // 2、调用 mapper 查询
        List<Map> res = stockRtInfoMapper.burSearchByCode(searchStr, date);
        return R.ok(res);
    }
```

#### 3、定义mapper接口及xml

mapper：

```java
		/**
     * 根据个股代码进行模糊查询
     *
     * @param str
     * @param date
     * @return
     */
    List<Map> burSearchByCode(@Param("str") String str, @Param("date") Date date);
```

Xml:

```xml
		<select id="burSearchByCode" resultType="java.util.Map">
        SELECT
            *
        FROM
            (
                SELECT DISTINCT
                    sri.stock_code AS code,
                    sri.stock_name AS name
                FROM
                    stock_rt_info AS sri
                WHERE
                    DATE_FORMAT(sri.cur_time,'%Y%m%d') = DATE_FORMAT(#{date},'%Y%m%d')
            ) AS sri2
        WHERE
            sri2.code LIKE #{str}
    </select>
```

#### 4、测试

![image-20230212234303661](https://p.ipic.vip/7nmuu8.png)

## 二十一、个股描述功能开发

### 业务分析

#### 1、原型示意

![image-20220110170919798](https://p.ipic.vip/y2hfwm.png)

#### 2、接口说明

~~~json
功能描述：个股主营业务查询接口
服务路径：/api/quot/stock/describe
服务方法：GET
请求参数：code #股票编码
~~~

响应参数：

~~~json
{
    "code": 1,
    "data": {
        "code": "000002", //股票编码
        "trade": "房地产  ", //行业，也就是行业板块名称
        "business": "房地产开发和物业服务",//公司主营业务
        "name": "万科Ａ" //公司名称
    }
}
~~~

### 功能开发

#### 1、定义web访问接口

```java
		/**
     * 根据股票编码查询个股主营业务
     * @param code
     * @return
     */
    @GetMapping("/stock/describe")
    public R<StockBusinessDomain> getStockBusinessByCode(String code) {
        return stockService.getStockBusinessByCode(code);
    }
```

#### 2、定义服务接口及实现类

```java
		/**
     * 根据股票编码查询个股主营业务
     * @param code
     * @return
     */
    @Override
    public R<StockBusinessDomain> getStockBusinessByCode(String code) {
        StockBusinessDomain stockBusinesses = stockBusinessMapper.getStockBusinessByCode(code);
        if (stockBusinesses == null) {
            return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
        }
        return R.ok(stockBusinesses);
    }
```

#### 3、定义mapper接口与实现类

Mapper:

```
/**
 * 根据股票代码查询个股业务
 * @param code
 * @return
 */
StockBusinessDomain getStockBusinessByCode(@Param("code") String code);
```

xml:

```xml
<select id="getStockBusinessByCode" resultType="tech.songjian.stock.common.domain.StockBusinessDomain">
    SELECT
        sb.sec_code AS code,
        sb.sec_name AS name,
        sb.business AS business,
        sb.sector_name AS trade
    FROM
        stock_business AS sb
    WHERE
        sb.sec_code = #{code}
</select>
```

#### 4、测试

![image-20230213001417697](https://p.ipic.vip/w0johg.png)

## 二十二、个股周K线功能开发

### 业务分析

#### 1、业务原型

<img src="https://p.ipic.vip/amrae8.png" alt="image-20230213114322304" style="zoom:50%;" />

<img src="https://p.ipic.vip/d8bzcj.png" alt="image-20230213114336904" style="zoom:50%;" />

#### 2、接口分析

~~~tex
个股周K线数据主要包含：
	股票ID、 一周内最高价、 一周内最低价 、周1开盘价、周5的收盘价、
	整周均价、以及一周内最大交易日期（一般是周五所对应日期）
~~~

接口要求：

~~~json
功能描述：统计每周内的股票数据信息，信息包含：
	股票ID、 一周内最高价、 一周内最低价 、周1开盘价、周5的收盘价、
	整周均价、以及一周内最大交易日期（一般是周五所对应日期）;
服务路径：/api/quot/stock/screen/weekkline
服务方法：GET
请求参数：code //股票编码
~~~

响应数据格式：

~~~json
{
    "code": 1,
    "data": [
        {
            "avgPrice": 8.574954,//一周内平均价
            "minPrice": 8.56,//一周内最低价
            "openPrice": 8.6,//周一开盘价
            "maxPrice": 8.6,//一周内最高价
            "closePrice": 8.57,//周五收盘价（如果当前日期不到周五，则显示最新价格）
            "mxTime": "20211219",//一周内最大时间
            "stock_code": "600000"//股票编码
        },
      .......
    ]
}
~~~

### 功能开发

#### 1、定义访问接口

```java
/**
 * 个股周K线展示
 * @param code
 * @return
 */
@GetMapping("/stock/screen/weekkline")
public R<List<WeeklineDomain>> getRtStockWeekline(String code){
    return stockService.getRtStockWeekline(code);
```

#### 2、定义服务接口及实现类

```java
/**
 * 个股周K线展示
 * @param code
 * @return
 */
R<List<WeeklineDomain>> getRtStockWeekline(String code);
```

实现类：

```java
/**
 * 个股周K线展示
 * @param code
 * @return
 */
@Override
public R<List<WeeklineDomain>> getRtStockWeekline(String code) {
    // 1 调用mapper层方法
    List<WeeklineDomain> result = stockRtInfoMapper.getRtStockWeekline(code);
    // 2 判断并返回结果
    if(CollectionUtils.isEmpty(result)){
        return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
    }
    return R.ok(result);
}
```

#### 3、定义mapper接口及xml

这里的难点在于sql的编写，需要使用到内连接

mapper：

```java
/**
 * 按照周分组查询
 * @param code
 * @return
 */
List<WeeklineDomain> getRtStockWeekline(String code);
```

xml：

```xml
<select id="getRtStockWeekline" resultType="tech.songjian.stock.common.domain.WeeklineDomain">
    SELECT
        tmp.avgPrice as avgPrice,
        tmp.minPrice as minPrice,
        sri1.open_price as openPrice,
        tmp.maxPrice as maxPrice,
        sri2.cur_price as closePrice,
        DATE_FORMAT(tmp.maxDate,'%Y%m%d') as mxTime,
        tmp.code as stock_code
    from stock_rt_info sri1
    inner join stock_rt_info sri2
    inner join (select
                    min(sri.cur_time) as minDate,
                    max( sri.cur_time ) as maxDate,
                    max(cur_price) as maxPrice,
                    avg(cur_price) as avgPrice,
                    min(cur_price) as minPrice,
                    stock_code as code
                from
                    stock_rt_info as sri
                where
                    sri.stock_code =#{code}
                group by
                    date_format( sri.cur_time, '%y%U')
    ) tmp
    on sri1.stock_code = tmp.code and sri1.cur_time =tmp.minDate and sri2.stock_code =tmp.code and sri2.cur_time = tmp.maxDate;
</select>
```

#### 4、测试

![image-20230213114758646](https://p.ipic.vip/c8pfg6.png)

## 二十三、个股最新分时行情数据

### 业务说明

#### 1、个股最新分时行情功能原型

![image-20220103164639283](https://p.ipic.vip/3obm7e.png)

#### 2、个股最新分时行情数据接口分析

~~~json
功能描述：
	获取个股最新分时行情数据，主要包含：
	开盘价、前收盘价、最新价、最高价、最低价、成交金额和成交量、交易时间信息; 
服务路径：/api/quot/stock/screen/second/detail
服务方法：GET
请求参数：code //股票编码
请求频率：每分钟
~~~

响应数据格式：

~~~json
{
    "code": 1,
    "data": {
        "tradeAmt": 58672751,//最新交易量
        "preClosePrice": 3.89,//前收盘价格
        "lowPrice": 3.89,//最低价
        "highPrice": 3.91,//最高价
        "openPrice": 3.9,//开盘价
        "tradeVol": 228625157,//交易金额
        "tradePrice": 3.9//当前价格
        "curDate": '202201031458'//当前日期
    }
}
~~~

注意事项：

~~~tex
如果当前日期不在股票交易时间内，则查询最近的股票交易时间的数据回显
~~~

### 功能开发

#### 0、定义DO

```java
/**
 * StockDetailSecDomain
 * @description 个股最新分时数据domain
 * @author SongJian
 * @date 2023/2/13 14:04
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockDetailSecDomain {
    //最新交易量
    private Long tradeAmt;
    //前收盘价格
    private BigDecimal preClosePrice;
    //最低价
    private BigDecimal lowPrice;
    //最高价
    private BigDecimal highPrice;
    //开盘价
    private BigDecimal openPrice;
    //交易金额
    private BigDecimal tradeVol;
    //当前价格
    private BigDecimal tradePrice;
    //当前日期
    private String curDate;
}
```

#### 1、定义web访问接口

```java
/**
 * 获取个股最新分时行情数据，主要包含：
 * 开盘价、前收盘价、最新价、最高价、最低价、成交金额和成交量、交易时间信息;
 * @return
 */
@GetMapping("/stock/screen/second/detail")
public R<StockDetailSecDomain> getStockDetailsByCode(String code) {
    return stockService.getStockDetailsByCode(code);
}
```

#### 2、定义服务接口及实现类

```java
/**
 * 获取个股最新分时行情数据，主要包含：
 * 开盘价、前收盘价、最新价、最高价、最低价、成交金额和成交量、交易时间信息;
 * @return
 */
R<StockDetailSecDomain> getStockDetailsByCode(String code);
```

实现类：

```java
/**
 * 获取个股最新分时行情数据，主要包含：
 * 开盘价、前收盘价、最新价、最高价、最低价、成交金额和成交量、交易时间信息;
 * @return
 */
@Override
public R<StockDetailSecDomain> getStockDetailsByCode(String code) {
    // 获取当前时间
    DateTime lastDate4Stock = DateTimeUtil.getLastDate4Stock(DateTime.now());
    Date date = lastDate4Stock.toDate();
    // TODO mock数据
    date = DateTime.parse("2021-12-30 09:32:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
    // 调用mapper接口获取数据
    StockDetailSecDomain details = stockRtInfoMapper.getStockDetailsByCode(code, date);
    if (details == null) {
        return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
    }
    return R.ok(details);
}
```

#### 3、定义mapper及xml

```java
/**
 * 获取个股最新分时行情数据，主要包含：
 * 开盘价、前收盘价、最新价、最高价、最低价、成交金额和成交量、交易时间信息;
 * @return
 */
StockDetailSecDomain getStockDetailsByCode(@Param("code") String code, @Param("date") Date date);
```

Xml:

```xml
<select id="getStockDetailsByCode" resultType="tech.songjian.stock.common.domain.StockDetailSecDomain">
    SELECT
        sri.trade_amount AS tradeAmt,
        sri.pre_close_price AS preClosePrice,
        sri.min_price AS lowPrice,
        sri.max_price AS highPrice,
        sri.open_price AS openPrice,
        sri.trade_volume AS tradeVol,
        sri.cur_price AS tradePrice,
        DATE_FORMAT(sri.cur_time,'%Y%m%d%H%i') AS curDate
    FROM
        stock_rt_info AS sri
    WHERE
        sri.stock_code = #{code}
      AND
        sri.cur_time = #{date}
</select>
```

#### 4、测试

![image-20230213141826998](https://p.ipic.vip/y3lgw0.png)

## 二十四、个股实时交易流水查询

### 业务分析

#### 1、功能原型

![image-20220103170258505](https://p.ipic.vip/kt3gfj.png)

#### 2、功能接口说明

~~~tex
功能描述：个股交易流水行情数据查询--查询最新交易流水，按照交易时间降序取前10
服务路径：/quot/stock/screen/second
服务方法：GET
请求频率：5秒
~~~

响应数据格式：

~~~json
{
    "code": 1,
    "data": [
        {
            "date": "202201031458",//当前时间，精确到分
            "tradeAmt": 58672751,//交易量
            "tradeVol": 228625157,//交易金额
            "tradePrice": 3.9//交易价格
        }
    ]
}
~~~

### 功能开发

#### 1、DO对象

```java
/**
 * StockTradeSecDomain
 * @description 股票交易流水domain
 * @author SongJian
 * @date 2023/2/13 14:27
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTradeSecDomain {
    //当前时间，精确到分
    private String date;
    //交易量
    private Long tradeAmt;
    //交易金额
    private BigDecimal tradeVol;
    //交易价格
    private BigDecimal tradePrice;
}
```

#### 2、定义web访问接口

```java
/**
 * 个股交易流水行情数据查询--查询最新交易流水，按照交易时间降序取前10
 * @return
 */
@GetMapping("/stock/screen/second")
public R<List<StockTradeSecDomain>> getStockTradeSec(String code) {
    return stockService.getStockTradeSec(code);
}
```

#### 3、定义服务层接口及实现类

```java
/**
 * 个股交易流水行情数据查询--查询最新交易流水，按照交易时间降序取前10
 * @return
 */
R<List<StockTradeSecDomain>> getStockTradeSec(String code);
```

实现类：

```java
/**
 * 个股交易流水行情数据查询--查询最新交易流水，按照交易时间降序取前10
 * @return
 */
@Override
public R<List<StockTradeSecDomain>> getStockTradeSec(String code) {
    List<StockTradeSecDomain> details = stockRtInfoMapper.getStockTradeSec(code);
    // log.info(details.toString());
    if (details == null) {
        return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
    }
    return R.ok(details);
}
```

#### 4、定义mapper及xml

```java
/**
 * 个股交易流水行情数据查询--查询最新交易流水，按照交易时间降序取前10
 * @return
 */
List<StockTradeSecDomain> getStockTradeSec(String code);
```

Xml：

```xml
<select id="getStockTradeSec" resultType="tech.songjian.stock.common.domain.StockTradeSecDomain">
    SELECT
        sri.trade_amount AS tradeAmt,
        sri.trade_volume AS tradeVol,
        sri.cur_price AS tradePrice,
        DATE_FORMAT(sri.cur_time,'%Y%m%d%H%i') AS date
    FROM
        stock_rt_info AS sri
    WHERE
        sri.stock_code = #{code}
    ORDER BY
        date DESC
    LIMIT
        10
</select>
```

#### 5、测试

![image-20230213144906439](https://p.ipic.vip/nesd06.png)

## 二十五、外盘数据采集功能实现

### 业务分析

国外大盘数据采集与国内大盘数据几乎一致，目前通过sina接口无法获取国外大盘的交易量和交易金额数据，所以针对国外大盘数据，需要单独处理。

注意事项：

> 国外大盘数据接口不提供交易量和交易金额的信息；

### 功能开发

在 `StockTimerTaskService` 中定义接口

```java
/**
 * 采集国外大盘数据信息
 */
void collectOuterMarketInfo();
```

实现：

```java
/**
 * 采集国外大盘数据信息
 *
 */
@Override
public void collectOuterMarketInfo() {
    // 1、定义采集的url接口，生成完整的url地址
    String marketUrl = stockInfoConfig.getMarketUrl() + String.join(",", stockInfoConfig.getOuter());
    // 2、调用 restTemplate 采集数据
    // 2.1、组装请求头
    HttpHeaders headers = new HttpHeaders();
    headers.add("Referer", "https://finance.sina.com.cn/stock/");
    headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
    // 2.2、组装请求对象
    HttpEntity<Object> entity = new HttpEntity<>(headers);
    // 2.3、发起请求
    String result = restTemplate.postForObject(marketUrl, entity, String.class);
    log.info(result);

    // 3、正则匹配
    String reg="var hq_str_(.+)=\"(.+)\";";
    // 3.2、正则匹配
    Pattern pattern = Pattern.compile(reg);
    Matcher matcher = pattern.matcher(result);
    // 3.3、收集大盘封装后后的对象
    List<StockMarketIndexInfo> list = new ArrayList<>();
    while (matcher.find()) {
        // 获取大盘的id
        String marketCode = matcher.group(1);
        // 其它信息
        String other = matcher.group(2);
        String[] others = other.split(",");
        // 大盘名称
        String marketName = others[0];
        // 当前点
        BigDecimal curPoint = new BigDecimal(others[1]);
        // 当前价格
        BigDecimal curPrice = new BigDecimal(others[2]);
        // 涨跌率
        BigDecimal upDownRate = new BigDecimal(others[3]);
        // 成交量
        Long tradeAmount = null;
        // 成交金额
        Long tradeVol = null;
        // 当前日期
        Date now = DateTimeUtil.getDateTimeWithoutSecond(DateTime.now()).toDate();

        //封装对象
        StockMarketIndexInfo stockMarketIndexInfo = StockMarketIndexInfo.builder()
                .id(idWorker.nextId()+"")
                .markName(marketName)
                .tradeVolume(tradeVol)
                .tradeAccount(tradeAmount)
                .updownRate(upDownRate)
                .curTime(now)
                .curPoint(curPoint)
                .currentPrice(curPrice)
                .markId(marketCode)
                .build();

        // 添加集合
        list.add(stockMarketIndexInfo);
    }
    log.info("集合长度：{}，内容：{}", list.size(), list);
    if (CollectionUtils.isEmpty(list)) {
        log.info("");
        return;
    }
    String curTime = DateTime.now().toString(DateTimeFormat.forPattern("yyyyMMddHHmmss"));
    log.info("采集的大盘数据：{},当前时间：{}",list,curTime);

    int count = stockMarketIndexInfoMapper.insertBatch(list);
    log.info("批量插入 {} 条大盘数据", count);
}
```

测试：

![image-20230213150805281](https://p.ipic.vip/0xkt96.png)

# 用户管理后端开发

## 一、用户权限详情完善登入功能

### 业务分析

#### 1、功能接口说明

~~~tex
功能描述：当前用户登录后，仅仅加载了用户表相关信息，接下来完成的功能是完善用户权限相关的信息；
服务路径：/api/login
请求方式：POST
~~~

接口响应数据格式：

~~~java
{
    "code": 1,
    "data": {
        "id": "1237361915165020161",//用户ID
        "username": "admin",//用户名称
        "phone": "13888888888",//手机号
        "nickName": "itheima",//昵称
        "realName": "heima",//真实名称
        "sex": 1,//性别
        "status": 1,//装填
        "email": "875267425@qq.com",//邮件
        "menus": [//权限树（包含按钮权限）
            {
                "id": "1236916745927790564",//权限ID
                "title": "组织管理",//权限标题
                "icon": "el-icon-star-off",//权限图标（按钮权限无图片）
                "path": "/org",//请求地址
                "name": "org",//权限名称对应前端vue组件名称
                "children": [
                    {
                        "id": "1236916745927790578",
                        "title": "角色管理",
                        "icon": "el-icon-s-promotion",
                        "path": "/roles",
                        "name": "roles",
                        "children": []
                    },
                    {
                        "id": "1236916745927790560",
                        "title": "菜单权限管理",
                        "icon": "el-icon-s-tools",
                        "path": "/menus",
                        "name": "menus",
                        "children": []
                    }
                ]
            },
            {
                "id": "1236916745927790569",
                "title": "账号管理",
                "icon": "el-icon-s-data",
                "path": "/user",
                "name": "user",
                "children": []
            }
        ],
        "permissions": [//按钮权限集合
            "sys:log:delete",//按钮权限security标识
            "sys:user:add",
            "sys:role:update",
            "sys:dept:list"
        ]
    }
}
~~~

提示：用户权限信息先批量查询，然后再通过递归组装数据；

#### 2、表分析

本功能涉及多张表：`sys_user_role`，`sys_role`，`sys_role_permission`，`sys_permission`

需要通过用户id查询角色id，角色id 查询权限id，最后查询权限信息。

### 功能开发

#### 1、定义DO

权限DO，在响应数据中需要封装此对象

```java
/**
 * PermissionDomain
 * @description 权限domain
 * @author SongJian
 * @date 2023/2/13 16:06
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDomain {
    private String id; // 权限id
    private String title; // 权限标题
    private String icon; // 权限图标
    private String path; // 请求地址
    private String name; // 权限名称对应前端vue组件名称
    private List<PermissionDomain> children; // 此id对应的子权限的集合
}
```

#### 2、定义VO

因为封装的数据发生了变化，因此之前的 `LoginVo` 需要进行修改，修改后 `NewLoginVo`

```java
/**
 * newLoginReqVo
 * @description 带权限信息的登入响应vo
 * @author SongJian
 * @date 2023/2/13 16:05
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewLoginReqVo {
    private String id; // 用户id
    private String username; // 用户名称
    private String phone; // 手机号
    private String nickName; // 昵称
    private String realName;// 真实名称
    private Integer sex; // 性别
    private Integer status; // 状态
    private String email; // 邮箱
    private List<PermissionDomain> menus; //权限树
    private List<String> permissions; // 按钮权限集合
}
```

#### 3、定义访问接口

```java
/**
 * 当前用户登录后，仅仅加载了用户表相关信息，接下来完成的功能是完善用户权限相关的信息；
 * @param vo
 * @return
 */
@PostMapping("/login")
public R<NewLoginReqVo> login(@RequestBody LoginReqVo vo){
    return userService.login(vo);
}
```

#### 4、定义服务接口及实现类

```java
/**
     * 用户登录功能实现
     * 功能描述：当前用户登录后，仅仅加载了用户表相关信息，接下来完成的功能是完善用户权限相关的信息；
     * @param vo
     * @return
     */
    @Override
    public R<NewLoginReqVo> login(LoginReqVo vo) {
        // 1.判断vo是否存在 或者 用户名是否存在 或者 密码是否存在 或者 验证码是否存在 或者 rkey 是否存在
        if (vo == null || Strings.isNullOrEmpty(vo.getUsername()) || Strings.isNullOrEmpty(vo.getPassword())
            || Strings.isNullOrEmpty(vo.getCode()) || Strings.isNullOrEmpty(vo.getRkey())) {
            return R.error(ResponseCode.DATA_ERROR.getMessage());
        }

        // 1.1 获取校验验证码
        String redisCode = (String) redisTemplate.opsForValue().get(vo.getRkey());
        // 1.2 校验
        if (redisCode == null || !redisCode.equals(vo.getCode())) {
            return R.error(ResponseCode.DATA_ERROR.getMessage());
        }
        // 1.3 redis删除key
        redisTemplate.delete(vo.getRkey());

        // 2、根据用户名查询用户权限相关信息
        SysUser user = sysUserMapper.getUserPermissionInfo(vo.getUsername());

        // 将 user 中查询到的数据封装到 newLoginResVo 中
        NewLoginReqVo result = new NewLoginReqVo();
        BeanUtils.copyProperties(user, result);

        // 将数据中的按钮权限封装到result中
        List<String> permissions = new ArrayList<>();
        List<SysPermission> sysPermissions = user.getPermissions();
        for (SysPermission p : sysPermissions) {
            if (!Strings.isNullOrEmpty(p.getPerms())) {
                permissions.add(p.getPerms());
            }
        }
        result.setPermissions(permissions);

        // 处理权限树
        // 获取跟节点，即父节点
        List<PermissionDomain> menus = user.getPermissions().stream().filter(s -> "0".equals(s.getPid())).map(item -> {
            PermissionDomain permissionDomain = new PermissionDomain();
            permissionDomain.setId(item.getId());
            permissionDomain.setName(item.getName());
            permissionDomain.setIcon(item.getIcon());
            permissionDomain.setTitle(item.getTitle());
            permissionDomain.setPath(item.getUrl());
            // 获取 SysPermission 类型的子权限树 传入的参数是 根节点的 id 和 采集到的 syspermission 类型数据
            List<SysPermission> childMenus = getChildMenus(item.getId(), user.getPermissions());
            item.setChildren(childMenus);
            // 拷贝子权限树 类型转换 将 syspermission 类型的权限树 转换为 前端需要的 permissionDomain
            permissionDomain.setChildren(new ArrayList<>());
            permissionDomain.setChildren(copyChildren(item.getChildren(), permissionDomain.getChildren()));
            return permissionDomain;
        }).collect(Collectors.toList());
        result.setMenus(menus);
        return R.ok(result);
   }
```

因为需要递归创建 `children`：

```java
/**
 * 递归获取子权限
 * @param id 父权限id
 * @param permissions 权限集合
 * @return
 */
private List<SysPermission> getChildMenus(String id, List<SysPermission> permissions) {
    // 创建容器，存放权限
    List<SysPermission> children = new ArrayList<>();
    // 根据传过来的父权限id查询所有子权限
    permissions.forEach(s -> {
        if (id.equals(s.getPid())) {
            children.add(s);
        }
    });
    // 对每个子孩子进行递归
    children.forEach(s -> {
        s.setChildren(getChildMenus(s.getId(), permissions));
    });
    return children;
}
```

同样，做类型转换时，也需要用到递归拷贝 `children`

```java
/**
 * 拷贝子权限树进行类型转换
 * @param source SysPermission
 * @param object PermissionDomain
 * @return
 */
private List<PermissionDomain> copyChildren(List<SysPermission> source, List<PermissionDomain> object) {
    if (source != null && source.size() != 0) {
        for (SysPermission s : source) {
            PermissionDomain p = new PermissionDomain();
            p.setChildren(new ArrayList<PermissionDomain>());
            // 类型转换
            p.setId(s.getId());
            p.setTitle(s.getTitle());
            p.setIcon(s.getIcon());
            p.setPath(s.getUrl());
            p.setName(s.getName());
            object.add(p);
            // 判断子权限是否还有子权限，有的话，需要递归拷贝
            if (s.getChildren().size() != 0) {
                copyChildren(s.getChildren(), p.getChildren());
            }
        }
    }
    return object;
}
```

#### 5、定义mapper及xml

mapper：

```java
/**
 * 根据用户名查询用户信息和权限信息
 * @param username
 * @return
 */
SysUser getUserPermissionInfo(String username);
```

```xml
<!--
    登录时 根据用户名查询 用户和权限信息
-->
 <resultMap id="getUserPermissionInfoMap" type="tech.songjian.stock.pojo.SysUser" autoMapping="true">
     <id column="id" property="id"/>
     <collection property="permissions" javaType="List" ofType="tech.songjian.stock.pojo.SysPermission"
                 autoMapping="true">
         <id column="pid" property="id"/>
         <result column="pName" property="name"/>
         <result column="ppid" property="pid"/>
         <result column="pStatus" property="status"/>
     </collection>
 </resultMap>
 <select id="getUserPermissionInfo" resultMap="getUserPermissionInfoMap">
     select u.id,
            u.username,
            u.phone,
            u.nick_name nickName,
            u.real_name realName,
            u.sex,
            u.status,
            u.email,
            p.id        pid,
            p.title,
            p.icon,
            p.url       path,
            p.name      pName,
            p.pid       ppid,
            p.status    pStatus,
            p.perms
     from sys_user u
              inner join sys_user_role ur on u.id = ur.user_id
              inner join sys_role r on ur.role_id = r.id
              inner join sys_role_permission rp on r.id = rp.role_id
              inner join sys_permission p on rp.permission_id = p.id
     where u.username = #{username};
 </select>
```

#### 6、测试

![image-20230213164658177](https://p.ipic.vip/3vsgfr.png)

## 二、多条件综合查询功能开发

### 业务说明

#### 1、原型效果

![1642413277524](https://p.ipic.vip/t609y5.png)

#### 2、接口说明

```
功能描述：多条件综合查询用户分页信息，条件包含：分页信息 用户创建日期范围
服务路径：/api/users
服务方法：Post
```

请求参数格式：

```
{
	"pageNum":"1",
	"pageSize":"20",
	"username":"",
	"nickName":"",
	"startTime":"",
	"endTime":""
}
```

响应数据格式：

```
{
    "code": 1,
    "data": {
        "totalRows": 12,
        "totalPages": 1,
        "pageNum": 1,
        "pageSize": 20,
        "size": 12,
        "rows": [
            {
                "id": 1237361915165020161,
                "username": "admin",
                "password": "$2a$10$JqoiFCw4LUj184ghgynYp.4kW5BVeAZYjKqu7xEKceTaq7X3o4I4W",
                "phone": "13888888888",
                "realName": "小池",
                "nickName": "超级管理员",
                "email": "875267425@qq.com",
                "status": 1,
                "sex": 1,
                "deleted": 1,
                "createId": null,
                "updateId": "1237361915165020161",
                "createWhere": 1,
                "createTime": "2019-09-22T11:38:05.000+00:00",
                "updateTime": "2020-04-07T10:08:52.000+00:00",
                "createUserName": null,
                "updateUserName": "admin"
            },
      		//.....
        ]
    }
}
```

### 功能开发

#### 0、定义请求和响应VO

`ConditionQueryUserResp` 类：

```java
/**
 * ConditionQueryUserResp
 * @description 多条件查询用户VO
 * @author SongJian
 * @date 2023/2/13 17:09
 * @version
 */
@Data
public class ConditionQueryUserResp {

    private Long totalRows;
    private Integer totalPages;
    private Integer pageNum;
    private Integer pageSize;
    private Integer size;
    private List<SysUser> rows;
}
```

`ConditionalQueryUserReq` 类：

```java
/**
 * ConditionQueryUserReq
 * @description 多条件查询用户信息请求vo
 * @author SongJian
 * @date 2023/2/13 17:11
 * @version
 */
@Data
public class ConditionalQueryUserReq {
    private String pageNum;
    private String pageSize;
    private String username;
    private String nickName;
    private String startTime;
    private String endTime;
}
```

#### 1、定义web访问接口

```java
/**
 * 多条件查询用户信息
 * @param req
 * @return
 */
@PostMapping("/users")
public R<ConditionQueryUserResp> conditionQueryUser(@RequestBody ConditionalQueryUserReq req) {
    return userService.conditionQueryUser(req);
}
```

#### 2、定义服务接口及实现类

```java
/**
 * 多条件查询用户信息
 * @param req
 * @return
 */
R<ConditionQueryUserResp> conditionQueryUser(ConditionalQueryUserReq req);
```

实现类：

使用 `PageHelper` 进行分页。

```java
/**
 * 多条件查询用户信息
 * @param req
 * @return
 */
@Override
public R<ConditionQueryUserResp> conditionQueryUser(ConditionalQueryUserReq req) {

    // 调用mapper接口
    int page = (Integer.parseInt(req.getPageNum()));
    int pageSize = Integer.parseInt(req.getPageSize());
    PageHelper.startPage(page, pageSize);

    List<SysUser> users = sysUserMapper.conditionQueryUser(req);
    ConditionQueryUserResp resp = new ConditionQueryUserResp();

    // 分页信息处理
    PageInfo<SysUser> pageInfo = new PageInfo<>(users);

    // 总条数
    resp.setTotalRows(pageInfo.getTotal());
    // 总页数
    resp.setTotalPages(pageInfo.getPages());
    // 当前页号
    resp.setPageNum(page);
    // 当前页大小
    resp.setPageSize(pageSize);
    // 当前页条数
    resp.setSize(users.size());
    // 信息
    resp.setRows(users);
    return R.ok(resp);
}
```

#### 3、定义mapper及xml

```java
/**
 * 多条件综合查询用户分页信息，条件包含：分页信息 用户创建日期范围
 * @param req
 * @return
 */
List<SysUser> conditionQueryUser(@Param("req") ConditionalQueryUserReq req);
```

xml：

```xml
<select id="conditionQueryUser" resultType="tech.songjian.stock.pojo.SysUser">
    SELECT
    u1.*,
    u2.username AS createUserName,
    u3.username AS updateUserName
    FROM
    sys_user u1
    LEFT JOIN sys_user u2 on u1.create_id = u2.id
    LEFT JOIN sys_user u3 on u1.update_id = u3.id
    <where>
        <if test="req.username!=null and req.username!=''">
            u1.username = #{req.username}
        </if>
        <if test="req.nickName!=null and req.nickName!=''">
            and u1.nick_name = #{req.nickName}
        </if>
        <if test="req.startTime!=null and req.startTime.trim()!=''">
            and u1.create_time &gt;=#{req.startTime}
        </if>
        <if test="req.endTime!=null and req.endTime.trim()!=''">
            and u1.create_time &lt;=#{req.endTime}
        </if>
    </where>
</select>
```

#### 4、测试

![image-20230213194554660](https://p.ipic.vip/43ccxp.png)

## 三、添加用户接口开发

### 业务分析

#### 1、业务原型

![1642413481274](https://p.ipic.vip/57v881.png)

#### 2、接口说明

```
功能描述：添加用户信息
服务路径：/api/users
服务方法：Post
```

请求参数格式：

```
{
	"username":"mike",
	"password":"20",
	"phone":"15367945613",
	"email":"123@qwe.cn",
	"nickName":"jane",
	"realName":"kangkang",
	"sex":"1",
	"createWhere":"1",
	"status":"1"
}
```

响应参数格式：

```
{
    "code": 1,
    "msg": "操作成功"
}
```

### 功能开发

#### 1、定义访问接口

```java
/**
 * 添加用户信息
 * @param adduser
 * @return
 */
@PostMapping("/user")
public R addUsers(@RequestBody SysUser adduser){
    return userService.addUsers(adduser);
}
```

#### 2、定义服务接口及实现类

```java
/**
 * 添加用户信息
 * @param adduser
 * @return
 */
@Override
public R addUsers(SysUser adduser) {
    // 调用mapper层方法
    // 全局主键设置
    adduser.setId(new IdWorker().nextId() + "");
    // 密码加密
    adduser.setPassword(passwordEncoder.encode(adduser.getPassword()));
    // 设置创建时间
    adduser.setCreateTime(DateTime.now().toDate());
    int insert = sysUserMapper.insertUser(adduser);
    if (insert == 0) {
        return R.error(ResponseCode.ERROR.getMessage());
    }
    return R.ok(ResponseCode.SUCCESS.getMessage());
}
```

#### 3、定义mapper接口及xml

mapper：

```java
/**
 * 添加用户
 * @param user
 * @return
 */
int insertUser(@Param("user") SysUser user);
```

Xml:

```xml
<insert id="insertUser">
    insert into sys_user
    ( id,username,password
    ,phone,real_name,nick_name
    ,email,status,sex
    ,deleted,create_id,update_id
    ,create_where,create_time,update_time
    )
    values (#{user.id,jdbcType=VARCHAR},#{user.username,jdbcType=VARCHAR},#{user.password,jdbcType=VARCHAR}
           ,#{user.phone,jdbcType=VARCHAR},#{user.realName,jdbcType=VARCHAR},#{user.nickName,jdbcType=VARCHAR}
           ,#{user.email,jdbcType=VARCHAR},#{user.status,jdbcType=TINYINT},#{user.sex,jdbcType=TINYINT}
           ,#{user.deleted,jdbcType=TINYINT},#{user.createId,jdbcType=VARCHAR},#{user.updateId,jdbcType=VARCHAR}
           ,#{user.createWhere,jdbcType=TINYINT},#{user.createTime,jdbcType=TIMESTAMP},#{user.updateTime,jdbcType=TIMESTAMP}
           )
</insert>
```

#### 4、测试

新增成功

![image-20230213195450766](https://p.ipic.vip/cfpeac.png)

## 四、获取用户的角色信息开发

### 业务分析

#### 1、原型效果

![1644398752969](https://p.ipic.vip/iotxye.png)

#### 2、接口说明

```
功能描述：获取用户具有的角色信息，以及所有角色信息
服务路径：/user/roles/{userId}
服务方法：Get
请求参数：String userId
```

响应参数格式：

```json
{
"code": 1,
"data": {
      "ownRoleIds": [
          1237258113002901515
      ],
        "allRole": [
        {
            "id": 1237258113002901512,
            "name": "超级管理员",
            "description": "我是超级管理员",
            "status": 1,
            "createTime": "2020-01-06T15:37:45.000+00:00",
            "updateTime": "2021-12-09T23:08:02.000+00:00",
            "deleted": 1
        },
        {
            "id": 1237258113002901513,
            "name": "标记用户角色测试",
            "description": "标记用户角色测试",
            "status": 1,
            "createTime": "2020-01-08T02:53:35.000+00:00",
            "updateTime": "2021-12-28T10:16:21.000+00:00",
            "deleted": 1
        },
       //..............
        
    		]
		}
}
```

### 功能开发

#### 1、定义DO

```java
/**
 * OwnRoleAndAllRoleIdsDomain
 * @description 用户角色信息domain
 * @author SongJian
 * @date 2023/2/13 20:05
 * @version
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OwnRoleAndAllRoleIdsDomain {
    // 用户具有的角色id
    private List<String> ownRoleIds;
    // 所有角色的信息
    private List<SysRole> allRole;
}
```

#### 2、定义访问接口

```java
/**
 * RoleController
 * @description 角色
 * @author SongJian
 * @date 2023/2/13 19:58
 * @version
 */
@RestController
@RequestMapping("/api/user")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取用户具有的角色信息，以及所有角色信息
     * @param userId
     * @return
     */
    @GetMapping("/roles/{userId}")
    public R<OwnRoleAndAllRoleIdsDomain> getUsersRoles(@PathVariable String userId) {
        return roleService.getUsersRoles(userId);
    }
}
```

#### 3、定义服务接口及实现类

```java
@Service("roleService")
public class RoleServiceImpl implements RoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    /**
     * 获取用户具有的角色信息，以及所有角色信息
     * @param userId
     * @return
     */
    @Override
    public R<OwnRoleAndAllRoleIdsDomain> getUsersRoles(String userId) {
        OwnRoleAndAllRoleIdsDomain domain = new OwnRoleAndAllRoleIdsDomain();
        List<SysRole> allRoles = sysRoleMapper.getAllRoles();
        List<String> ids = sysUserRoleMapper.queryRolesById(userId);
        domain.setAllRole(allRoles);
        domain.setOwnRoleIds(ids);
        return R.ok(domain);
    }
}
```

#### 4、定义mapper及xml

`SysRoleMapper`：

```java
/**
 * 查询所有角色
 * @return
 */
List<SysRole> getAllRoles();
```

```xml
<select id="getAllRoles" resultType="tech.songjian.stock.pojo.SysRole">
    SELECT
        sr.id AS id,
        sr.name AS name,
        sr.description AS description,
        sr.status AS status,
        sr.create_time AS createTime,
        sr.update_time AS updateTime,
        sr.deleted AS deleted
    FROM
        sys_role AS sr;
</select>
```

`SysUserRoleMapper`：

```java
/**
 * 根据用户id查询角色id
 * @param userId
 * @return
 */
List<String> queryRolesById(@Param("userId") String userId);
```

```xml
<select id="queryRolesById" resultType="java.lang.String">
    SELECT
        sur.role_id
    FROM
        sys_user_role AS sur
    WHERE
        sur.user_id = #{userId}
</select>
```

#### 5、测试

![image-20230213202923743](https://p.ipic.vip/87yaht.png)

## 五、更新用户角色信息开发

### 业务说明

#### 1、原型效果

![1644398784145](https://p.ipic.vip/qiypkt.png)

#### 2、接口说明

```
功能描述：更新用户角色信息
服务路径：/user/roles
服务方法：Put
```

请求参数格式：

```yaml
{
    "userId": 
    	1247078461865070592,
    "roleIds": [
        1237258113002901515,
        1245949043784421376
    ]
}
```

响应数据格式：

```
{
    "code": 1,
    "msg": "操作成功"
}
```

### 功能开发

#### 1、定义请求VO

```java
/**
 * UpdateRoleInfoReq
 * @description 更新用户角色信息vo
 * @author SongJian
 * @date 2023/2/13 21:19
 * @version
 */
@Data
public class UpdateRoleInfoReq {
    private String userId;
    private List<String> roleIds;
}
```

#### 2、定义访问接口

```java
/**
 * 更新用户角色信息
 * @param req
 * @return
 */
@PutMapping("/roles")
public R<String> updateRoleInfo(@RequestBody UpdateRoleInfoReq req) {
    return roleService.updateRoleInfo(req);
}
```

#### 3、定义服务接口与实现类

```java
/**
 * 更新用户角色信息
 * @param req
 * @return
 */
R<String> updateRoleInfo(UpdateRoleInfoReq req);
```

```java
/**
 * 更新用户角色信息
 * @param req
 * @return
 */
@Override
public R<String> updateRoleInfo(UpdateRoleInfoReq req) {
    // 调用mapper层方法
    sysUserRoleMapper.deleteByUserId(req.getUserId());
    List<String> roleIds = req.getRoleIds();
    for (String roleId : roleIds) {
        long primaryKey = new IdWorker().nextId();
        Date updateTime = DateTime.now().toDate();
        sysUserRoleMapper.inserByUserRoleIds(primaryKey, req.getUserId(), roleId, updateTime);
    }
    return R.ok(ResponseCode.SUCCESS.getMessage());
}
```

#### 4、定义mapper接口与xml

```java
/**
 * 插入条目
 * @param primaryKey
 * @param userId
 * @param roleId
 * @param updateTime
 */
void inserByUserRoleIds(@Param("primaryKey") long primaryKey, @Param("userId") String userId,
                        @Param("roleId") String roleId, @Param("updateTime") Date updateTime);
```

```xml
<insert id="inserByUserRoleIds">
    insert into sys_user_role
    ( id,user_id,role_id
    ,create_time)
    values (#{primaryKey}, #{userId}, #{roleId},#{updateTime})
</insert>
```

```java
/**
 * 根据用户 id 删除
 * @param userId
 */
void deleteByUserId(@Param("userId") String userId);
```

```xml
<delete id="deleteByUserId">
    delete from sys_user_role as sur
    where  sur.user_id = #{userId}
</delete>
```

## 六、批量删除用户信息开发

### 业务说明

#### 1、原型效果

![1644398954735](https://p.ipic.vip/xl007u.png)

#### 2、接口说明

```json
功能描述：批量删除用户信息，delete请求可通过请求体携带数据
服务路径：/user
服务方法：Delete
```

请求数据格式：

~~~json
 [
        1473296822679244800,
        1473296022544453632
 ]
~~~

接口提示：请求参数：`@RequestBody List<Long> userIds`

响应数据格式：

~~~json
{
    "code": 1,
    "msg": "操作成功"
}
~~~

### 功能开发

#### 1、定义访问接口

```java
/**
 * 批量删除用户信息，delete请求可通过请求体携带数据
 * @param userIds
 * @return
 */
@DeleteMapping("/user")
public R<String> deleteByUserId(@RequestBody List<Long> userIds) {
    return userService.deleteByUserId(userIds);
}
```

#### 2、定义服务接口及实现类

```java
/**
 * 批量删除用户信息，delete请求可通过请求体携带数据
 * @param userIds
 * @return
 */
R<String> deleteByUserId(List<Long> userIds);
```

```java
/**
 * 批量删除用户信息，delete请求可通过请求体携带数据
 * @param userIds
 * @return
 */
@Override
public R<String> deleteByUserId(List<Long> userIds) {
    for (Long id : userIds) {
        int res = sysUserMapper.deleteByUserId(id);
        if (res == 0) {
            return R.error(ResponseCode.ERROR.getMessage());
        }
    }
    return R.ok(ResponseCode.SUCCESS.getMessage());
}
```

#### 3、定义mapper及xml

```java
/**
 * 根据用户id删除
 * @param id
 */
int deleteByUserId(@Param("id") Long id);
```

```xml
<delete id="deleteByUserId">
    delete from sys_user
    where  id = #{id}
</delete>
```

## 七、根据用户id查询用户信息

### 业务分析

#### 1、原型效果

<img src="https://p.ipic.vip/qoz5uz.png" alt="1644399036899" style="zoom:50%;" />

#### 2、接口说明

```
功能描述： 根据用户id查询用户信息
服务路径：/user/info/{userId}
服务方法：Get
请求参数：Long id
```

响应数据格式：

```
{
    "code": 1,
    "data": {
        "id": 1247515643591397376,
        "username": "admin123",
        "phone": "13699999999",
        "nickName": "admin测试",
        "realName": "admin测试",
        "sex": 1,
        "status": 1,
        "email": "admin123@qq.com"
    }
}
```

### 功能开发

#### 1、定义vo

```java
/**
 * GetUserInfoVo
 * @description
 * @author SongJian
 * @date 2023/2/13 22:12
 * @version
 */
@Data
public class GetUserInfoVo {
    /**
     * 用户id
     */
    private String id;
    /**
     * 账户
     */
    private String username;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 真实名称
     */
    private String realName;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 邮箱(唯一)
     */
    private String email;

    /**
     * 账户状态(1.正常 2.锁定 )
     */
    private Integer status;

    /**
     * 性别(1.男 2.女)
     */
    private Integer sex;
}
```

#### 2、定义访问接口

```java
/**
 * 根据用户id查询用户信息
 * @param userId
 * @return
 */
@GetMapping("/user/info/{userId}")
public R<GetUserInfoVo> getUserInfoById(@PathVariable Long userId) {
    return userService.getUserInfoById(userId);
}
```

#### 3、定义服务接口及实现类

```java
/**
 * 根据用户id查询用户信息
 * @param userId
 * @return
 */
R<GetUserInfoVo> getUserInfoById(Long userId);
```

```java
/**
 * 根据用户id查询用户信息
 * @param userId
 * @return
 */
@Override
public R<GetUserInfoVo> getUserInfoById(Long userId) {
    GetUserInfoVo info = sysUserMapper.getUserInfoById(userId);
    System.out.println(info.toString());
    return R.ok(info);
}
```

#### 4、定义mapper接口及xml

```java
/**
 * 根据用户id查询用户信息
 * @param userId
 * @return
 */
GetUserInfoVo getUserInfoById(@Param("userId") Long userId);
```

```xml
<select id="getUserInfoById" resultType="tech.songjian.stock.vo.resp.GetUserInfoVo">
    select
        su.id as id,
        su.username as username,
        su.phone as phone,
        su.nick_name as nickName,
        su.real_name as realName,
        su.sex as sex,
        su.status as status,
        su.email as email
    from sys_user as su
    where su.id = #{userId}
</select>
```

## 八、更新用户信息功能开发

### 业务说明

#### 1、原型效果

<img src="https://p.ipic.vip/hs2ira.png" alt="1644398578874" style="zoom:50%;" />

#### 2）接口说明

```
功能描述： 根据id更新用户基本信息
服务路径：/api/user
服务方法：PUT
```

请求参数格式：

~~~json
{
    id: 123456789
    username: 'zhangsan'
    phone: '18811023034'
    email: '345@163.com'
    nickName: '老王'
    realName: '王五'
    sex: '1'
    createWhere: '1'
    status: '1'
}
~~~

响应数据格式：

```
 {    
 	"code": 1,    
 	"msg": "操作成功"
 }
```

### 功能开发

#### 1、定义VO

```java
/**
 * SetUserInfoVo
 * @description
 * @author SongJian
 * @date 2023/2/13 22:46
 * @version
 */
@Data
public class SetUserInfoVo {
    private String id;
    private String username;
    private String phone;
    private String email;
    private String nickName;
    private String realName;
    private Integer sex;
    private Integer createWhere;
    private Integer status;
}
```

#### 2、定义访问接口

```java
/**
 * 更新用户信息
 * @param getUserInfoVo
 * @return
 */
@PutMapping("/user")
public R<String> updateUserInfo(@RequestBody SetUserInfoVo setUserInfoVo) {
    return userService.updateUserInfo(setUserInfoVo);
}
```

#### 3、定义服务接口及实现类

```java
/**
 * 更新用户信息
 * @param setUserInfoVo
 * @return
 */
R<String> updateUserInfo(SetUserInfoVo setUserInfoVo);
```

```java
/**
 * 更新用户信息
 * @param setUserInfoVo
 * @return
 */
@Override
public R<String> updateUserInfo(SetUserInfoVo setUserInfoVo) {
    SysUser user = new SysUser();
    BeanUtils.copyProperties(setUserInfoVo, user);

    Date updateTime = DateTime.now().toDate();
    user.setUpdateTime(updateTime);
    int col = sysUserMapper.updateUserInfo(user);

    if (col <= 0) {
        return R.error(ResponseCode.ERROR.getMessage());
    }
    return R.ok(ResponseCode.SUCCESS.getMessage());
}
```

#### 4、定义mapper接口及xml

```java
/**
 * 更新用户信息
 * @param user
 * @return
 */
int updateUserInfo(@Param("user") SysUser user);
```

```xml
<update id="updateUserInfo">
    update sys_user
    set
        username =  #{user.username},
        phone =  #{user.phone},
        real_name =  #{user.realName},
        nick_name =  #{user.nickName},
        email =  #{user.email},
        status =  #{user.status},
        sex =  #{user.sex},
        update_id =  #{user.updateId},
        create_where =  #{user.createWhere},
        update_time =  #{user.updateTime}
    where   id = #{user.id}
</update>
```

## 九、分页查询当前角色信息

### 业务分析

#### 1、原型效果

![1644232542742](/Users/mac/Desktop/Stock/%E5%90%8E%E7%AB%AF%E5%8A%9F%E8%83%BD%E5%BC%80%E5%8F%91.assets/1644232542742.png)

#### 2、接口说明

```
功能描述： 分页查询当前角色信息
服务路径： /api/roles
服务方法：Post
```

请求参数格式：

```
{
	"pageNum":1,
	"pageSize":10
}
```

响应数据格式：

```
{
    "code": 1,
    "data": {
        "totalRows": 10,
        "totalPages": 1,
        "pageNum": 1,
        "pageSize": 10,
        "size": 10,
        "rows": [
            {
                "id": 1237258113002901512,
                "name": "超级管理员",
                "description": "我是超级管理员",
                "status": 1,
                "createTime": "2020-01-06T15:37:45.000+00:00",
                "updateTime": "2021-12-09T23:08:02.000+00:00",
                "deleted": 1
            },
            {
                "id": 1237258113002901513,
                "name": "标记用户角色测试",
                "description": "标记用户角色测试",
                "status": 1,
                "createTime": "2020-01-08T02:53:35.000+00:00",
                "updateTime": "2021-12-28T10:16:21.000+00:00",
                "deleted": 1
            },
         //.........
        ]
    }
}
```

### 功能开发

#### 1、定义访问接口

```java
/**
 * 分页查询当前角色信息
 * @return
 */
@PostMapping("/roles")
public R<PageResult<SysRole>> getRolesInfoByPage(@RequestBody Map<String, Integer> pageInfo) {
    return roleService.getRolesInfoByPage(pageInfo.get("pageNum"), pageInfo.get("pageSize"));
}
```

#### 2、定义服务接口及实现类

```java
/**
 * 分页查询角色信息
 * @param pageNum
 * @param pageSize
 * @return
 */
R<PageResult<SysRole>> getRolesInfoByPage(Integer pageNum, Integer pageSize);
```

```java
/**
 * 分页查询角色信息
 * @param pageNum
 * @param pageSize
 * @return
 */
@Override
public R<PageResult<SysRole>> getRolesInfoByPage(Integer pageNum, Integer pageSize) {
    // 开启 PageHelper
    PageHelper.startPage(pageNum, pageSize);
    List<SysRole> pages = sysRoleMapper.getRolesInfo();
    if (CollectionUtils.isEmpty(pages)) {
        return R.error(ResponseCode.NO_RESPONSE_DATA.getMessage());
    }
    // 3、组装 pageInfo 对象，他封装了一切的分页信息
    PageInfo<SysRole> pageInfo = new PageInfo<>(pages);
    PageResult<SysRole> pageResult = new PageResult<>(pageInfo);
    return R.ok(pageResult);
}
```

#### 3、定义mapper接口及xml

```java
/**
 * 查询所有角色信息
 * @return
 */
List<SysRole> getRolesInfo();
```

```xml
<select id="getRolesInfo" resultType="tech.songjian.stock.pojo.SysRole">
    select
        sr.id as id,
        sr.name as name,
        sr.description as description,
        sr.status as status,
        sr.create_time as createTime,
        sr.update_time as updateTime,
        sr.deleted as deleted
    from sys_role as sr
</select>
```

## 十、添加角色回显权限选项功能

### 业务分析

#### 1、原型效果

![1644232518903](https://p.ipic.vip/yrigjo.png)

#### 2、接口说明

```
功能描述： 树状结构回显权限集合,底层通过递归获取权限数据集合
服务路径： /api/permissions/tree/all
服务方法：GET
请求参数：无
```

响应格式：

```
{
    "code": 1,
    "data": [
        {
            "id": "1236916745927790564",
            "title": "组织管理",
            "icon": "el-icon-menu",
            "path": "/org",
            "name": "org",
            "children": [
                {
                    "id": "1236916745927790560",
                    "title": "菜单权限管理",
                    "icon": "el-icon-menu",
                    "path": "/menus",
                    "name": "menus",
                    "children": [
                        {
                            "id": "1236916745927790563",
                            "title": "删除菜单权限",
                            "icon": "",
                            "path": "/api/permission",
                            "name": "",
                            "children": []
                        },
                    ]
                }
        {
          //......
        }
        ]
 }       
```

### 功能开发

#### 1、domain类

还是使用之前使用的 `PermissionDomain`：

```java
/**
 * PermissionDomain
 * @description 权限domain
 * @author SongJian
 * @date 2023/2/13 16:06
 * @version
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDomain {
    /**
     * 权限id
     */
    private String id;
    /**
     * 权限标题
     */
    private String title;
    /**
     * 权限图标
     */
    private String icon;
    /**
     * 请求地址
     */
    private String path;
    /**
     * 权限名称对应前端vue组件名称
     */
    private String name;
    /**
     * 此id对应的子权限的集合
     */
    private List<PermissionDomain> children;
}
```

#### 2、定义访问接口

```java
/**
 * 树状结构回显权限集合,底层通过递归获取权限数据集合
 * @return
 */
@GetMapping("/permissions/tree/all")
public R<List<PermissionDomain>> getPermissionTree() {
    return permissionService.getPermissionTree();
}
```

#### 3、定义服务接口及实现

```java
/**
 * 树状结构回显权限集合,底层通过递归获取权限数据集合
 * @return
 */
R<List<PermissionDomain>> getPermissionTree();
```

```java
/**
 * 树状结构回显权限集合,底层通过递归获取权限数据集合
 * @return
 */
@Override
public R<List<PermissionDomain>> getPermissionTree() {
    List<SysPermission> lists = sysPermissionMapper.getAllPermission();
    List<PermissionDomain> permissionTreeVos = lists.stream().filter(s -> "0".equals(s.getPid())).map(item -> {
        PermissionDomain domain = new PermissionDomain();
        domain.setId(item.getId());
        domain.setTitle(item.getTitle());
        domain.setPath(item.getUrl());
        domain.setIcon(item.getIcon());
        domain.setName(item.getName());
        List<SysPermission> childrenSys = getChildrenPermission(item.getId(), lists);
        domain.setChildren(new ArrayList<>());
        domain.setChildren(copyChildrenPermission(childrenSys, domain.getChildren()));
        return domain;
    }).collect(Collectors.toList());
    return R.ok(permissionTreeVos);
}

/**
 * 递归拷贝
 * @param source
 * @param object
 * @return
 */
private List<PermissionDomain> copyChildrenPermission(List<SysPermission> source, List<PermissionDomain> object) {
    if (source != null && source.size() != 0) {
        for (SysPermission s : source) {
            PermissionDomain p = new PermissionDomain();
            p.setChildren(new ArrayList<PermissionDomain>());
            // 类型转换
            p.setId(s.getId());
            p.setTitle(s.getTitle());
            p.setIcon(s.getIcon());
            p.setPath(s.getUrl());
            p.setName(s.getName());
            object.add(p);
            // 判断子权限是否还有子权限，有的话，需要递归拷贝
            if (s.getChildren().size() != 0) {
                copyChildrenPermission(s.getChildren(), p.getChildren());
            }
        }
    }
    return object;
}

/**
 * 根据父权限id，递归封装children
 * @param Pid
 * @param permissions
 * @return
 */
private List<SysPermission> getChildrenPermission(String Pid, List<SysPermission> permissions) {
    // 创建容器，存放权限
    List<SysPermission> children = new ArrayList<>();
    // 根据传过来的父权限id查询所有子权限
    permissions.forEach(s -> {
        if (Pid.equals(s.getPid())) {
            children.add(s);
        }
    });
    // 对每个子孩子进行递归
    children.forEach(s -> {
        s.setChildren(getChildrenPermission(s.getId(), permissions));
    });
    return children;
}
```

#### 4、定义mapper接口及xml

```java
/**
 * 查询所有权限信息，封装至PermissionTreeVo
 * @return
 */
List<SysPermission> getAllPermission();
```

```xml
<select id="getAllPermission" resultType="tech.songjian.stock.pojo.SysPermission">
    SELECT
        sp.id AS id,
        sp.title AS title,
        sp.icon AS icon,
        sp.url AS path,
        sp.name AS name,
        sp.pid AS pid
    FROM
        sys_permission AS sp
</select>
```

#### 5、测试

![image-20230214140134275](https://p.ipic.vip/yr29ft.png)

## 十一、根据角色id查找对应的权限id集合

### 业务分析

#### 1、原型效果

![1644233266689](https://p.ipic.vip/wllgag.png)

#### 2、接口说明

```
功能描述：添加角色和角色关联权限
服务路径：/api/role/{roleId}
服务方法：Get
请求参数：String roleId
```

响应数据格式：

```
{
    "code": 1,
    "data": [
        "1236916745927790580",
        "1236916745927790558",
        "1236916745927790556",
        "1236916745927790578",
        "1236916745927790579",
        "1236916745927790557",
        "1236916745927790577"		
        /........
    ]
}
```

### 功能开发

#### 1、定义访问接口

```java
/**
 * 根据用户id查询用户关联权限
 * @param roleId
 * @return
 */
@GetMapping("/role/{roleId}")
public R<List> getPermissionByUserId(@PathVariable String roleId) {
    return permissionService.getPermissionByUserId(roleId);
}
```

#### 2、定义服务接口及实现类

```java
/**
 * 根据用户id查询用户关联权限
 * @param roleId
 * @return
 */
R<List> getPermissionByUserId(String roleId);
```

```java
/**
 * 根据用户id查询用户关联权限
 * @param roleId
 * @return
 */
@Override
public R<List> getPermissionByUserId(String roleId) {
    List<String> roles = sysRolePermissionMapper.getPermissionByUserId(roleId);
    return R.ok(roles);
}
```

#### 3、定义mapper接口及xml

```java
/**
 * 根据用户id 查询权限集合
 * @param roleId
 * @return
 */
List<String> getPermissionByUserId(@Param("roleId") String roleId);
```

```xml
<select id="getPermissionByUserId" resultType="java.lang.String">
    SELECT
        srp.permission_id
    FROM
        sys_role_permission AS srp
    WHERE
        srp.role_id = #{roleId}
</select>
```

#### 4、测试

![image-20230214142146286](https://p.ipic.vip/hbadd3.png)

## 十二、更新角色信息

### 业务分析

#### 1、原型效果

![1644233266689](https://p.ipic.vip/l29hs7.png)

#### 2、接口说明

```
功能描述：添加角色和角色关联权限
服务路径：/role
服务方法：Put
```

请求参数格式：

```yaml
{
    "id": 1483338014502690844,
    "name": "vvvvvvvvvvvvvvvvvvvvv",
    "description": "vvvvvvvvvvvvvvvvvv",
    "permissionsIds":[
        1236916745927790564,
        1236916745927790577,
        1236916745927790568
    ]
}
```

响应数据类型:

```
{
    "code": 1,
    "msg": "操作成功"
}
```

### 功能开发

#### 1、定义请求vo

```java
/**
 * UpdateRolePermissionReq
 * @description 更新角色信息vo
 * @author SongJian
 * @date 2023/2/14 14:28
 * @version
 */
@Data
public class UpdateRolePermissionReq {

    /**
     * 角色id
     */
    private String id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 权限集合
     */
    private List<String> permissionsIds;

}
```

#### 2、定义访问接口

```java
/**
 * 更新角色信息（包括权限信息）
 * @param updateRolePermissionReq
 * @return
 */
@PutMapping("/role")
public R<String> updatePermissionByRoleId(@RequestBody UpdateRolePermissionReq updateRolePermissionReq) {
    return roleService.updatePermissionByRoleId(updateRolePermissionReq);
}
```

#### 3、定义服务接口及实现类

```java
/**
 * 更新角色信息（包括权限信息）
 * @param updateRolePermissionReq
 * @return
 */
@Override
public R<String> updatePermissionByRoleId(UpdateRolePermissionReq updateRolePermissionReq) {
    // 取出roleId
    String roleId = updateRolePermissionReq.getId();
    // 根据roleId修改角色名称和描述
    sysRoleMapper.updateNameAndDesById(updateRolePermissionReq.getName(),
                                       updateRolePermissionReq.getDescription(),
                                       roleId,
                                       DateTime.now().toDate());
    // 根据roleId删除权限
    sysRolePermissionMapper.deleteByRoleId(roleId);

    // 批量插入roleid和权限集合
    for (String p : updateRolePermissionReq.getPermissionsIds()) {
        SysRolePermission sysRolePermission = new SysRolePermission();
        sysRolePermission.setRoleId(roleId);
        sysRolePermission.setPermissionId(p);
        sysRolePermission.setId(String.valueOf(idWorker.nextId()));
        sysRolePermission.setCreateTime(DateTime.now().toDate());
        sysRolePermissionMapper.insert(sysRolePermission);
    }

    return R.ok(ResponseCode.SUCCESS.getMessage());
}
```

#### 4、定义mapper及xml

```java
/**
 * 根据角色id修改角色名称和角色描述
 * @param name
 * @param description
 */
void updateNameAndDesById(@Param("name") String name,
                          @Param("description") String description,
                          @Param("id") String id,
                          @Param("updateTime") Date updateTime);
```

```xml
<update id="updateNameAndDesById">
    update sys_role
    set
        name =  #{name},
        description =  #{description},
        update_time =  #{updateTime}
    where   id = #{id}
</update>
```

```java
/**
 * 根据roleid删除权限
 * @param roleId
 */
void deleteByRoleId(@Param("roleId") String roleId);
```

```xml
<delete id="deleteByRoleId">
    delete
    from sys_role_permission
    where  role_id = #{roleId}
</delete>
```

## 十三、根据角色id删除角色信息

### 业务分析

#### 1、原型效果

![1644233624883](https://p.ipic.vip/0nczdb.png)

#### 2、接口说明

```
功能描述： 删除角色和角色关联权限
服务路径： /role/{roleId}
服务方法：Delete
请求参数：String roleId
```

响应数据类型:

```
{
    "code": 1,
    "msg": "操作成功"
}
```

### 功能开发

#### 1、定义访问接口

```java
/**
 * 删除角色和角色关联的权限
 * @param roleId
 * @return
 */
@DeleteMapping("/role/{roleId}")
public R<String> deleteRoleAndPerByRoleId(@PathVariable String roleId) {
    return roleService.deleteRoleAndPerByRoleId(roleId);
}
```

#### 2、定义服务接口及实现类

```java
/**
 * 删除角色和角色关联的权限
 * @param roleId
 * @return
 */
@Override
public R<String> deleteRoleAndPerByRoleId(String roleId) {
    sysRolePermissionMapper.deleteByRoleId(roleId);
    sysRoleMapper.deleteByRoleId(roleId);
    sysUserRoleMapper.deleteByRoleId(roleId);
    return R.ok(ResponseCode.SUCCESS.getMessage());
}
```

#### 3、定义mapper接口及xml

```java
/**
 * 根据roleid删除权限
 * @param roleId
 */
void deleteByRoleId(@Param("roleId") String roleId);
```

```xml
<delete id="deleteByRoleId">
    delete
    from sys_role_permission
    where  role_id = #{roleId}
</delete>
```

```java
/**
 * 根据角色id删除角色
 * @param roleId
 */
void deleteByRoleId(String roleId);
```

```xml
<delete id="deleteByRoleId">
    delete from sys_role
    where  id = #{roleId}
</delete>
```

```java
/**
 * 根据角色id删除
 * @param roleId
 */
void deleteByRoleId(String roleId);
```

```xml
<delete id="deleteByRoleId">
    delete from sys_user_role as sur
    where  sur.role_id = #{roleId}
</delete>
```

## 十四、更新角色的状态信息

### 业务分析

#### 1、原型效果

![1644233697696](https://p.ipic.vip/8ug5ca.png)

#### 2、接口说明

```
功能描述： 更新用户的状态信息
服务路径： /role/{roleId}/{status}
服务方法：Post
```

响应数据类型:

```
{
    "code": 1,
    "msg": "操作成功"
}
```

### 功能开发

#### 1、定义访问接口

```java
/**
 * 根据角色id更新角色状态
 * @param roleId
 * @param status
 * @return
 */
@PostMapping("/role/{roleId}/{status}")
public R<String> updateRoleStatus(@PathVariable String roleId, @PathVariable String status) {
    return roleService.updateRoleStatus(roleId, status);
}
```

#### 2、定义服务接口及实现类

```java
/**
 * 根据角色id更新角色状态
 * @param roleId
 * @param status
 * @return
 */
R<String> updateRoleStatus(String roleId, String status);
```

```java
/**
 * 根据角色id更新角色状态
 * @param roleId
 * @param status
 * @return
 */
@Override
public R<String> updateRoleStatus(String roleId, String status) {
    sysRoleMapper.updateRoleStatus(roleId, Integer.valueOf(status), DateTime.now().toDate());
    return R.ok(ResponseCode.SUCCESS.getMessage());
}
```

#### 3、定义mapper接口及xml

```java
/**
 * 根据角色id更新角色状态
 * @param roleId
 * @param status
 */
void updateRoleStatus(@Param("roleId") String roleId,
                      @Param("status") Integer status,
                      @Param("updateTime") Date updateTime);
```

```xml
<update id="updateRoleStatus">
    update sys_role
    set
        status = #{status},
        update_time =  #{updateTime}
    where   id = #{roleId}
</update>
```

# 权限管理后端开发

## 一、权限列表展示

### 业务分析

#### 1、原型效果

![1644233839418](https://p.ipic.vip/fj9vv1.png)

#### 2、接口说明

```
功能描述： 查询所有权限集合
服务路径： /api/permissions
服务方法：Get
请求参数：无
```

响应数据格式:

```Json
{
    "code": 1,
    "data": [
        {
            "id": 1236916745927790556,
            "code": "btn-user-delete",
            "title": "删除用户权限",
            "icon": "",
            "perms": "sys:user:delete",
            "url": "/api/user",
            "method": "DELETE",
            "name": "",
            "pid": 1236916745927790575,
            "orderNum": 100,
            "type": 3,
            "status": 1,
            "createTime": "2020-01-08T07:42:50.000+00:00",
            "updateTime": null,
            "deleted": 1
        },
        {
            "id": 1473855535827783680,
            "code": "",
            "title": "测试-01",
            "icon": "el-icon-user-solid",
            "perms": "",
            "url": null,
            "method": "",
            "name": "test-01",
            "pid": 0,
            "orderNum": 555,
            "type": 1,
            "status": 1,
            "createTime": "2021-12-23T03:18:36.000+00:00",
            "updateTime": "2021-12-23T03:18:36.000+00:00",
            "deleted": 1
        }
        //...............
    ]
}
```

### 功能开发

#### 1、定义访问接口

```java
/**
 * 获取所有权限集合
 * @return
 */
@GetMapping("/permissions")
public R<List<SysPermission>> getAllPermissions() {
    return permissionService.getAllPermissions();
}
```

#### 2、定义服务接口及实现类

```java
/**
 * 获取所有权限集合
 * @return
 */
@Override
public R<List<SysPermission>> getAllPermissions() {
    List<SysPermission> permissions =  sysPermissionMapper.getAllPermissions();
    return R.ok(permissions);
}
```

#### 3、定义mapper接口及xml

```xml
<select id="getAllPermissions" resultType="tech.songjian.stock.pojo.SysPermission">
    SELECT
        sp.id as id,
        sp.code as code,
        sp.title as title,
        sp.icon as icon,
        sp.perms as perms,
        sp.url as url,
        sp.method as method,
        sp.name as name,
        sp.pid as pid,
        sp.order_num as orderNum,
        sp.type as type,
        sp.status as status,
        sp.create_time as createTime,
        sp.update_time as updateTime,
        sp.deleted as deleted
    FROM
        sys_permission AS sp
    WHERE
        sp.deleted = 1
</select>
```

#### 4、测试

![image-20230214171353168](https://p.ipic.vip/p5wpoq.png)

## 二、添加权限时回显权限树

### 业务说明

#### 1、原型效果

![1644233893500](https://p.ipic.vip/wta6qn.png)

#### 2、接口说明

```
功能描述： 添加权限时回显权限树,仅仅显示目录和菜单
服务路径： /api/permissions/tree
服务方法：Get
请求参数：无
```

响应数据格式:

```json
{
    "code": 1,
    "data": [
        {
            "id": 0,
            "title": "顶级菜单",
            "level": 0
        },
        {
            "id": 1236916745927790564,
            "title": "组织管理",
            "level": 1
        },
        {
            "id": 1236916745927790560,
            "title": "菜单权限管理",
            "level": 2
        },
        {
            "id": 1473855535827783680,
            "title": "测试-01",
            "level": 1
        }
        //..................
    ]
}
```

### 功能开发

#### 1、定义访问接口

```java
/**
 * 添加权限时回显权限树，仅仅显示目录和菜单
 * @return
 */
@GetMapping("/permissions/tree")
public R<List<Map>> getPermissionsTree4Add() {
    return permissionService.getPermissionsTree4Add();
}
```

#### 2、定义服务接口及实现类

```java
/**
 * 添加权限时回显权限树，仅仅显示目录和菜单
 * @return
 */
List<Map> mapList = new ArrayList<>();

@Override
public R<List<Map>> getPermissionsTree4Add() {
    //按照用户id查找其权限信息
    List<Map> list = sysPermissionMapper.findAllPermissionlLevel1();

    Map<String,Object> mostLevel = new HashMap<>();
    mostLevel.put("id","0");
    mostLevel.put("title","顶级菜单");
    mostLevel.put("level",0);
    mapList.add(mostLevel);
    for (Map map : list) {
        map.put("level",1);
    }
    int i = 1;
    //List result = extracted(list,i);
    extracted(list);

    return R.ok(mapList);
}
private void extracted(List<Map> list) {
    for (Map map : list) {
        mapList.add(map);
        String id = (String) map.get("id");
        //寻找权限下的子权限
        List<Map> sonPermissionSon = sysPermissionMapper.findAllPermissionSon(id);

        if (sonPermissionSon != null) {
            if (sonPermissionSon.size() != 0) {
                for (Map map1 : sonPermissionSon) {
                    int level = (int) map.get("level");
                    map1.put("level", level + 1);
                    //mapList.add(map1);
                }
                //i++;
                extracted(sonPermissionSon);
            }
        }

    }
}
```

#### 3、定义mapper接口及xml

```java
List<Map> findAllPermissionSon(String id);

List<Map> findAllPermissionlLevel1();
```

```xml
<!--    查找顶层权限-->
<select id="findAllPermissionlLevel1" resultType="java.util.Map">
    select id,
           title
    from sys_permission
    where pid = 0
      and deleted = 1
</select>
<!--    寻找子权限-->
<select id="findAllPermissionSon" resultType="java.util.Map">
    select id,
           title
    from sys_permission
    where pid = #{id}
      and deleted = 1
</select>
```

#### 4、测试

![image-20230214191713650](https://p.ipic.vip/bp7r5b.png)

## 三、权限添加按钮

### 业务分析

#### 1、原型效果

![1644233893500](https://p.ipic.vip/2l7z4e.png)

#### 2、接口说明

```
功能描述： 权限添加按钮
服务路径： /permission
服务方法：Post
```

请求参数格式:

```json
{
    "type":"1",		//菜单等级 0 顶级目录 1.目录 2 菜单 3 按钮
    "title":"更新角色权限",
    
     /**
     * 对应资源路径
     *  1.如果类型是目录，则url为空
     *  2.如果类型是菜单，则url对应路由地址
     *  3.如果类型是按钮，则url对应是访问接口的地址
     */
    "pid":1236916745927790560,
 
    "url":"api/permissions/tree",//只有菜单类型有名称，默认是路由的名称
    "name":"org",
    "icon":"el-icon-menu",
    "perms":"sys:role:update",//基于springSecrutiry约定的权限过滤便是
    "method":"DELETE",//请求方式：get put delete post等
    "code":"btn-role-update",//vue按钮回显控制辨识
    "orderNum":100//排序
}
```

接口提示：请求参数：`@RequestBody PermissionAddVo vo`

响应数据格式:

```json
{
    "code": 0,
    "msg": "添加成功"
}
```

### 功能开发

#### 1、定义访问接口

```java
/**
 * 权限添加按钮
 * @param sysPermission
 * @return
 */
@PostMapping("/permission")
public R<String> addPermission(@RequestBody SysPermission sysPermission){
    return permissionService.addPermission(sysPermission);
}
```

#### 2、定义服务接口及实现类

```java
@Override
public R<String> addPermission(SysPermission sysPermission) {
    sysPermission.setId(String.valueOf(idWorker.nextId()));
    if (sysPermission.getType() == 1) {
        sysPermission.setUrl(null);
    }
    sysPermission.setCreateTime(DateTime.now().toDate());
    sysPermission.setDeleted(1);
    sysPermission.setStatus(1);
    int i = sysPermissionMapper.addPermission(sysPermission);
    if (i > 0) {
        return R.ok(ResponseCode.SUCCESS.getMessage());
    }
    return R.error(ResponseCode.ERROR.getMessage());
}
```

#### 3、定义mapper接口及xml

```xml
<insert id="addPermission">
    insert into sys_permission
    ( id,code,title
    ,icon,perms,url
    ,method,name,pid
    ,order_num,type,status
    ,create_time,update_time,deleted
    )
    values (#{sysPermission.id,jdbcType=VARCHAR},#{sysPermission.code,jdbcType=VARCHAR},#{sysPermission.title,jdbcType=VARCHAR}
           ,#{sysPermission.icon,jdbcType=VARCHAR},#{sysPermission.perms,jdbcType=VARCHAR},#{sysPermission.url,jdbcType=VARCHAR}
           ,#{sysPermission.method,jdbcType=VARCHAR},#{sysPermission.name,jdbcType=VARCHAR},#{sysPermission.pid,jdbcType=VARCHAR}
           ,#{sysPermission.orderNum,jdbcType=INTEGER},#{sysPermission.type,jdbcType=TINYINT},#{sysPermission.status,jdbcType=TINYINT}
           ,#{sysPermission.createTime,jdbcType=TIMESTAMP},#{sysPermission.updateTime,jdbcType=TIMESTAMP},#{sysPermission.deleted,jdbcType=TINYINT}
           )

</insert>
```

## 四、更新权限

### 业务分析

#### 1、原型效果

![1644234127208](https://p.ipic.vip/d4kf5r.png)

#### 2、接口说明

```
功能描述： 更新权限
服务路径： /permission
服务方法：Put
```

请求参数格式:

```json
{
    "id": 1236916745927790556   //权限id
    "type":"1",		//菜单等级 0 顶级目录 1.目录 2 菜单 3 按钮
    "title":"更新角色权限",
    
     /**
     * 对应资源路径
     *  1.如果类型是目录，则url为空
     *  2.如果类型是菜单，则url对应路由地址
     *  3.如果类型是按钮，则url对应是访问接口的地址
     */
    "pid":1236916745927790560,
 
    "url":"api/permissions/tree",//只有菜单类型有名称，默认是路由的名称
    "name":"org",
    "icon":"el-icon-menu",
    "perms":"sys:role:update",//基于springSecrutiry约定的权限过滤便是
    "method":"DELETE",//请求方式：get put delete post等
    "code":"btn-role-update",//vue按钮回显控制辨识
    "orderNum":100//排序
}
```

接口提示：请求参数：@RequestBody PermissionUpdateVo vo

响应数据格式:

```json
{
    "code": 0,
    "msg": "更新成功"
}
```

## 五、删除权限

#### 1）原型效果

![1644234168465](https://p.ipic.vip/c4i65b.png)

#### 2）接口说明

```
功能描述： 删除权限
服务路径： /permission/{permissionId}
服务方法：Delete
```

响应数据格式:

```json
{
    "code": 0,
    "msg": "删除成功"
}
```

### 功能开发

#### 1、定义访问接口

```java
/**
 * 删除权限
 * @param permissionId
 * @return
 */
@DeleteMapping("/permission/{permissionId}")
public R<String> deletePermission (@PathVariable String permissionId){
    return permissionService.deletePermission(permissionId);
}
```

#### 2、定义服务接口及实现类

```java
/**
 * 删除权限
 * @param permissionId
 * @return
 */
@Override
public R<String> deletePermission(String permissionId) {
    int i = sysPermissionMapper.deletePermission(permissionId);
    if (i <= 0){

        return R.error("操作失败");
    }
    return R.ok("操作成功");
}
```

#### 3、定义mapper接口及xml

```xml
<update id="deletePermission">
    update sys_permission
    set
        deleted =  0
    where   id = #{permissionId}
</update>
```

