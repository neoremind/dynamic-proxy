# Dynamic proxy for Java

![](https://travis-ci.org/neoremind/dynamic-proxy.svg?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/neoremind/dynamic-proxy/badge.svg?branch=master)](https://coveralls.io/github/neoremind/dynamic-proxy?branch=master)

This following byte-code manipulation techniques can be used for generating proxy:
* ASM
* CGLIB
* Javassist
* JDKDynamicProxy
* ByteBuddy

Examples:
```
public static void testNewSpecificCreator() {
    ProxyCreator proxyCreator = new CglibCreator();
    ObjectInvoker fixedStringObjInvoker = new ObjectInvoker() {
        @Override
        public Object invoke(Object proxy, Method method, Object... arguments) throws Throwable {
            return "Hello world!";
        }
    };
    EchoService service = proxyCreator.createInvokerProxy(fixedStringObjInvoker, EchoService.class);
    assertThat(service.echo("wow"), Matchers.is("Hello world!"));
}

public static void testSpiCreator() {
    ProxyCreator proxyCreator = ProxyUtil.getInstance();
    EchoService service = proxyCreator.createInvokerProxy(new ObjectInvoker() {
        @Override
        public Object invoke(Object proxy, Method method, Object... arguments) throws Throwable {
            return arguments[0];
        }
    }, EchoService.class);
    assertThat(service.echo("wow"), Matchers.is("wow"));
}
```

Create Performace
```
-------------------------------------
| Create proxy number:   10000      |
-------------------------------------
|   ProxyCreator   |    time cost   |
-------------------------------------
| ByteBuddyCreator |    7792ms      |
|       ASMCreator |      51ms      |
|     CglibCreator |     318ms      |
| JavassistCreator |     107ms      |
|  JdkProxyCreator |      53ms      |
-------------------------------------
```

Invoke Performance
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