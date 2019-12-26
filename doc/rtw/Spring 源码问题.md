
# ONE # 
## BeanDefinition是什么?为什么要有这个?这个对我们写代码有什么借鉴意义
1. BeanDefinition的官方解释：BeanDefinition接口顶级基础接口,用来描述Bean,里面存放Bean元数据，比如Bean类名、scope、属性、构造函数参数列表、依赖的bean、是否是单例类、是否是懒加载等一些列信息。
2. 也就是说我们通过xml，注解的方式会对bean进行配置，这些配置信息都保存在BeanDefinition中。一般在具体初始化中都会用到RootBeanDefinition，是BeanDefinition的子类

3. 借鉴意义：把所有包含的信息都放在BeanDefinition中，后续进行创建bean的话只需要这么一个对象即可。



## 从容器角度,将了解到的概念,用一个简单直接的话或图描述

1. 容器是帮助我们管理大量bean的空间，但是需要我们指定哪些Bean，Bean的依赖等。这都是需要用户自己指定的。容器需要把xml、注解转化为BeanDefinition。
2. BeanDefinition又会在FactoryBean中创建成BeanInstance。该类就是最后成型的bean，我们可以直接使用这个bean。





# TWO #

## 已知Spring可以解决单例模式，属性中循环依赖，Setter注入的循环依赖问题，但是为什么Spring只能解决这个特定的依赖问题，解决它所用到的原理大致介绍一下。


#### 1. 什么是循环依赖？
循环依赖其实就是循环引用，也就是两个或者两个以上的**bean互相持有对方**，最终形成**闭环**。比如A依赖于B，B依赖于C，C又依赖于A。

注意，这里**不是**函数的循环调用，是对象的**相互依赖关系**。循环调用其实就是一个死循环，除非有终结条件。

Spring中循环依赖场景有： 
##### （1）构造器的循环依赖 ：
- 构造器的循环依赖问题**无法解决**，只能拋出**BeanCurrentlyInCreationException**异常
##### （2）field属性的循环依赖
- 在解决属性循环依赖时，spring采用的是**提前暴露对象**的方法。

#### 2. 怎么检测是否存在循环依赖
检测循环依赖相对比较容易，Bean在创建的时候可以给该Bean**打标**，如果递归调用回来发现**正在创建中**的话，即说明了循环依赖了。

## 3. Spring怎么解决循环依赖
Spring的循环依赖的理论依据基于**Java的引用传递**，当获得对象的引用时，对象的属性是可以延后设置的。（但是构造器必须是在获取引用之前）。

### 初始化步骤

1. Spring的单例对象的初始化主要分为三步：

- createBeanInstance：实例化，其实也就是调用对象的构造方法实例化对象
- populateBean：填充属性，这一步主要是多bean的依赖属性进行填充
- initializeBean：调用spring xml中的init 方法。

2. 从上面单例bean的初始化可以知道：**循环依赖主要发生在第一、二步**，也就是构造器循环依赖和field循环依赖。那么我们要解决循环引用也应该从初始化过程着手，对于单例来说，在Spring容器整个生命周期内，有且只有一个对象，所以很容易想到这个对象应该存在Cache中，Spring为了解决单例的循环依赖问题，使用了三级缓存。这三级缓存分别指： 

* **singletonFactories** ： 单例对象工厂的cache 
* **earlySingletonObjects** ：提前暴光的单例对象的Cache,只会通过singletonFactories的factory的getObject获得。（addSingletonFactory）
* **singletonObjects**：单例对象的cache

3. 在创建bean的时候，首先想到的是从cache中获取这个单例的bean，这个缓存就是**singletonObjects**。如果获取不到，并且对象正在创建中，就再从二级缓存earlySingletonObjects中获取。

   - 如果还是获取不到且允许singletonFactories通过getObject()获取，就从三级缓存singletonFactory.getObject()(三级缓存)获取，如果获取到了则：从**singletonFactories中移除**，调用其getObject() 并放入earlySingletonObjects中。其实也就是从**三级缓存移动到了二级缓存**。

   - > ```java
     > // 为了避免后期循环依赖，可以在bean初始化完成前将创建实例的ObjectFactory加入工厂
     > addSingletonFactory(beanName,
     >       // 主要用于解决循环引用问题，对bean再一次依赖引用，主要应用SmartInstantiationAware BeanPostProcessor
     >       // 其中我们熟知的AOP就是在这里将advice动态织入bean中，若没有则直接返回bean，不做任何处理。
     >       () -> getEarlyBeanReference(beanName, mbd, bean)
     > );	
     > ```

4. 从上面三级缓存的分析，我们可以知道，Spring解决循环依赖的诀窍就在于singletonFactories这个三级cache。这个cache的类型是ObjectFactory。这里就是**解决循环依赖的关键**，发生在createBeanInstance之后，也就是说单例对象此时已经被创建出来(调用了构造器)。这个对象已经被生产出来了，虽然还不完美（还没有进行初始化的第二步和第三步），但是已经能被人认出来了（根据对象引用能定位到堆中的对象），所以Spring此时将这个对象提前曝光出来让大家认识，让大家使用。

### 实例分析

1. 这样做有什么好处呢？让我们来分析一下“A的某个field或者setter依赖了B的实例对象，同时B的某个field或者setter依赖了A的实例对象”这种循环依赖的情况。
2. A首先完成了初始化的第一步，并且将自己提前曝光到singletonFactories中，此时进行初始化的第二步，发现自己依赖对象B，此时就尝试去get(B)，发现B还没有被create，所以走create流程，B在初始化第一步的时候发现自己依赖了对象A，于是尝试get(A)，尝试一级缓存singletonObjects(肯定没有，因为A还没初始化完全)，尝试二级缓存earlySingletonObjects（也没有），尝试三级缓存singletonFactories，由于A通过ObjectFactory将自己提前曝光了，**所以B能够通过ObjectFactory.getObject拿到A对象(虽然A还没有初始化完全，但是总比没有好呀)**，B拿到A对象后顺利完成了初始化阶段1、2、3，完全初始化之后将自己放入到一级缓存singletonObjects中。此时返回A中，A此时能拿到B的对象顺利完成自己的初始化阶段2、3，最终A也完成了初始化，进去了一级缓存singletonObjects中，而且更加幸运的是，由于B拿到了A的对象引用，所以B现在hold住的A对象完成了初始化。
3. 知道了这个原理时候，肯定就知道为啥Spring不能解决“A的构造方法中依赖了B的实例对象，同时B的构造方法中依赖了A的实例对象”这类问题了！因为加入singletonFactories三级缓存的前提是执行了构造器，所以构造器的循环依赖没法解决。

#### 4. 基于构造器的循环依赖
1. Spring容器会将每一个正在创建的Bean 标识符放在一个“**当前创建Bean池**”中，Bean标识符在创建过程中将一直保持在这个池中，因此如果在创建Bean过程中发现自己已经在“当前创建Bean池”里时将抛出BeanCurrentlyInCreationException异常表示循环依赖；而对于创建完毕的Bean将从“当前创建Bean池”中清除掉。
2. Spring容器先创建单例A，A依赖B，然后将A放在“当前创建Bean池”中，此时创建B,B依赖C ,然后将B放在“当前创建Bean池”中,此时创建C，C又依赖A， 但是，此时A已经在池中，所以会报错，，因为在池中的Bean都是未初始化完的，所以会依赖错误 ，（初始化完的Bean会从池中移除）

#### 5. 基于setter属性的循环依赖
![处理循环依赖](http://rtt-picture.oss-cn-hangzhou.aliyuncs.com/2019-12-13-010756.png)

我们结合上面那张图看，Spring先是用构造实例化Bean对象 ，创建成功后，Spring会通过以下代码提前将对象暴露出来，此时的对象A还没有完成属性注入，属于早期对象，此时Spring会将这个实例化结束的对象放到一个Map中，并且Spring提供了获取这个未设置属性的实例化对象引用的方法。 结合我们的实例来看，当Spring实例化了A、B、C后，紧接着会去设置对象的属性，此时A依赖B，就会去Map中取出存在里面的单例B对象，以此类推，不会出来循环的问题。






## Spring的lookup-method 和 replace-method 用的不多，但是也有一定的应用场景。简单说一下我们实际可以运用的场景。

1. lookup-method 和 replaced-method 是在 xml 配置bean的时候的  可选配置。
   - lookup-method可以声明方法返回某个特定的bean。
   - replaced-method可以改变某个方法甚至改变方法的逻辑。

2. 也就是说我们可以通过改变xml进而修改某个接口的实现类，或者某一个方法，可以实现快速切换实现类。一定情况可以替代一下if  else。比如上某个功能，新功能新写一个类，使用xml来控制两者的使用。



# THREE

## 1、Spring起来后有几个spring容器？只有一个吗？

1. 最为常见的场景是在一个项目中引入Spring和SpringMVC这两个框架，其本质就是两个容器：Spring是根容器，SpringMVC是其子容器。



### 启动过程

1. 对于一个web应用，其部署在web容器中，web容器提供其一个全局的上下文环境，这个上下文就是ServletContext。（用于存放在Tomcat容器中的对象）

2. 在web.xml中会提供有contextLoaderListener。在web容器启动时，会触发容器初始化事件，这时候contextLoaderListener会监听到这个事件，其contextInitialized方法会被调用。在这个方法中，Spring会初始化一个启动上下文，这个上下文被称为根上下文，即WebApplicationContext。

3. ContextLoaderListener监听器初始化完毕后，开始初始化web.xml中配置的Servlet，这个servlet可以配置多个，以最常见的DispatcherServlet为例，这个servlet实际上是一个标准的前端控制器，用以转发、匹配、处理每个servlet请求。

4. **DispatcherServlet上下文在初始化的时候会建立自己的IoC上下文，用以持有spring mvc相关的bean。特别地，在建立DispatcherServlet自己的IoC上下文前，会利用WebApplicationContext.ROOTWEBAPPLICATIONCONTEXTATTRIBUTE先从ServletContext中获取之前的根上下文(即WebApplicationContext)作为自己上下文的parent上下文**。

   



### 作用范围

1. **子容器(SpringMVC容器)可以访问父容器(Spring容器)的Bean，父容器(Spring容器)不能访问子容器(SpringMVC容器)的Bean**。也就是说，当在SpringMVC容器中getBean时，如果在自己的容器中找不到对应的bean，则会去父容器中去找，这也解释了为什么由SpringMVC容器创建的Controller可以获取到Spring容器创建的Service组件的原因。



## 2、BeanDefinitionRegistryPostProcessor、BeanFactoryPostProcessor 触发是否有顺序，如果有顺序，那么是以怎么样的顺序进行触发的？

1. 详见Spring容器的初始化:

   > org.springframework.context.support.PostProcessorRegistrationDelegate#registerBeanPostProcessors(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, org.springframework.context.support.AbstractApplicationContext)

2. 总结下来就是：
   - 先执行BeanDefinitionRegistryPostProcessor
   - 后执行BeanFactoryPostProcesso
3. 其中如果两者是有多个相同类型的，则更具优先级的顺序进行处理。





# FOUR

## 1. BeanFactory和ApplicationContext的区别

1. ApplicationContext包含BeanFactory的所有功能。一般建议直接使用ApplicationContext。
2. 以下列举了ApplicationContext相较于BeanFactory多出来的部分
   - 国际化处理
   - 优先级加载BeanFactory
   - 支持Spel的支持
   - 事件监听器
   - 容器各种事件的发布机制
   - 支持生命周期的相关接口，如SmartLifecycle
   - 如果配置的Bean是非懒加载，则会在启动的时候就把这些bean初始化，不像BeanFactory一样需要getBean后才初始化。





## 2. ApplicationContext 上下文的生命周期

https://www.cnblogs.com/kenshinobiy/p/4652008.html

### BeanFactory 中的 Spring Bean的上下文生命周期

1. 验证可能覆盖的方法(lookup-method, replaced-method)。
   - prepareMethodOverrides方法。
2. 处理继承InstantiationAwareBeanPostProcessor的bean，这是可以直接返回一个完整的bean的最后一个机会。
   - resolveBeforeInstantiation方法。
3. 填充属性populateBean，根据类型自动注入，根据名称自动注入。如果有依赖别的bean则会进行循环调用。
   - populateBean 循环依赖调用其依赖的bean。
4. 对特殊的bean处理：BeanNameAware，BeanFactoryAware
   - invokeAwareMethods方法，如果发现bean实现了一些Aware接口，则将一些beanName, BeanFactory塞进去。
5. 应用前置处理器postProcessBeforeInitialization
   - applyBeanPostProcessorsBeforeInitialization
6. 应用激活InitializingBean##afterPropertiesSet方法
   - invokeInitMethods
7. 应用激活用户自定义的init方法，Bean定义文件中定义init-method
   - invokeInitMethods
8. 应用后置处理器postProcessAfterInitialization
   - applyBeanPostProcessorsAfterInitialization
9. 可以使用啦！！！
   - 尽情的享用Spring吧
10. 调用  DisposableBean#destroy  方法
11. 调用destroy - method 用户配置的销毁方法



### ApplicationContext  中的 Spring Bean的上下文生命周期

1. 因为ApplicationContext的最后会进行调用BeanFactory的getBean。只不过之前会给其加上一些ApplicationContext特有的BeanPostProcessor。
   - **比如说会在prepareBeanFactory中加上ApplicationContextAwareProcessor。这就是在BeanFactory中所没有的。**可以让bean中含有ApplicationContext







# Five

##spring提供的BeanPostProcessor主要有哪些？各自的作用

###  常用

1. AbstractAutoProxyCreator 就是在postProcessAfterInitialization中将bean进行代理，实现其AOP功能。
2. AutowiredAnnotationBeanPostProcessor就是应该识别@Autowired的关键实现



### 作用

1. 如果我们需要进行一些bean在初始化前或初始化后进行的操作都可以让bean继承BeanPostProcessor后重写具体的方法。
   - Spring容器在初始化bean的时候会自动调用我们bean实现的BeanPostProcessor



### 源码角度

1. 在Spring容器初始化的时候会获取到所有实现BeanPostProcessor的bean,然后注册到beanFactory中去(registerBeanPostProcessors)
2. 具体什么时候调用BeanPostProcessor的前后置处理器呢，是在BeanFactory.getBean中获取到的。







##spring的监听器是怎么注册的？在何时注册的？

1. Spring源码有一个EventListenerMethodProcessor类，继承自SmartInitializingSingleton，主要是用来初始化单例bean的，EventListenerMethodProcessor顾名思义就是将EventListener注释方法注册为单独的ApplicationEvent实例。

2. EventListenerMethodProcessor##afterSingletonsInstantiated方法中会对每个bean都调用processBean。

```java
private void processBean(final String beanName, final Class<?> targetType) {
		if (!this.nonAnnotatedClasses.contains(targetType) &&
				AnnotationUtils.isCandidateClass(targetType, EventListener.class) &&
				!isSpringContainerClass(targetType)) {

			Map<Method, EventListener> annotatedMethods = null;
			try {
				// 获取对应bean上方法级别的注解
				annotatedMethods = MethodIntrospector.selectMethods(targetType,
						(MethodIntrospector.MetadataLookup<EventListener>) method ->
								AnnotatedElementUtils.findMergedAnnotation(method, EventListener.class));
			}
			.......省略......
			else {
				// Non-empty set of methods
				ConfigurableApplicationContext context = this.applicationContext;
				Assert.state(context != null, "No ApplicationContext set");
				// 取出两个factories，分别为TransactionEventListenerFactory 和 EventListenerMethodProcessor
				// 应该对应两个@EventListner 和 @TransactionalEventListener
				List<EventListenerFactory> factories = this.eventListenerFactories;
				Assert.state(factories != null, "EventListenerFactory List not initialized");
				for (Method method : annotatedMethods.keySet()) {
					for (EventListenerFactory factory : factories) {
						if (factory.supportsMethod(method)) {
							Method methodToUse = AopUtils.selectInvocableMethod(method, context.getType(beanName));
							ApplicationListener<?> applicationListener =
									factory.createApplicationListener(beanName, targetType, methodToUse);
							if (applicationListener instanceof ApplicationListenerMethodAdapter) {
								((ApplicationListenerMethodAdapter) applicationListener).init(context, this.evaluator);
							}
							// 封装成applicationListener后添加到容器的ApplicationListener中
							context.addApplicationListener(applicationListener);
							break;
						}
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug(annotatedMethods.size() + " @EventListener methods processed on bean '" +
							beanName + "': " + annotatedMethods);
				}
			}
		}
	}
```

- 也就是说如果发现方法上有@EventListner 和 @TransactionalEventListener 会将其使用context.addApplicationListener()添加到监听器容器中去。









##spring事件监听的实现原理

1. Dubbo的服务提供方也是利用了该原理进行实现.....具体需要在进行深入了解

### 发送事件触发流程

1. org.springframework.context.support.AbstractApplicationContext#publishEvent(java.lang.Object, org.springframework.core.ResolvableType)
2. org.springframework.context.event.SimpleApplicationEventMulticaster#multicastEvent(org.springframework.context.ApplicationEvent, org.springframework.core.ResolvableType)

```java
public void multicastEvent(final ApplicationEvent event, ResolvableType eventType) {
		ResolvableType type = (eventType != null ? eventType : resolveDefaultEventType(event));
		for (final ApplicationListener<?> listener : getApplicationListeners(event, type)) {
      // 需
			Executor executor = getTaskExecutor();
			if (executor != null) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						invokeListener(listener, event);
					}
				});
			}
			else {
				invokeListener(listener, event);
			}
		}
	}
```

- invokeListener中调用listener.onApplicationEvent，时会判断当前listener是否可以接受对应的event，然后才会具体调用相对应的代理proxy类。

3. 那么又引出一个问题@Async("asyncEventTaskExecutor")这个功能是在哪里实现的呢。大致判断是在生成具体的bean的proxy时，添加了AsyncExecutionInterceptor拦截器。
   - 熟悉AOP的都知道，看一下AsyncExecutionInterceptor的invoker方法就是我们的callback

```java
public Object invoke(final MethodInvocation invocation) throws Throwable {
   Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
   Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
   final Method userDeclaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

   // 我们使用@Async("asyncEventTaskExecutor")使用的线程池
   AsyncTaskExecutor executor = determineAsyncExecutor(userDeclaredMethod);
   if (executor == null) {
      throw new IllegalStateException(
            "No executor specified and no default executor set on AsyncExecutionInterceptor either");
   }

   // 生成一个callable方法，放到线程池中运行
   Callable<Object> task = () -> {
      try {
         Object result = invocation.proceed();
         if (result instanceof Future) {
            return ((Future<?>) result).get();
         }
      }
      catch (ExecutionException ex) {
         handleError(ex.getCause(), userDeclaredMethod, invocation.getArguments());
      }
      catch (Throwable ex) {
         handleError(ex, userDeclaredMethod, invocation.getArguments());
      }
      return null;
   };

   return doSubmit(task, executor, invocation.getMethod().getReturnType());
}
```

4. 如果我们不发送event来触发eventListner的话，直接通过调用eventListner的方法。
   - 如果在对应方法上添加了@Async("asyncEventTaskExecutor")，那么任然会异步运行。














