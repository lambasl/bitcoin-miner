package miner.server

import akka.actor.ActorSystem
import akka.actor._
import akka.event.Logging
import akka.event.LoggingAdapter
import miner.common.Master
import miner.common.Messages.handshake


class Server {
  
  val leadingZeros = 3;
  val numberOfWorkers = 2;
  
  val serverSystem = ActorSystem("serverSystem")
  val master = serverSystem.actorOf(Props(new Master(numberOfWorkers)), "master")
  master ! handshake
  
}