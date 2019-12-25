# 2019-11-25周 #
### 1、BeanDefinition是什么?为什么要有这个?这个对我们写代码有什么借鉴意义

BeanDefinition描述了一个bean实例，BeanDefinition中有很多Bean的信息，例如类名、scope、属性、构造函数参数列表、依赖的bean、是否是单例类、是否是懒加载等，其实就是将Bean的定义信息存储到这个BeanDefinition相应的属性中。

这只是一个最小的接口：主要目的是允许BeanFactoryPostProcessor（例如PropertyPlaceholderConfigurer）内省和修改属性值和其他bean元数据。（即后面对Bean的操作就直接对BeanDefinition进行，例如拿到这个BeanDefinition后，可以根据里面的类名、构造函数、构造函数参数，使用反射进行对象创建）

借鉴意义，我觉得他就像一个beanBuilder

### 2.从容器角度,将了解到的概念,用一个简单直接的话或图描述
1. 获取 BeanFactory
    - （1）先初始化 BeanFactory
    - （2）然后通过 XmlBeanDefinitionReader 将 xml 中的配置读取，放到 BeanDefinitionHolder 中
    - （3）BeanDefinitionHolder 创建 BeanDefinition，并注册到 BeanDefinitionRegistry 中（DefaultListableBeanFactory 实现了 BeanDefinitionRegistry，这里的 BeanDefinitionRegistry 指的就是 DefaultListableBeanFactory）。
2. 往容器中注册 postProcessorBeanFactory。
3. 根据顺序触发 BeanDefinitionRegistryPostProcessor 以及 BeanFactoryPostProcessor。
4. 注册 BeanPostProcessor。
5. 初始化 initMessageSource 以及 ApplicationEventMulticaster。
6. 注册 listeners（我们自定义的Listener 在这时候就注册到容器中了）。
7. 加载非惰性实例（我们自定义的 bean 在这个时候被加载）。
8. 加载 lifecycleProcessor 以及触发对应的事件，发布 ContextRefreshedEvent 事件。




# 2019-12-02周 #
### 1. 已知Spring可以解决单例模式，属性中循环依赖，Setter注入的循环依赖问题，但是为什么Spring只能解决这个特定的依赖问题，解决它所用到的原理大致介绍一下。

#### 1. 什么是循环依赖？
循环依赖其实就是循环引用，也就是两个或者两个以上的**bean互相持有对方**，最终形成**闭环**。比如A依赖于B，B依赖于C，C又依赖于A。

注意，这里**不是**函数的循环调用，是对象的**相互依赖关系**。循环调用其实就是一个死循环，除非有终结条件。

Spring中循环依赖场景有： 
##### （1）构造器的循环依赖 ：
- 构造器的循环依赖问题**无法解决**，只能拋出**BeanCurrentlyInCreationException**异常
##### （2）field属性的循环依赖
- 在解决属性循环依赖时，spring采用的是**提前暴露对象**的方法。

#### 2. 怎么检测是否存在循环依赖
检测循环依赖相对比较容易，Bean在创建的时候可以给该Bean**打标**，如果递归调用回来发现**正在创建中**的话，即说明了循环依赖了。

#### 3. Spring怎么解决循环依赖
Spring的循环依赖的理论依据基于J**ava的引用传递**，当获得对象的引用时，对象的属性是可以延后设置的。（但是构造器必须是在获取引用之前）。

**Spring的单例对象的初始化主要分为三步：**

- （1）createBeanInstance：实例化，其实也就是调用对象的构造方法实例化对象

- （2）populateBean：填充属性，这一步主要是多bean的依赖属性进行填充

- （3）initializeBean：调用spring xml中的init 方法。

从上面单例bean的初始化可以知道：**循环依赖主要发生在第一、二步**，也就是构造器循环依赖和field循环依赖。那么我们要解决循环引用也应该从初始化过程着手，对于单例来说，在Spring容器整个生命周期内，有且只有一个对象，所以很容易想到这个对象应该存在Cache中，Spring为了解决单例的循环依赖问题，使用了三级缓存。这三级缓存分别指： 
* **singletonFactories** ： ObjectFactory的cache
* **earlySingletonObjects** ：提前暴光的单例对象的Cache 
* **singletonObjects**：单例对象的cache

在创建bean的时候，首先想到的是从cache中获取这个单例的bean，这个缓存就是**singletonObjects**。如果获取不到，并且对象正在创建中，就再从二级缓存earlySingletonObjects中获取。如果还是获取不到且允许singletonFactories通过getObject()获取，就从三级缓存singletonFactory.getObject()(三级缓存)获取，如果获取到了则：从**singletonFactories中移除**，并放入earlySingletonObjects中。其实也就是从**三级缓存移动到了二级缓存**。

从上面三级缓存的分析，我们可以知道，Spring解决循环依赖的诀窍就在于singletonFactories这个三级cache。这个cache的类型是ObjectFactory。这里就是**解决循环依赖的关键**，发生在createBeanInstance之后，也就是说单例对象此时已经被创建出来(调用了构造器)。这个对象已经被生产出来了，虽然还不完美（还没有进行初始化的第二步和第三步），但是已经能被人认出来了（根据对象引用能定位到堆中的对象），所以Spring此时将这个对象提前曝光出来让大家认识，让大家使用。

_`（这样做有什么好处呢？让我们来分析一下“A的某个field或者setter依赖了B的实例对象，同时B的某个field或者setter依赖了A的实例对象”这种循环依赖的情况。A首先完成了初始化的第一步，并且将自己提前曝光到singletonFactories中，此时进行初始化的第二步，发现自己依赖对象B，此时就尝试去get(B)，发现B还没有被create，所以走create流程，B在初始化第一步的时候发现自己依赖了对象A，于是尝试get(A)，尝试一级缓存singletonObjects(肯定没有，因为A还没初始化完全)，尝试二级缓存earlySingletonObjects（也没有），尝试三级缓存singletonFactories，由于A通过ObjectFactory将自己提前曝光了，所以B能够通过ObjectFactory.getObject拿到A对象(虽然A还没有初始化完全，但是总比没有好呀)，B拿到A对象后顺利完成了初始化阶段1、2、3，完全初始化之后将自己放入到一级缓存singletonObjects中。此时返回A中，A此时能拿到B的对象顺利完成自己的初始化阶段2、3，最终A也完成了初始化，进去了一级缓存singletonObjects中，而且更加幸运的是，由于B拿到了A的对象引用，所以B现在hold住的A对象完成了初始化。
知道了这个原理时候，肯定就知道为啥Spring不能解决“A的构造方法中依赖了B的实例对象，同时B的构造方法中依赖了A的实例对象”这类问题了！因为加入singletonFactories三级缓存的前提是执行了构造器，所以构造器的循环依赖没法解决。）`_

#### 4. 基于构造器的循环依赖
Spring容器会将每一个正在创建的Bean 标识符放在一个“**当前创建Bean池**”中，Bean标识符在创建过程中将一直保持在这个池中，因此如果在创建Bean过程中发现自己已经在“当前创建Bean池”里时将抛出BeanCurrentlyInCreationException异常表示循环依赖；而对于创建完毕的Bean将从“当前创建Bean池”中清除掉。

_`（Spring容器先创建单例A，A依赖B，然后将A放在“当前创建Bean池”中，此时创建B,B依赖C ,然后将B放在“当前创建Bean池”中,此时创建C，C又依赖A， 但是，此时A已经在池中，所以会报错，，因为在池中的Bean都是未初始化完的，所以会依赖错误 ，（初始化完的Bean会从池中移除））`_

#### 5. 基于setter属性的循环依赖
![依赖](img/1.png)

我们结合上面那张图看，Spring先是用构造实例化Bean对象 ，创建成功后，Spring会通过以下代码提前将对象暴露出来，此时的对象A还没有完成属性注入，属于早期对象，此时Spring会将这个实例化结束的对象放到一个Map中，并且Spring提供了获取这个未设置属性的实例化对象引用的方法。 结合我们的实例来看，当Spring实例化了A、B、C后，紧接着会去设置对象的属性，此时A依赖B，就会去Map中取出存在里面的单例B对象，以此类推，不会出来循环的问题。


### 2. Spring的lookup-method 和 replace-method 用的不多，但是也有一定的应用场景。简单说一下我们实际可以运用的场景。
@Lookup是lookup-method的注解版本，在方法或者抽象方法上使用@Lookup注解，将会根据该方法的返回值，自动在BeanFactory中调用getBean()来注入该Bean。
可以用 @Autowire @Qualifier替换

我觉得最好不要用，虽然看起来很操作很简单，但是增加了查看代码难度。如果不了解原理，可能会导致未知问题。

# 2019-12-09周 #
### 1、Spring起来后有几个spring容器？只有一个吗？

一个web应用会有一个全局的上下文环境，上下为 **ServletContext**，其为后面的spring IoC容器提供宿主环境。

Spring 是通过 web.xml 中的 contextLoaderListener 监听触发容器启动的，在这个 listener 中会创建一个跟上下文--WebApplicationContext。

`// todo 等看完 springMVC 再来完善`

在 SpringMVC 启动时又会创建 WebApplicationContext 的子容器。 

### 2、BeanDefinitionRegistryPostProcessor、BeanFactoryPostProcessor、BeanPostProcessor（这个没看到可以先留着）在什么时候触发？

    
    org.springframework.context.support.AbstractApplicationContext
    @Override
    public void refresh() throws BeansException, IllegalStateException {
       synchronized (this.startupShutdownMonitor) {
          ...
          // Tell the subclass to refresh the internal bean factory.
          // 告诉子类刷新内部的bean工厂。
          ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
    
          // Prepare the bean factory for use in this context.
          // 准备在该上下文下使用的bean工厂。
          prepareBeanFactory(beanFactory);
    
          try {
             // Allows post-processing of the bean factory in context subclasses.
             // 允许在上下文子类中对bean工厂进行后处理。
             postProcessBeanFactory(beanFactory);
    
    
             // Invoke factory processors registered as beans in the context.
             // 在上下文中调用注册为bean的工厂处理器。
             // 这里有实例化部分bean。（BeanDefinitionRegistryPostProcessor 以及 BeanFactoryPostProcessor 类型）
             // 这里提供了修改 beanFactory 的spi（BeanDefinitionRegistryPostProcessor 以及 BeanFactoryPostProcessor 类型的实现即可）
             invokeBeanFactoryPostProcessors(beanFactory);
             
             // Register bean processors that intercept bean creation.
             // 注册 BeanPostProcessor
             registerBeanPostProcessors(beanFactory);
    ...

#### 2.1 BeanDefinitionRegistryPostProcessor、BeanFactoryPostProcessor注册触发
上面这段代码中的 **invokeBeanFactoryPostProcessors** 中就是触发 BeanDefinitionRegistryPostProcessor 和 BeanFactoryPostProcessor 的逻辑。

#### 2.2 BeanPostProcessor注册触发
`// todo 等看完 getBean 再来完善`

**registerBeanPostProcessors** 就是BeanPostProcessor的注册，至于触发是在Bean实例化以及初始化前后触发的。

### 3、BeanDefinitionRegistryPostProcessor、BeanFactoryPostProcessor 触发是否有顺序，如果有顺序，那么是以怎么样的顺序进行触发的？

#### 3.1. BeanDefinitionRegistryPostProcessor 和 BeanFactoryPostProcessor

PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors()); 就触发了所有的 BeanDefinitionRegistryPostProcessor 处理以及 BeanFactoryPostProcessor 处理。具体代码太长了就不贴出来了。总体分成以下几步

* BeanFactory 是否属于 BeanDefinitionRegistry 类型
    * 如果 BeanFactory 属于 BeanDefinitionRegistry 类型
        * 先将 BeanFactory 中 BeanDefinitionRegistryPostProcessor（已实例化并注册到 BeanFacotry 中的） 筛选出来，存在缓存 registryProcessors 中，并运行 processor 的 postProcessBeanDefinitionRegistry。BeanFactory 中其余的 BeanFactoryPostProcessor，存在缓存 regularPostProcessors（**只继承了BeanFactoryPostProcessor**） 中。
        * 从 BeanFactory 中获取所有类型为 BeanDefinitionRegistryPostProcessor 类型的 BeanNames 。根据 **priorityOrder、Order**、**普通顺序触发 postProcessBeanDefinitionRegistry**（注意，**_PriorityOrder 级别筛选完成之后触发了事件，需要重新从容器中加载 BeanNames，因为触发的后置事件可能往容器中添加了 BeanDefinitionRegistryPostProcessor_**）
        * 最后再调用 BeanDefinitionRegistryPostProcessor 的 postProcessBeanFactory 方法（**顺序还是priorityOrder、Order、普通顺序**）
        * 调用 regularPostProcessors 的 postProcessBeanFactory 方法
    * 如果 BeanFactory 不属于 BeanDefinitionRegistry 类型
        * 触发 beanFactory 中所有 BeanFactoryPostProcessor 的 postProcessBeanFactory
* 从容器中获取 BeanFactoryPostProcessor 类型的所有 BeanNames ，然后根据 priorityOrder、Order、**普通顺序**触发postProcessBeanFactory

#### 3.2. BeanPostProcessor

PostProcessorRegistrationDelegate 中注册 BeanPostProcessor 的注册步骤和上面触发 BeanDefinitionRegistryPostProcessor 以及 BeanFactoryPostProcessor 基本一样，这里的 BeanPostProcessor 只是注册，并没有触发他的 postProcess 操作。具体步骤如下：
* 从 beanFactory 中获取 BeanPostProcessor 类型的 BeanNames
* 根据 **priorityOrder、Order、普通顺序实例化初始化注册到容器中**



### @Compont @Service 系列什么时候实例化
是在解析xml中的自定义标签的时候，通过ContextNamespaceHandler初始化出来的ComponentScanBeanDefinitionParser进行扫描指定包，找出带@Compont、@Service标签的类，并将其实例化。

# 2019-12-16周 #
### 1:BeanFactory和ApplicationContext的区别
* Spring提供了两种容器，一是BeanFactory，一个是ApplicationContext应用上下文。
* BeanFactory是spring比较原始 的Factory，例如xmlBeanFactory是一种典型的BeanFactory。原始的BeanFactory无法支持spring的许多插件，比如AOP功能，Web应用等。
* ApplicationContext接口，继承于BeanFactory，能够提供BeanFactory的所有功能，它是一种更加面向框架的工作方式，此外ApplicationContext还提供 了如下功能：MessageSource，提供国际化支持；资源访问，例如url和文件；事件传播；载入多个（有继承关系的上下文），使每个上下文都有一个特定的层次。比如Web。
  - （1）**相同点**
    - 都是通过xlm来加载factory容器。
  - （2）**不同点**
    - Bean何时加载？
      * BeanFactory是采用延时加载来注入Bean的，即只有在使用某个Bean时，调用getBean方法，来对该Bean进行加载实例化。
      * ApplicationContext则相反， 它是在容器启动时，一次创建所有的Bean，这样，在容器启动时，我们就可以发现Spring配置中存在的问题。
    - **Applicationc 相比Beanfactory提供了更多的功能**
      *   国际化支持
      *   资源访问：Resource rs = ctx. getResource(“classpath:config.properties”), “file:c:/config.properties”
      *   事件传递：通过实现ApplicationContextAware接口
      
### 2:ApplicationContext 上下文的生命周期
