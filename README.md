# 学成在线

#### 介绍

学成在线借鉴了MOOC(大型开放式网络课程，即MOOC(massive open online courses))的设计思想，是一 个提供IT职业课程在线学习的平台，它为即将和已经加入IT领域的技术人才提供在线学习服务，用户通过在线学 习、在线练习、在线考试等学习内容，最终掌握所学的IT技能，并能在工作中熟练应用。

#### 软件架构

采用当前流行的前后端分离架构开发，由用户层、UI层、微服务层、数据层等部分组成，为PC、App、 H5等客户端用户提供服务


#### 技术栈

- 学成在线服务端基于Spring Boot构建，采用Spring Cloud微服务框架。
- 持久层：MySQL、MongoDB、Redis、ElasticSearch
- 数据访问层：使用Spring Data JPA 、Mybatis、Spring Data Mongodb等
- 业务层：Spring IOC、Aop事务控制、Spring Task任务调度、Feign、Ribbon、Spring AMQP、Spring Data Redis 等。
- 控制层：Spring MVC、FastJSON、RestTemplate、Spring Security Oauth2+JWT等
- 微服务治理：Eureka、Zuul、Hystrix、Spring Cloud Config等
