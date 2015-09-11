package miner.common

import akka.actor.Actor
import miner.common.Messages.handshake

/**
 * @author user
 */
class Master(numLeadingZeros: Int) extends Actor{
  
  def receive = {
    case "handshake" => 
      println("first message")
    case _ => println("Dont know why it's not printing")
        
  }
  
}