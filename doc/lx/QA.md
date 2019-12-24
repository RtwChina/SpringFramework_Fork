###  4.1.spring提供的BeanPostProcessor主要有哪些？各自的作用
###  4.2.spring的监听器是怎么注册的？在何时注册的？
###  4.3.事件监听的实现原理




### 2.2已知Spring可以解决单例模式，属性中循环依赖，Setter注入的循环依赖问题，但是为什么Spring只能解决这个特定的依赖问题，解决它所用到的原理大致介绍一下。

### 2.1Spring的lookup-method 和 replace-method 用的不多，但是也有一定的应用场景。简单说一下我们实际可以运用的场景。



### 1.1BeanDefinition是什么?为什么要有这个?这个对我们写代码有什么借鉴意义

#####► Bean：书
#####► BeanDefinition: 书的模板
		容器中的每一个bean都会有一个对应的BeanDefinition实例，
		该实例负责保存bean对象的所有必要信息，包括bean对象的class类型、是否是抽象类、构造方法和参数、其它属性等等。
		当客户端向容器请求相应对象时，容器（BeanFactory）就会通过这些信息为客户端返回一个完整可用的bean实例。
		两个主要实现类 ：
		RootBeanDefinition和ChildBean-Definition	
#####► BeanDefinitionRegistry： 书架
		抽象出bean（BeanDefinition）的注册逻辑
#####► BeanFactory：--容器  图书馆
		抽象出了bean（BeanDefinition）的管理逻辑
		采用延迟初始化策略：只有当访问容器中的某个对象时，才对该对象进行初始化和依赖注入操作
		配置实现：XmlBeanFactory 
#####► DefaultListableBeanFactory：   图书管理员--担当Bean注册管理的角色 
		实现了BeanFactory 和 BeanDefinitionRegistry接口：
		作为一个比较通用的BeanFactory实现，它同时也实现了BeanDefinitionRegistry接口，
		因此它就承担了Bean的注册管理工作
#####► ApplicationContext（容器）
		它构建在BeanFactory之上，
		除了具有BeanFactory的所有能力之外，还提供对事件监听机制以及国际化的支持、统一资源加载策略、多配置模块加载的简化等。
		它管理的bean，在容器启动时全部完成初始化和依赖注入操作。
