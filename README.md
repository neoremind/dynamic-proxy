

# Dynamic proxy for Java

![](https://travis-ci.org/neoremind/dynamic-proxy.svg?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/neoremind/dynamic-proxy/badge.svg?branch=master)](https://coveralls.io/github/neoremind/dynamic-proxy?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.neoremind/dynamicproxy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.neoremind/dynamicproxy)
[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Dynamic proxy is a useful library for Java developers to generate proxy object. This library leverages a wide range of byte-code generation methods, including 

* [ASM](http://asm.ow2.org/)
* [CGLIB](https://github.com/cglib/cglib)
* [Javassist](http://jboss-javassist.github.io/javassist/)
* [JDK Dynamic Proxy](https://docs.oracle.com/javase/7/docs/api/java/lang/reflect/Proxy.html)
* [ByteBuddy](http://bytebuddy.net/)

## 1. Guideline
### 1.1 Prerequisite

Maven dependency:

```
<dependency>
    <groupId>net.neoremind</groupId>
    <artifactId>dynamicproxy</artifactId>
    <version>1.0.0</version>
</dependency>
```
Next, let us look at some examples of implementing dynamic proxy. 

### 1.2 Creating invoker
First, let us define an interface.

```
public interface EchoService {
    String echo(String message);
}
```

By using runtime code generation technique, you can create an instance implementing some specific interfaces or extending a class without defining the `Class`.

By initiate `ProxyCreator` instance directly, you can get the specific code generation creator leveraging ASM, Javassist, ByteBuddy, CGLIB and traditional JDK Dynamic Proxy.

`ObjectInvoker` is where method’s behavior is defined. ` createInvokerProxy` method takes two arguments, one is the `ProxyCreator` instance, the other is a var-args which takes multiple interfaces you would like to implement. Note that ByteBuddy currently only supports one interface or class.

Invoker is very useful when implementing a RPC client, caller can depend on an interface, `ProxyCreator` creates a subclass that takes sufficient information to execute a remote call.

```
ProxyCreator proxyCreator = new CglibCreator();
//  ProxyCreator proxyCreator = new ASMCreator();
//  ProxyCreator proxyCreator = new JavassistCreator();
//  ProxyCreator proxyCreator = new ByteBuddyCreator();
//  ProxyCreator proxyCreator = new JdkProxyCreator();

ObjectInvoker fixedStringObjInvoker = new ObjectInvoker() {
    @Override
    public Object invoke(Object proxy, Method method, Object... arguments) throws Throwable {
        return "Hello world!";
    }
};

EchoService service = proxyCreator.createInvokerProxy(fixedStringObjInvoker, EchoService.class);

assertThat(service.echo("wow"), Matchers.is("Hello world!"));
```


### 1.3 Creating interceptor
Interceptors are used to implement cross-cutting concerns, such as logging, auditing, and security, from the business logic.

Here, you can define an `Interceptor` instance, for example you can manipulate the returned string by appending a suffix - "Huh!". `createInterceptorProxy` method which takes three arguments, the first argument is the target object, the second argument is the interceptor, the third argument is the interfaces you would like to proxy.

Note that this time, `ProxyCreator` is created by `DefaultProxyCreator` that looks up implementation of ProxyCreator through SPI, we will talk about this later in chapter 1.5.

```
ProxyCreator proxyCreator = new DefaultProxyCreator();
Interceptor interceptor = new Interceptor() {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return invocation.proceed() + ".Huh!";
    }
};
EchoService service =
        proxyCreator.createInterceptorProxy(new EchoServiceImpl(), interceptor, EchoService.class);
assertThat(service.echo("wow"), Matchers.is("wow.Huh!"));
```

### 1.4 Creating delegator

Delegator is useful when you do not want to expose the real implementation out to the caller or if you would like to change the implementation, for example here we return a decorated object to the caller, the decorated implementation changes the output string to upper case. Usually this reflects the GoF design pattern.

The decorated `EchoService` implementation:

```
class DecoratorEchoService implements EchoService {

    private EchoService delegate;

    public DecoratorEchoService(EchoService delegate) {
        this.delegate = delegate;
    }

    @Override
    public String echo(String message) {
        message = message.toUpperCase();
        return delegate.echo(message);
    }

    public EchoService getDelegate() {
        return delegate;
    }

    public void setDelegate(EchoService delegate) {
        this.delegate = delegate;
    }
}
```

```
ProxyCreator proxyCreator = new DefaultProxyCreator();
ObjectProvider objectProvider = new ObjectProvider() {

    @Override
    public Object getObject() {
        return new DecoratorEchoService(new EchoServiceImpl());
    }
};
EchoService service =
        proxyCreator.createDelegatorProxy(objectProvider, EchoService.class);
assertThat(service.echo("wow"), Matchers.is("WOW"));
```


### 1.5 Using SPI to lookup the capable ProxyCreator

The SPI class `ProxyCreator` can be subclassed into classes including:

* CglibCreator
* ASMCreator
* JavassistCreator
* ByteBuddyCreator
* JdkProxyCreator

You can specify the implementation by creating a file named `net.neoremind.dynamicproxy.ProxyCreator` under `META-INF/services` folder.

The content looks like below, this is also the default configuration.

```
net.neoremind.dynamicproxy.impl.CglibCreator
net.neoremind.dynamicproxy.impl.ByteBuddyCreator
net.neoremind.dynamicproxy.impl.ASMCreator
net.neoremind.dynamicproxy.impl.JdkProxyCreator
net.neoremind.dynamicproxy.impl.JavassistCreator
```

When calling `ProxyUtil.getInstance()`, the capable `ProxyCreator` implementation will be look up automatically.
```
ProxyCreator proxyCreator = ProxyUtil.getInstance();
EchoService service = proxyCreator.createInvokerProxy(new ObjectInvoker() {
    @Override
    public Object invoke(Object proxy, Method method, Object... arguments) throws Throwable {
        return arguments[0];
    }
}, EchoService.class);
assertThat(service.echo("wow"), Matchers.is("wow"));
```


## 2. Performace test
The following performance tests are based on `Invoker` mode. To view source code, please [visit here](https://github.com/neoremind/dynamic-proxy/blob/master/src/test/java/net/neoremind/dynamicproxy/PerformaceTest.java).

Test execution environment:
```
CPU: Intel(R) Core(TM) i5-4278U CPU @ 2.60GHz
MEM: 8G
JVM Args: -Xms512m -Xmx512m -XX:PermSize=128M -XX:MaxPermSize=128M
```
###2.1 Invocation performance test result
####Java6 and 7


```
-------------------------------------
| Invoke number:     1000000        |
-------------------------------------
|   ProxyCreator   |    time cost   |
-------------------------------------
| ByteBuddyCreator |       8ms      |
|       ASMCreator |     718ms      |
|     CglibCreator |      12ms      |
| JavassistCreator |     959ms      |
|  JdkProxyCreator |      20ms      |
-------------------------------------
```

The performance rank is ByteBuddy > CGLIB > JDKProxy > ASM > Javassist.
It has to be highlighted here ASM shouldn’t be so slow, it is because the way invoker is implemented where reflection API is used, that means even ASM is very efficient but byte code that uses reflection API is generated. Using the reflection API is slower than a hard-coded method invocation, JVM needs to perform a rather expensive method lookup to get hold of an object that describes a specific method. And when a method is invoked, this requires the JVM to run native code, which requires long run time compared to a direct invocation.

Moreover, Javassist itself provides two levels of API: source level and bytecode level. Here source level is used, Javassist generates byte code and loads into the JVM at runtime. It is rather slow than bytecode level which implements by manipulating opcodes directly. So JavassistCreator's performance is not very good.


####Java8

```
1.8
-------------------------------------
| Invoke number:     1000000        |
-------------------------------------
|   ProxyCreator   |    time cost   |
-------------------------------------
| ByteBuddyCreator |      13ms      |
|       ASMCreator |     281ms      |
|     CglibCreator |      15ms      |
| JavassistCreator |    1048ms      |
|  JdkProxyCreator |      18ms      |
-------------------------------------
```

Here ASMCreator performs way better under Java8 compared with Java6/7. That is because modern JVM knows a concept called inflation where the JNI-based method invocation is replaced by generated byte code that is injected into a dynamically created class. (Even the JVM itself uses code generation!)

### 2.2 Proxy creation performance test result

```
-------------------------------------
| Create proxy number:   10000      |
-------------------------------------
|   ProxyCreator   |    time cost   |
-------------------------------------
| ByteBuddyCreator |    7567ms      |
|       ASMCreator |      52ms      |
|     CglibCreator |     275ms      |
| JavassistCreator |     102ms      |
|  JdkProxyCreator |      53ms      |
-------------------------------------
```
Result shows no big difference on Java6,7,8.

## 3. Dependencies
By default, the following dependencies will bring into your project.

```
[INFO] +- org.apache.commons:commons-lang3:jar:3.2.1:compile
[INFO] +- commons-collections:commons-collections:jar:3.2.1:compile
[INFO] +- com.google.guava:guava:jar:18.0:compile
[INFO] +- org.javassist:javassist:jar:3.18.1-GA:compile
[INFO] +- cglib:cglib-nodep:jar:2.2.2:compile
[INFO] +- cn.wensiqun:asmsupport-core:jar:0.4.3:compile
[INFO] |  \- cn.wensiqun:asmsupport-standard:jar:0.4.3:compile
[INFO] |     \- cn.wensiqun:asmsupport-third:jar:0.4.3:compile
[INFO] +- org.ow2.asm:asm-commons:jar:5.0.4:compile
[INFO] |  \- org.ow2.asm:asm-tree:jar:5.0.4:compile
[INFO] +- org.ow2.asm:asm:jar:5.0.4:compile
```

##4. Acknowledgment

The development of dynamic-proxy was inspired by [Apache commons-proxy](http://commons.apache.org/proper/commons-proxy/) and made some performance improvements like leveraging cache and introducing ByteBuddy into the library.

## 5. Appendix
[An useful introduction to bytecode tools](http://stackoverflow.com/questions/2261947/are-there-alternatives-to-cglib)