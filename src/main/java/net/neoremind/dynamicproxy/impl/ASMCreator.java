package net.neoremind.dynamicproxy.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import net.neoremind.dynamicproxy.Interceptor;
import net.neoremind.dynamicproxy.ObjectInvoker;
import net.neoremind.dynamicproxy.ObjectProvider;
import net.neoremind.dynamicproxy.exception.ProxyCreatorException;
import net.neoremind.dynamicproxy.support.DelegatorInvoker;
import net.neoremind.dynamicproxy.support.InterceptorInvoker;
import net.neoremind.dynamicproxy.support.Invokering;
import net.neoremind.dynamicproxy.template.ClassCache;
import net.neoremind.dynamicproxy.template.GeneratorTemplate;
import net.neoremind.dynamicproxy.template.SubclassCreatorTemplate;

/**
 * ASMCreator
 *
 * @author zhangxu
 */
public class ASMCreator extends SubclassCreatorTemplate {

    protected static final ClassCache PROXY_CLASS_CACHE = new ClassCache(new ProxyGenerator());

    @Override
    public <T> T createDelegatorProxy(ClassLoader classLoader, ObjectProvider<?> delegateProvider,
                                      Class<?>... proxyClasses) {
        return createProxy(classLoader, new DelegatorInvoker(delegateProvider), proxyClasses);
    }

    @Override
    public <T> T createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
                                        Class<?>... proxyClasses) {
        return createProxy(classLoader, new InterceptorInvoker(target, interceptor), proxyClasses);
    }

    @Override
    public <T> T createInvokerProxy(ClassLoader classLoader, ObjectInvoker invoker, Class<?>... proxyClasses) {
        return createProxy(classLoader, new Invokering(invoker), proxyClasses);
    }

    private <T> T createProxy(ClassLoader classLoader, ObjectInvoker invoker, final Class<?>... proxyClasses) {
        Class<?> proxyClass = PROXY_CLASS_CACHE.getProxyClass(classLoader, proxyClasses);
        try {
            @SuppressWarnings("unchecked")
            T result = (T) proxyClass.getConstructor(ObjectInvoker.class).newInstance(invoker);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class ProxyGenerator extends GeneratorTemplate implements Opcodes {
        private static final AtomicInteger CLASS_NUMBER = new AtomicInteger(0);
        private static final String CLASSNAME_PREFIX = "CommonsProxyASM_";
        private static final String HANDLER_NAME = "__handler";
        private static final Type INVOKER_TYPE = Type.getType(ObjectInvoker.class);

        @Override
        public Class<?> generateProxyClass(final ClassLoader classLoader, final Class<?>... proxyClasses) {
            Class<?> superclass = getSuperclass(proxyClasses);
            String proxyName = CLASSNAME_PREFIX + CLASS_NUMBER.incrementAndGet();
            Method[] implementationMethods = getImplementationMethods(proxyClasses);
            Class<?>[] interfaces = toInterfaces(proxyClasses);
            String classFileName = proxyName.replace('.', '/');

            try {
                byte[] proxyBytes = generateProxy(superclass, classFileName, implementationMethods, interfaces);
                return loadClass(classLoader, proxyName, proxyBytes);
            } catch (final Exception e) {
                throw new ProxyCreatorException(e);
            }
        }

        private static byte[] generateProxy(Class<?> classToProxy, String proxyName, Method[] methods,
                                            Class<?>... interfaces) throws ProxyCreatorException {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            Type proxyType = Type.getObjectType(proxyName);

            // push class signature
            String[] interfaceNames = new String[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                interfaceNames[i] = Type.getType(interfaces[i]).getInternalName();
            }

            Type superType = Type.getType(classToProxy);
            cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, proxyType.getInternalName(), null, superType.getInternalName(),
                    interfaceNames);

            // create Invoker field
            cw.visitField(ACC_FINAL + ACC_PRIVATE, HANDLER_NAME, INVOKER_TYPE.getDescriptor(), null, null).visitEnd();

            init(cw, proxyType, superType);

            for (final Method method : methods) {
                processMethod(cw, method, proxyType, HANDLER_NAME);
            }

            return cw.toByteArray();
        }

        private static void init(ClassWriter cw, Type proxyType, Type superType) {
            GeneratorAdapter mg =
                    new GeneratorAdapter(ACC_PUBLIC, new org.objectweb.asm.commons.Method("<init>", Type.VOID_TYPE,
                            new Type[] {INVOKER_TYPE}), null, null, cw);
            // invoke super constructor:
            mg.loadThis();
            mg.invokeConstructor(superType, org.objectweb.asm.commons.Method.getMethod("void <init> ()"));

            // assign handler:
            mg.loadThis();
            mg.loadArg(0);
            mg.putField(proxyType, HANDLER_NAME, INVOKER_TYPE);
            mg.returnValue();
            mg.endMethod();
        }

        // FIXME 虽然使用ASM，但是利用了反射，性能不佳
        private static void processMethod(ClassWriter cw, Method method, Type proxyType, String handlerName)
                throws ProxyCreatorException {
            // Type sig = Type.getType(method);
            Type[] exceptionTypes = getTypes(method.getExceptionTypes());

            // push the method definition
            int access = (ACC_PUBLIC | ACC_PROTECTED) & method.getModifiers();
            org.objectweb.asm.commons.Method m = org.objectweb.asm.commons.Method.getMethod(method);
            GeneratorAdapter mg = new GeneratorAdapter(access, m, null, getTypes(method.getExceptionTypes()), cw);

            Label tryBlock = exceptionTypes.length > 0 ? mg.mark() : null;

            mg.push(Type.getType(method.getDeclaringClass()));

            // the following code generates the bytecode for this line of Java:
            // Method method = <proxy>.class.getMethod("add", new Class[] {
            // <array of function argument classes> });

            // get the method name to invoke, and push to stack

            mg.push(method.getName());

            // create the Class[]
            mg.push(Type.getArgumentTypes(method).length);
            Type classType = Type.getType(Class.class);
            mg.newArray(classType);

            // push parameters into array
            for (int i = 0; i < Type.getArgumentTypes(method).length; i++) {
                // keep copy of array on stack
                mg.dup();

                // push index onto stack
                mg.push(i);
                mg.push(Type.getArgumentTypes(method)[i]);
                mg.arrayStore(classType);
            }

            // invoke getMethod() with the method name and the array of types
            mg.invokeVirtual(classType, org.objectweb.asm.commons.Method
                    .getMethod("java.lang.reflect.Method getDeclaredMethod(String, Class[])"));
            // store the returned method for later

            // the following code generates bytecode equivalent to:
            // return ((<returntype>) invoker.invoke(this, method, new Object[]
            // { <function arguments }))[.<primitive>Value()];

            mg.loadThis();

            mg.getField(proxyType, handlerName, INVOKER_TYPE);
            // put below method:
            mg.swap();

            // we want to pass "this" in as the first parameter
            mg.loadThis();
            // put below method:
            mg.swap();

            // need to construct the array of objects passed in

            // create the Object[]
            mg.push(Type.getArgumentTypes(method).length);
            Type objectType = Type.getType(Object.class);
            mg.newArray(objectType);

            // push parameters into array
            for (int i = 0; i < Type.getArgumentTypes(method).length; i++) {
                // keep copy of array on stack
                mg.dup();

                // push index onto stack
                mg.push(i);

                mg.loadArg(i);
                mg.valueOf(Type.getArgumentTypes(method)[i]);
                mg.arrayStore(objectType);
            }

            // invoke the invoker
            mg.invokeInterface(INVOKER_TYPE, org.objectweb.asm.commons.Method
                    .getMethod("Object invoke(Object, java.lang.reflect.Method, Object[])"));

            // cast the result
            mg.unbox(Type.getReturnType(method));

            // push return
            mg.returnValue();

            // catch InvocationTargetException
            if (exceptionTypes.length > 0) {
                Type caughtExceptionType = Type.getType(InvocationTargetException.class);
                mg.catchException(tryBlock, mg.mark(), caughtExceptionType);

                Label throwCause = new Label();

                mg.invokeVirtual(caughtExceptionType,
                        org.objectweb.asm.commons.Method.getMethod("Throwable getCause()"));

                for (int i = 0; i < exceptionTypes.length; i++) {
                    mg.dup();
                    mg.push(exceptionTypes[i]);
                    mg.swap();
                    mg.invokeVirtual(classType,
                            org.objectweb.asm.commons.Method.getMethod("boolean isInstance(Object)"));
                    // if true, throw cause:
                    mg.ifZCmp(GeneratorAdapter.NE, throwCause);
                }
                // no exception types matched; throw
                // UndeclaredThrowableException:
                int cause = mg.newLocal(Type.getType(Exception.class));
                mg.storeLocal(cause);
                Type undeclaredType = Type.getType(UndeclaredThrowableException.class);
                mg.newInstance(undeclaredType);
                mg.dup();
                mg.loadLocal(cause);
                mg.invokeConstructor(undeclaredType, new org.objectweb.asm.commons.Method("<init>", Type.VOID_TYPE,
                        new Type[] {Type.getType(Throwable.class)}));
                mg.throwException();

                mg.mark(throwCause);
                mg.throwException();
            }

            // finish this method
            mg.endMethod();
        }

        private static Type[] getTypes(Class<?>... src) {
            Type[] result = new Type[src.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = Type.getType(src[i]);
            }

            return result;
        }

        /**
         * Adapted from http://asm.ow2.org/doc/faq.html#Q5
         *
         * @param b
         *
         * @return Class<?>
         */
        private static Class<?> loadClass(ClassLoader loader, String className, byte[] b) {
            // override classDefine (as it is protected) and define the class.
            try {
                Method method =
                        ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class,
                                int.class);

                // protected method invocation
                boolean accessible = method.isAccessible();
                if (!accessible) {
                    method.setAccessible(true);
                }
                try {
                    return (Class<?>) method
                            .invoke(loader, className, b, Integer.valueOf(0), Integer.valueOf(b.length));
                } finally {
                    if (!accessible) {
                        method.setAccessible(false);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
