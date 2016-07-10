package net.neoremind.dynamicproxy.demo;

/**
 * @author zhangxu
 */
public class EchoServiceImpl implements EchoService {

    public String echo(String message) {
        return message;
    }

}
