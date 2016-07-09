package net.neoremind.dynamicproxy.util;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Sets;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import net.neoremind.dynamicproxy.exception.ObjectProviderException;

/**
 * @author <a href="mailto:xuchen06@baidu.com">xuc</a>
 * @version create on 2015-3-13 上午12:45:22
 */
public abstract class JavassistUtil {

    public static final String DEFAULT_BASE_NAME = "JavassistUtilsGenerated";
    private static final AtomicInteger classNumber = new AtomicInteger(0);
    private static final ClassPool classPool = new ClassPool();
    private static final Set<ClassLoader> classLoaders = Sets.newHashSet();

    static {
        classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
    }

    public static void addField(Class<?> fieldType, String fieldName, CtClass enclosingClass)
            throws CannotCompileException {
        enclosingClass.addField(new CtField(resolve(fieldType), fieldName, enclosingClass));
    }

    public static void addInterfaces(CtClass ctClass, Class<?>[] proxyClasses) {
        for (int i = 0; i < proxyClasses.length; i++) {
            Class<?> proxyInterface = proxyClasses[i];
            ctClass.addInterface(resolve(proxyInterface));
        }
    }

    public static CtClass createClass(Class<?> superclass) {
        return createClass(DEFAULT_BASE_NAME, superclass);
    }

    public static synchronized CtClass createClass(String baseName, Class<?> superclass) {
        return classPool.makeClass(baseName + "_" + classNumber.incrementAndGet(), resolve(superclass));
    }

    public static CtClass resolve(Class<?> clazz) {
        synchronized(classLoaders) {
            try {
                final ClassLoader loader = clazz.getClassLoader();
                if (loader != null && !classLoaders.contains(loader)) {
                    classLoaders.add(loader);
                    classPool.appendClassPath(new LoaderClassPath(loader));
                }

                return classPool.get(ProxyUtil.getJavaClassName(clazz));
            } catch (NotFoundException e) {
                throw new ObjectProviderException("Unable to find class " + clazz.getName()
                        + " in default Javassist class pool.", e);
            }
        }
    }

    public static CtClass[] resolve(Class<?>[] classes) {
        final CtClass[] ctClasses = new CtClass[classes.length];
        for (int i = 0; i < ctClasses.length; ++i) {
            ctClasses[i] = resolve(classes[i]);
        }
        return ctClasses;
    }

}
