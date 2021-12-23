package it.adami.chat

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory
import it.adami.chat.actor.{ConversationBehavior, UserBehavior}

import java.util.UUID
import scala.util.Random

object ChatSystemMain {

  sealed trait ChatSystemEvent
  case class ConversationJob(name: String, numberOfUsers: Int)

  def apply: Behavior[ConversationJob] = Behaviors.setup { context =>

    val conversationName = s"conversation-${UUID.randomUUID().toString}"
    val conversation = context.spawn(ConversationBehavior.apply(conversationName), conversationName)

    Behaviors.receiveMessage {
      case ConversationJob(name, n) =>
        context.log.info(s"Received Command to create conversation with name $name and $n users")

        val userNames = (1 to n).map(i => s"user-$i")
        val userActors: Map[String, ActorRef[UserBehavior.UserRoot]] =
        userNames.map {username =>
          (username -> context.spawn(UserBehavior.apply(username), username))
        }.toMap

        userActors.foreach((item) => conversation ! ConversationBehavior.AddUser(item._1, item._2))

        (1 to 1000).foreach { i =>
          val user = Random.shuffle(userActors.toSeq).head
          user._2 ! UserBehavior.PostMessage(s"message-$i")
        }


        Behaviors.same
    }

  }

  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.load()

    val system = ActorSystem(ChatSystemMain.apply, "chat-actor-system", config)

    system ! ConversationJob(UUID.randomUUID().toString, 10)

  }

}
