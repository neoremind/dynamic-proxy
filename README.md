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