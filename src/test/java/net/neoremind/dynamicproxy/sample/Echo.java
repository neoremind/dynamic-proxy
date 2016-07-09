package net.neoremind.dynamicproxy.sample;

import java.io.IOException;

public interface Echo {

    void echo();

    Sound echoSound(int volume);

    String echoBack(String message);

    String echoBack(String[] messages);

    String echoBack(String[][] messages);

    String echoBack(String[][][] messages);

    int echoBack(int i);

    boolean echoBack(boolean b);

    String echoBack(String message1, String message2);

    void illegalArgument();

    void ioException() throws IOException;

}
