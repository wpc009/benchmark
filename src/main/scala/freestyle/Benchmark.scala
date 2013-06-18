package freestyle

import akka.actor._
import akka.routing.BroadcastRouter

case class Config(concurrent: Int = 1, num: Int = 1, timeLimit: Int = 5, method: String = "GET",
                  agent: String = "Chrome", url: String = "",debug:Boolean =false)

object Benchmark {
    val parser = new scopt.immutable.OptionParser[Config]("benchmark", "0.1.0") {
        def options = Seq(
            intOpt("c", "concurrency", "Number of concurrent thread. defualt is 1") {
                (co: Int, c: Config) => c.copy(concurrent = co)
            },
            intOpt("n", "number", "Number of requests each thread sends. default is 1") {
                (n: Int, c: Config) => c.copy(num = n)
            },
            intOpt("t", "timelimit", "Seconds to max. wait for responses. default is 1 sec.") {
                (t: Int, c: Config) => c.copy(timeLimit = t)
            },
            opt("m", "method", "HTTP method to use. Default is GET") {
                (m: String, c: Config) => c.copy(method = m)
            },
            opt("a", "user-agent", "Custom the User-Agent header, default is Chrome browser's.") {
                (agent: String, c: Config) => c.copy(agent = agent)
            },
            booleanOpt("d","debug","output debug info"){
                (d:Boolean,c:Config) => c.copy(debug = d)
            },
            arg("<url>", "url to request") {
                (url: String, c: Config) => c.copy(url = url)
            }
        )
    }

    def main(args: Array[String]) = {
        parser.parse(args, Config()) map {
            config =>
                println(s"concurrent:${config.concurrent} num:${config.num} timeout:${config.timeLimit}")
                start(config)
        } getOrElse {
        }
        /*
         */
    }

    case class Msg()

    class TestActor extends Actor {
        def receive = {
            case Msg => println("test")
        }
    }

    def start(config: Config) {
        val system = ActorSystem("benchmark")

        val monitor = system.actorOf(Props(new Monitor(config)), "monitor")

        //    for(i<- 1 to config.num){
        //      router ! Request
        //    }
    }
}
