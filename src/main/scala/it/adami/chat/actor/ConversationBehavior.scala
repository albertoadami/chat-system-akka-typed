package it.adami.chat.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.adami.chat.actor.UserBehavior.UserRoot

import java.util.UUID

object ConversationBehavior {
  sealed trait ConversationCommand
  case class AddUser(name: String, replyTo: ActorRef[UserRoot]) extends ConversationCommand
  case class PostMessage(user: String, message: String, replyTo: ActorRef[UserRoot]) extends ConversationCommand

  def apply(name: String): Behavior[ConversationCommand] = conversation(name, Map.empty)

  private def conversation(name: String, users: Map[String, ActorRef[UserRoot]]): Behavior[ConversationCommand] = Behaviors.receive { (context, message) => message match {
    case AddUser(username, replyTo) =>
      context.log.info(s"Adding user with name $username to conversation $name")
      conversation(name, users + (name -> replyTo))
    case PostMessage(user, message, replyTo) =>
      context.log.info(s"Received message $message from user $user")
      val otherUsers = users.filter(_._1 != user)
      otherUsers.foreach(user => user._2 ! UserBehavior.MessagePosted(UUID.randomUUID().toString, user._1))
      replyTo ! UserBehavior.PostedConfirmation(message)
      Behaviors.same
  }

  }

}
