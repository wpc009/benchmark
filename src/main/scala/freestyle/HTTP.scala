package freestyle

import akka.util.ByteString

/**
 * Created with IntelliJ IDEA.
 * User: wysa
 * Date: 13-4-10
 * Time: 下午9:38
 * To change this template use File | Settings | File Templates.
 */
object HTTP {

    def decode(byteString: ByteString) = {
        val str = byteString.decodeString("utf-8")

    }

}

class HTTPHead(method: String, uri: String, protocol: String, version: String) {
    val headers = new java.util.HashMap[String, String]()
}
