package it.adami.chat.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object UserBehavior {

 sealed trait UserRoot

  case class PostMessage(message: String) extends UserRoot
  case class MessagePosted(message: String, from: String) extends UserRoot
  case class PostedConfirmation(message: String) extends UserRoot

  def apply(username: String): Behavior[UserRoot] = Behaviors.receive { (context, message) => message match {
    case PostMessage(message) =>
      context.log.info(s"User $username is posting message $message to conversation")
      Behaviors.same
    case MessagePosted(message, from) =>
      context.log.info(s"Received message $message from the user $from in the conversation")
      Behaviors.same
    case PostedConfirmation(message) =>
      context.log.info(s"Received confirmation for message $message")
      Behaviors.same

  }

  }
}