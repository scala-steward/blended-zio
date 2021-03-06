package blended.zio.streams.jms

import javax.jms._

import zio._
import zio.duration._

sealed trait JmsApiObject {
  def id: String
  override def toString: String = s"${getClass().getSimpleName()}($id)"
}

object JmsApiObject {

  // doctag<connection>
  final case class JmsConnectionFactory(
    override val id: String,
    factory: ConnectionFactory,
    reconnectInterval: Duration,
    keepAlive: Option[JmsKeepAliveMonitor] = None,
    credentials: Option[(String, String)] = None
  ) extends JmsApiObject
  // end:doctag<connection>

  final case class JmsConnection(
    factory: JmsConnectionFactory,
    conn: Connection,
    clientId: String
  ) extends JmsApiObject {
    override val id = s"${factory.id}-${clientId}"
  }

  final case class JmsSession(
    conn: JmsConnection,
    name: String,
    session: Session
  ) extends JmsApiObject {
    override val id: String = s"${conn.id}-$name"
  }

  final case class JmsConsumer(
    session: JmsSession,
    name: String,
    dest: JmsDestination,
    consumer: MessageConsumer
  ) extends JmsApiObject {
    override val id: String = s"${session.id}-$name-${dest.asString}"
  }

  final case class JmsProducer(
    session: JmsSession,
    name: String,
    producer: MessageProducer
  ) extends JmsApiObject {
    override val id: String = s"${session.id}-$name"
  }
}

sealed trait JmsMessageBody
object JmsMessageBody {
  case object Empty                             extends JmsMessageBody
  final case class Binary(content: Chunk[Byte]) extends JmsMessageBody
  final case class Text(content: String)        extends JmsMessageBody
}

// doctag<keepalive>
final case class JmsKeepAliveMonitor(
  dest: JmsDestination,
  interval: Duration,
  allowed: Int
)
// end:doctag<keepalive>
