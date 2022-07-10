在自己的虚拟机上测了下`MongoDB`千万级数据的性能，方法比较粗糙，仅供参考。

#### 一、环境准备

> CentOS 7.6  CPU 4核 内存 8G
>
> MongoDB 5.0.9

#### 二、测试代码

用`Spring Boot`写了个测试程序

**`Maven`依赖 `pom.xml`**

```xml
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>

		<!-- mongodb -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-mongodb</artifactId>
		</dependency>

		<!-- lombok依赖 -->
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<version>1.18.16</version>
			<scope>provided</scope>
		</dependency>
	</dependencies>
```

**配置文件 `application.properties`**

```properties
spring.data.mongodb.uri=mongodb://192.168.73.146:27017/springboot
```

**实体类**

```java
package com.rkyao.spring.boot.mongo.entity;

import lombok.Data;
import lombok.ToString;

/**
 * mongo表实体类
 *
 * @author Administrator
 * @date 2022/6/17
 */
@Data
@ToString
public class MongoEntity {

    private String userId;

    private String userName;

    private Integer age;

    private String address;

    private String field5;

    private String field6;

    private String field7;

    private String field8;

    private String field9;

    private String field10;

    private String field11;

    private String field12;

    private String field13;

    private String field14;

    private String field15;

    private String field16;

    private String field17;

    private String field18;

    private String field19;
    
    private String field20;

}
```

**插入、查询代码，性能测试结果参见注释**

```java
package com.rkyao.spring.boot.mongo.controller;

import com.rkyao.spring.boot.mongo.entity.MongoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.HashedIndex;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 性能测试程序
 *
 * 4核
 * 8G
 * 40G SSD
 *
 * @author Administrator
 * @date 2022/6/17
 */
@RestController
@Slf4j
public class MongoController {

    @Autowired
    private MongoTemplate mongoTemplate;

    public static final String MONGO_TABLE = "t_user_info";

    /**
     * localhost:8080/findOne
     *
     * 总量10000000条
     *
     * 查询单条耗时 4个字段 < 5ms
     * 查询单条耗时 20个字段 < 5ms
     *
     * @return
     */
    @RequestMapping("/findOne")
    public String findOne() {
        long start = System.currentTimeMillis();

        Query query = new Query(Criteria.where("age").is(23));
        MongoEntity mongoEntity = mongoTemplate.findOne(query, MongoEntity.class, MONGO_TABLE);

        long cost = System.currentTimeMillis() - start;
        String logStr = String.format("查询 %s 条数据耗时: %s ms", 1, cost);
        log.info(logStr);
        return logStr;
    }

    /**
     * localhost:8080/find
     *
     * 总量10000000条
     *
     * 查询10000条耗时 无索引 4个字段 4000ms ~ 5000ms
     * 查询10000条耗时 Test索引 4个字段 4000ms ~ 5000ms
     * 查询10000条耗时 Hashed索引 4个字段 100ms ~ 150ms
     *
     * 查询10000条耗时 无索引 20个字段 25000ms ~ 30000ms
     * 查询10000条耗时 Test索引 20个字段 23781ms
     * 查询10000条耗时 Hashed索引 20个字段 500ms ~ 600ms (总量2千万时性能没有明显差距)
     *
     * @return
     */
    @RequestMapping("/find")
    public String find() {
        long start = System.currentTimeMillis();

        Query query = new Query(Criteria.where("age").is(23));
//        query.fields().include("userId", "userName", "age", "address");
        query.limit(10000);
        List<MongoEntity> mongoEntityList = mongoTemplate.find(query, MongoEntity.class, MONGO_TABLE);

        long cost = System.currentTimeMillis() - start;
        String logStr = String.format("查询 %s 条数据耗时: %s ms", mongoEntityList.size(), cost);
        log.info(logStr);
        return logStr;
    }

    /**
     * localhost:8080/insert
     *
     * 插入单条耗时 < 5ms
     *
     * @return
     */
    @RequestMapping("/insert")
    public String insert() {
        long start = System.currentTimeMillis();

        mongoTemplate.insert(createMongoEntity(), MONGO_TABLE);

        long cost = System.currentTimeMillis() - start;
        String logStr = String.format("插入 %s 条数据耗时: %s ms", 1, cost);
        log.info(logStr);
        return logStr;
    }

    /**
     * localhost:8080/insertBatch
     *
     * 插入10000条耗时 逐条插入 4个字段 6000 ~ 10000ms
     * 插入10000条耗时 逐条插入 20个字段 13468ms
     *
     * @return
     */
    @RequestMapping("/insertBatch")
    public String insertBatch() {
        long start = System.currentTimeMillis();

        int count = 10000;
        for (int i = 0; i < count; i++) {
            mongoTemplate.insert(createMongoEntity(), MONGO_TABLE);
        }

        long cost = System.currentTimeMillis() - start;
        String logStr = String.format("插入 %s 条数据耗时: %s ms", count, cost);
        log.info(logStr);
        return logStr;
    }

    /**
     * localhost:8080/insertBatch2
     *
     * 插入10000条耗时 批量插入 4个字段 300 ~ 500ms
     * 插入10000条耗时 批量插入 20个字段 1060ms
     *
     * @return
     */
    @RequestMapping("/insertBatch2")
    public String insertBatch2() {
        int count = 10000;
        List<MongoEntity> mongoEntityList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            mongoEntityList.add(createMongoEntity());
        }

        long start = System.currentTimeMillis();

        mongoTemplate.insert(mongoEntityList, MONGO_TABLE);

        long cost = System.currentTimeMillis() - start;
        String logStr = String.format("插入 %s 条数据耗时: %s ms", count, cost);
        log.info(logStr);
        return logStr;
    }

    /**
     * localhost:8080/insertBatch3
     *
     * 插入10000000条耗时 批量插入 4个字段 298000ms 354557ms
     * 插入10000000条耗时 批量插入 20个字段 1372460ms
     *
     * 存储大小: 6.69G  单索引大小：107.46MB
     *
     * @return
     */
    @RequestMapping("/insertBatch3")
    public String insertBatch3() {
        int loop = 1000;
        int count = 10000;

        long start = System.currentTimeMillis();

        for (int i = 0; i < loop; i++) {
            List<MongoEntity> mongoEntityList = new ArrayList<>();
            for (int j = 0; j < 10000; j++) {
                mongoEntityList.add(createMongoEntity());
            }
            long startTemp = System.currentTimeMillis();

            mongoTemplate.insert(mongoEntityList, MONGO_TABLE);

            long costTemp = System.currentTimeMillis() - startTemp;

            String logStr = String.format("插入 %s 条数据耗时: %s ms", i * count, costTemp);
            log.info(logStr);
        }

        long cost = System.currentTimeMillis() - start;
        String logStr = String.format("插入 %s 条数据耗时: %s ms", loop * count, cost);
        log.info(logStr);
        return logStr;
    }

    /**
     * localhost:8080/createIndex
     *
     * 创建Text类型单列索引 表里有10000000数据 21538 ms
     * 创建Hashed类型单列索引 表里有10000000数据 20962 ms
     *
     * @return
     */
    @RequestMapping("/createIndex")
    public String createIndex() {
        long start = System.currentTimeMillis();

        // 创建Text类型索引
//        TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder().onField("age").build();
//        mongoTemplate.indexOps(MONGO_TABLE).ensureIndex(textIndex);

        // 创建Hashed类型索引
        HashedIndex hashedIndex = HashedIndex.hashed("age");
        mongoTemplate.indexOps(MONGO_TABLE).ensureIndex(hashedIndex);

        long cost = System.currentTimeMillis() - start;
        String logStr = String.format("创建索引耗时: %s ms", cost);
        log.info(logStr);
        return logStr;
    }

    private MongoEntity createMongoEntity() {
        Random rd = new Random();
        int temp = rd.nextInt(1000);

        // 四个字段
        MongoEntity mongoEntity = new MongoEntity();
        mongoEntity.setUserId(UUID.randomUUID().toString());
        mongoEntity.setUserName("rkyao" + temp);
        mongoEntity.setAge(temp);
        mongoEntity.setAddress("山东");

        // 16个字段 数量可选，不需要的注释掉
        mongoEntity.setField5(UUID.randomUUID().toString());
        mongoEntity.setField6(UUID.randomUUID().toString());
        mongoEntity.setField7(UUID.randomUUID().toString());
        mongoEntity.setField8(UUID.randomUUID().toString());
        mongoEntity.setField9(UUID.randomUUID().toString());
        mongoEntity.setField10(UUID.randomUUID().toString());
        mongoEntity.setField11(UUID.randomUUID().toString());
        mongoEntity.setField12(UUID.randomUUID().toString());
        mongoEntity.setField13(UUID.randomUUID().toString());
        mongoEntity.setField14(UUID.randomUUID().toString());
        mongoEntity.setField15(UUID.randomUUID().toString());
        mongoEntity.setField16(UUID.randomUUID().toString());
        mongoEntity.setField17(UUID.randomUUID().toString());
        mongoEntity.setField18(UUID.randomUUID().toString());
        mongoEntity.setField19(UUID.randomUUID().toString());
        mongoEntity.setField20(UUID.randomUUID().toString());

        return mongoEntity;
    }

}
```

#### 三、源码

https://github.com/yaorongke/spring-boot-demos/tree/main/spring-boot-mongo

