package freestyle

import akka.actor._
import akka.event.Logging
import java.security.MessageDigest
import scala.concurrent.duration.Duration
import java.net.InetSocketAddress
import akka.actor.IO.SocketHandle
import akka.util.ByteString
import java.util

/**
 * Created with IntelliJ IDEA.
 * User: wysa
 * Date: 13-4-9
 * Time: 上午10:44
 * To change this template use File | Settings | File Templates.
 */
case class Request(socket: SocketHandle)


class Worker(id: Int, uri: String, address: InetSocketAddress, c: Config) extends Actor {
    val log = Logging(context.system, getClass)
    val monitor = context.system.actorFor("/user/monitor")
    var content: ByteString = _
    var connectionActor: ActorRef = _
    var h: String = _
    var p: Int = _
    var identity: String = _
    var counter: Int = 0
    var socket: SocketHandle = _
    override def preStart = {
        import freestyle.Worker._
        socket = IOManager(context.system).connect(address)
        log.debug(s"[$id] connecting to ${address}")
        val headers = HTTPHeaders.clone
        headers("User-Agent") = userAgents(c.agent)
        val contentBuilder = new StringBuilder
        contentBuilder.append(s"${c.method} ${uri} HTTP/1.1\r\n")
        contentBuilder.append(headers.view map {
            case (key, value) => key + ": " + value
        } mkString ("\r\n"))
        contentBuilder.append("\r\n")
        val builder = ByteString.newBuilder
        builder.putBytes(contentBuilder.toString().getBytes())
        content = builder.result()
    }

    override def postStop = {
        try {
            log.debug(s"[$id] on stop")
            socket.close()
        } catch {
            case e: NullPointerException =>
                log.debug(s"{$id] on stop. connection not established.")
        }
    }

    def receive = {
        case Request(socket) =>
            log.debug("start Request")
            if (counter >= c.num) {
                log.debug(s"[$id]finished")

                context.stop(self)
                monitor ! Finished(id)
            } else {
                log.debug(s"[$id]request")
                val time = System.currentTimeMillis();
                identity = s"$id$time"
                counter += 1
                monitor ! Start(identity, time)
                socket.asWritable.write(content)
                log.debug("send write")
            }
        case ReceiveTimeout =>
            log.error("Time out")
            context.setReceiveTimeout(Duration.Undefined)
            monitor ! Timeout(identity)
        /*
            IOManager events
         */
        case IO.Error(cause:Throwable) =>
            log.error(cause.toString);
        case IO.Connected(socket, address) =>
            log.debug(s"[$id] connected to $address")
            self ! Request(socket)
        case IO.Read(socket, byteStr) =>
            log.debug(s"[$id] received incoming data")
            log.debug(s"length:${byteStr.length}")
            monitor ! Completed(identity,System.currentTimeMillis())
        case IO.Closed(socket: IO.SocketHandle, cause) =>
            log.debug(s"[$id] socket:$socket closed with $cause")
            if(counter >= c.num){
                log.debug(s"[$id]finished")

                context.stop(self)
                monitor ! Finished(id)
            }else{
                this.socket = IOManager(context.system).connect(address)
            }
    }
}

object Worker {
    val userAgents = Map(
        "IE9" -> "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)",
        "Chrome" -> "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.43 Safari/537.31",
        "Firefox" -> "Mozilla/5.0 (Windows NT 6.1; Intel Mac OS X 10.6; rv:7.0.1) Gecko/20100101 Firefox/7.0.1"
    )
    val HTTPHeaders = scala.collection.mutable.Map(
        "Cache-Control" -> "no-cache",
        "Accept" -> "*",
        "Connection" -> "keep-alive",
        "Content-Type" -> "text/html",
        "Content-Length" -> "0",
        "User-Agent" -> ""
    )
}