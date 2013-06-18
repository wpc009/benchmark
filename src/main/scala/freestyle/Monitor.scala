package freestyle

import akka.actor.{Status, ActorRef, Props, Actor}
import akka.event.Logging
import spray.can.client.HttpClient
import akka.routing.BroadcastRouter
import java.net.InetSocketAddress
import java.util
import scala.collection.JavaConversions._

/**
 * Created with IntelliJ IDEA.
 * User: wysa
 * Date: 13-4-9
 * Time: 下午5:42
 * To change this template use File | Settings | File Templates.
 */

case class Completed(id: String, time: Long)

case class Start(id: String, time: Long)

case class Timeout(id: String)

case class Finished(id:Int)

class Monitor(c: Config) extends Actor {
    val log = Logging(context.system, this)
    val profile = new java.util.HashMap[String, Long]
    val results = new util.ArrayList[Long]
    var router: ActorRef = _
    var h: String = _
    var p: Int = _
    var content: String = _
    var finishedCt = 0;
    override def preStart = {
        log.info("monitor started")
        c.url match {
            case Monitor.urlPattern(host, port, uri) =>
                h = host
                val u = Option(uri).getOrElse("/")
                p = Option(port).getOrElse("80").toInt
                log.debug(s"initial [$u]@$host:$port")
                val routees = for (i <- 1 to c.concurrent) yield context.actorOf(Props(new Worker(i, uri, new InetSocketAddress(h, p), c)))
                println(s"${routees.length} workers")
                router = context.actorOf(Props().withRouter(BroadcastRouter(routees = routees)), "broadcastRouter")
            case _ =>
                throw new Exception("wrong url format!")
        }

    }

    def receive = {
        case Completed(id, time) =>
            log.debug(f"[$id] completed")
            val startTime = profile.get(id);
            results.add(time-startTime);
            if(results.length % 100==0 && !results.isEmpty){
                println(s"${results.length} finished")
            }
            sender ! Request
        case Start(id, time) =>
            log.debug(s"[$id] started")
            profile.put(id, time)
        case Timeout(id) =>
            log.debug(s"[$id] timeout")
            results.add(-1)
            sender ! Request
        case Finished(id) =>
            log.debug(s"[$id] finished")
            finishedCt+=1
            log.debug(s"[${finishedCt}/${c.concurrent}]")
            if(finishedCt == c.concurrent){
                println("Done..")
                var failed = 0;
                results.foldLeft(0L){(v,e) => if(e == -1){ failed+=1;v}else{v+e;}};
                val succeed = results.filter(p => p != -1);
                var max=0L;
                val sum = succeed.foldLeft(0L){(v,e) => max=if(max>e){max}else{e};v+e;}
                val mean = sum / succeed.length
                val varience = Math.sqrt(succeed.foldLeft(0.0){(v,e) => v+Math.pow(e-mean,2.0);}) / succeed.length
                println(s"response time : mean:$mean  max:$max variance: ${varience}");
                println(s"succeed: ${succeed.length}/${profile.size} ${succeed.length.toDouble/ profile.size
                    .toDouble}");
                context.system.shutdown();
            }
        case HttpClient.Connected =>
            log.info("httpclient connected. Start requesting....")
            router ! Request
        case Status.Failure(cause) =>
            log.error(s"httpClient connect failed: $cause")
    }
}


object Monitor {
    val urlPattern = """http://([\w\.]+)(:[0-9]+)?(.*)?""".r
}