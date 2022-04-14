#### 一、Maven Archetype是啥

`Archetype` 是 `Maven` 的工程模板，使用 `Archetype` 可以快速创建一个统一结构的工程，提高效率。`Maven` 有一些自带的 `Archetype`，但是很多时候不能满足我们的需求，因此需要自定义  `Archetype` 以生成满足我们需求的工程。

本文介绍自定义Archetype的创建方法，以一个工程为基础，生成 `Archetype`模板，使用模板快速创建目标工程，涉及三个工程。

- **spring-boot-original：**基础工程，用这个生成 `Archetype` 模板工程
- **spring-boot-archetype：**自定义 `Archetype` 模板工程
- **spring-boot-target：**使用自定义 `Archetype` 生成的目标工程

#### 二、自定义Archetype

1、首先找一个已有的工程 `spring-boot-original`，以此为基础，目录结构如下

```shell
spring-boot-original
│  .gitignore
│  pom.xml
│  README.md
│  
└─src
    └─main
        ├─java
        │  └─com
        │      └─rkyao
        │          └─spring
        │              └─boot
        │                  └─original
        │                          Application.java
        │                          
        └─resources
                application.properties
```

2、在 `spring-boot-original` 的根目录下执行如下命令，生成 `Archetype`

```shell
mvn clean archetype:create-from-project -Dmaven.test.skip=true

# 上面命令执行时如果报错，指定maven配置文件，配置文件路径改成自己的
mvn clean archetype:create-from-project --settings D:\\Mysoftware\\apache-maven-3.5.2\\conf\\settings.xml -Dmaven.test.skip=true
```

在 `target\generated-sources\archetype` 目录会生成 `Archetype` 的相关内容，把这个目录下的内容复制出来，放到一个新目录 `spring-boot-archetype` 里，这样就形成了一个新的模板工程

```shell
spring-boot-archetype
│  pom.xml
│  
└─src
    ├─main
    │  └─resources
    │      ├─archetype-resources
    │      │  │  .gitignore
    │      │  │  pom.xml
    │      │  │  README.md
    │      │  │  
    │      │  └─src
    │      │      └─main
    │      │          ├─java
    │      │          │      Application.java
    │      │          │      
    │      │          └─resources
    │      │                  application.properties
    │      │                  
    │      └─META-INF
    │          └─maven
    │                  archetype-metadata.xml
    │                  
    └─test
        └─resources
            └─projects
                └─basic
                        archetype.properties
                        goal.txt
```

3、`spring-boot-archetype` 里面可以自定义一些模板变量，例如将服务端口和路径定义为变量

`application.properties`  里添加 `${serverPort}` 和 `${contextPath}` 变量

```shell
#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
spring.application.name=${artifactId}

server.port=${serverPort}
server.servlet.context-path=/${contextPath}
```

`archetype-metadata.xml` 里添加两个变量的声明

```shell
  <requiredProperties>
    <requiredProperty key="serverPort">
      <defaultValue>8080</defaultValue>
    </requiredProperty>
    <requiredProperty key="contextPath">
      <defaultValue>/</defaultValue>
    </requiredProperty>
  </requiredProperties>
```

`archetype.properties` 里也要添加一下，设置个默认值

```shell
serverPort=8080
contextPath=/
```

4、在 `spring-boot-archetype`  根目录下执行，安装 `Archetype` 到本地仓库(这里只是为了演示，也可上传到私服供其它开发人员使用)

```shell
mvn clean install
```

5、使用  `Archetype`  创建一个工程，执行如下命令

```shell
mvn archetype:generate -DinteractiveMode=false -DarchetypeGroupId=com.rkyao -DarchetypeArtifactId=spring-boot-archetype -DarchetypeVersion=0.0.1-SNAPSHOT -DgroupId=com.rkyao -DartifactId=spring-boot-target -Dpackage=com.rkyao.spring.boot.target -Dversion=0.0.1-SNAPSHOT -DserverPort=9000 -DcontextPath=target_service
```

参数说明：

- **-DarchetypeGroupId：**自定义archetype的groupId
- **-DarchetypeArtifactId：**自定义archetype的artifactId
- **-DarchetypeVersion：**自定义archetype的版本号
- **-DgroupId：**要生成的工程的groupId
- **-DartifactId：**要生成的工程的artifactId
- **-Dpackage：**要生成的工程的包名
- **-Dversion：**要生成的工程的版本号
- **-DserverPort：**自定义变量，服务端口
- **-DcontextPath：**自定义变量，服务路径

#### 三、GitHub源码地址

https://github.com/yaorongke/spring-boot-demos/tree/main/spring-boot-archetype-sample