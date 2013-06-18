package freestyle

import java.nio.channels.AsynchronousSocketChannel
import java.net.InetSocketAddress
import java.nio.ByteBuffer

/**
 * Created with IntelliJ IDEA.
 * User: wysa
 * Date: 13-4-9
 * Time: 上午10:48
 * To change this template use File | Settings | File Templates.
 */
object HttpServer {
  /*def main(args:Array[String])={
    val socket = AsynchronousSocketChannel.open()
    socket.connect(new InetSocketAddress("119.75.218.70",80)).get()
    socket.write(ByteBuffer.wrap("GET / HTTP/1.1\r\nContent-Type: text/html\r\nContent-Length: 0\r\n\r\n".getBytes())).get()
    val buffer = ByteBuffer.allocate(1024);
    socket.read(buffer).get()
    socket.close()

  }  */


}
