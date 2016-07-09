package net.neoremind.dynamicproxy.util;

import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.Type;

/**
 * ASM的工具类
 *
 * @author <a href="mailto:xuchen06@baidu.com">xuc</a>
 * @version create on 2015-12-9 上午1:36:33
 */
public abstract class AsmUtil {

    public static String[] getInternalNames(Class<?>... classes) {
        if (ArrayUtils.isEmpty(classes)) {
            return null;
        }

        int length = classes.length;
        String[] ret = new String[length];
        for (int i = 0; i < length; i++) {
            Type type = Type.getType(classes[i]);
            ret[i] = type.getInternalName();
        }

        return ret;
    }

}
