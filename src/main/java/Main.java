import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Created by admin on 07.04.2017.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(10));
        AsynchronousServerSocketChannel ass = AsynchronousServerSocketChannel.open(group);
        ass.bind(new InetSocketAddress(80));
        ass.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel s, Void param) {
                ass.accept(null, this);
                process(s);
            }

            @Override
            public void failed(Throwable error, Void param) {
                error.printStackTrace();
            }
        });
//        while (true) {
//            Thread.sleep(1000);
//        }
        Object x = new Object();
        synchronized (x){
            x.wait();
        }
    }

    static void process(AsynchronousSocketChannel s) {
        ByteBuffer buf = ByteBuffer.allocate(10240);
        StringBuilder request = new StringBuilder();
        s.read(buf, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void param) {
                buf.flip();
                byte[] data = new byte[buf.remaining()];
                buf.get(data);
                request.append(new String(data, StandardCharsets.US_ASCII));
                int len = request.length();
                if (len >= 4 && request.substring(len - 4).equals("\r\n\r\n")) {
                    System.out.println(request);
                    sendResponse(s);
                } else {
                    buf.clear();
                    s.read(buf, null, this);
                }

            }

            @Override
            public void failed(Throwable error, Void param) {
                error.printStackTrace();
            }
        });
    }

    static void sendResponse(AsynchronousSocketChannel s) {
        ByteBuffer buf = ByteBuffer.wrap("Dimini Inc.\nhttp://dimini.tk".getBytes());
        s.write(buf, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void param) {
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable error, Void param) {
                error.printStackTrace();
            }
        });
    }

}
