package net.neoremind.dynamicproxy.template;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.neoremind.dynamicproxy.MethodSignature;

/**
 * @author zhangxu
 */
public abstract class GeneratorTemplate implements ClassGenerator {

    public static Method[] getImplementationMethods(Class<?>[] proxyClasses) {
        Map<MethodSignature, Method> signatureMethodMap = Maps.newHashMap();
        final Set<MethodSignature> finalizedSignatures = Sets.newHashSet();

        for (int i = 0; i < proxyClasses.length; i++) {
            Class<?> proxyInterface = proxyClasses[i];
            Method[] methods = proxyInterface.getMethods();
            for (int j = 0; j < methods.length; j++) {
                MethodSignature signature = new MethodSignature(methods[j]);
                if (Modifier.isFinal(methods[j].getModifiers())) {
                    finalizedSignatures.add(signature);
                } else if (!signatureMethodMap.containsKey(signature)) {
                    signatureMethodMap.put(signature, methods[j]);
                }
            }
        }

        Collection<Method> resultingMethods = signatureMethodMap.values();
        for (MethodSignature signature : finalizedSignatures) {
            resultingMethods.remove(signatureMethodMap.get(signature));
        }

        return resultingMethods.toArray(new Method[resultingMethods.size()]);
    }
}
