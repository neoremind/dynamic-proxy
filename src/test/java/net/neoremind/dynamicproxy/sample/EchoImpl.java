package net.neoremind.dynamicproxy.sample;

import java.io.IOException;
import java.io.Serializable;

public class EchoImpl extends AbstractEcho implements DuplicateEcho, Serializable {

    private static final long serialVersionUID = -4844873352607521103L;

    @Override
    public void echo() {
    }

    @Override
    public Sound echoSound(int volume) {
        return new Sound(volume);
    }

    @Override
    public boolean echoBack(boolean b) {
        return b;
    }

    @Override
    public String echoBack(String[] messages) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.length; i++) {
            String message = messages[i];
            sb.append(message);
        }
        return sb.toString();
    }

    @Override
    public String echoBack(String[][] messages) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.length; i++) {
            sb.append(echoBack(messages[i]));
        }
        return sb.toString();
    }

    @Override
    public String echoBack(String[][][] messages) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < messages.length; i++) {
            sb.append(echoBack(messages[i]));
        }
        return sb.toString();
    }

    @Override
    public int echoBack(int i) {
        return i;
    }

    @Override
    public String echoBack(String message1, String message2) {
        return message1 + message2;
    }

    @Override
    public void illegalArgument() {
        throw new IllegalArgumentException("dummy message");
    }

    @Override
    public void ioException() throws IOException {
        throw new IOException("dummy message");
    }
}
