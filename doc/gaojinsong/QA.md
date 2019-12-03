#### BeanDefinition是什么?为什么要有这个?这个对我们写代码有什么借鉴意义
    1：A BeanDefinition describes a bean instance, which has property values, constructor argument values, and further information supplied by concrete implementations.  
    接口的文档已经详细说明了BeanDefinition的作用，简单来说BeanDefinition来描述Bean的定义.  
    因为Bean的定义来源有多维度，例如注解、xml文件等。增加BeanDefinition为后续处理逻辑提供了统一的逻辑。
#### 从容器角度,将了解到的概念,用一个简单直接的话或图描述
![image](http://img.souche.com/f2e/1ed3527c0f624856ac68f3dd81e45dcc.png)
该图描述了Spring容器从加载配置文件到创建出一个完整Bean的作业流程


#### 已知Spring可以解决单例模式，属性中循环依赖，Setter注入的循环依赖问题，但是为什么Spring只能解决这个特定的依赖问题，解决它所用到的原理大致介绍一下。
#### Spring的lookup-method 和 replace-method 用的不多，但是也有一定的应用场景。简单说一下我们实际可以运用的场景。