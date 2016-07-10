package net.neoremind.dynamicproxy;

import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.hamcrest.Matchers;
import org.junit.Test;

import net.neoremind.dynamicproxy.sample.Echo;
import net.neoremind.dynamicproxy.util.ReflectionUtil;

/**
 * @author zhangxu
 */
public class MethodSignatureTest {

    @Test
    public void testMethod() {
        Method m = ReflectionUtil.getMethod(Echo.class, "echoBack", new Class<?>[] {String.class});
        MethodSignature methodSignature = new MethodSignature(m);
        System.out.println(methodSignature.toString());
        assertThat(methodSignature.toString(), Matchers.is("echoBack(Ljava/lang/String;)"));
        System.out.println(methodSignature.toMethod(Echo.class));
        assertThat(methodSignature.toMethod(Echo.class), Matchers.is(m));
    }

}
