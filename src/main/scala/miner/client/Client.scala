/* 
 * Authors: Srinivas Narne, Satbeer Lamba
 * Bitcoin Miner Server Implementation
*/ 
package miner.client
import akka.actor._
import akka.event.Logging
import akka.event.LoggingAdapter
import java.security.MessageDigest
import akka.routing.RoundRobinRouter
import miner.common.Worker
import miner.common.Messages

object Client  {

  val system = ActorSystem("Client")
  System.setProperty("java.net.preferIPv4Stack", "true")
  var serverIP = ""

  def assign(argument:String)={
    if(argument.containsSlice(".")){
      serverIP = argument
      println("IP ADDRESS OF SERVER : "+argument)
    }
    else 
     {
      println("*********Invalid Argument**********" + "\n Input should be of type : \nscala Project1 IPAddress" +"\n ***********************************")
      System.exit(1)
    }

  }
  def run(){
  
  val clientMaster = system.actorOf(Props(new Master()),name = "clientMaster")

  }
	class Master extends Actor {
    
    var noofbitcoins=0
    var workUnitsDone = 0
    var totalWorkUnits = 0
    val numWorkers = ((Runtime.getRuntime().availableProcessors()) * 1).toInt
    val serverGuardian = context.actorFor("akka://ServerSystem@"+ serverIP +":6565/user/ServerGuardian")
    val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(numWorkers)), name = "workerRouterClient")
    serverGuardian ! "Ready"

    def receive = {
      
      
      case "NumberofWorkers" =>
            sender ! Messages.NumWorkers(numWorkers)
      
      case Messages.WorkLoad(numUnitWork, workUnitSize, numberOfZeroes, randomStringLength) => 
        println("Got work from server..............")
        totalWorkUnits = numUnitWork
        var i = 0
        while(i < numUnitWork) {
              workerRouter ! new Messages.Work(workUnitSize, numberOfZeroes, randomStringLength)
            }
      
      case Messages.Done(numberOfCoinsFound) =>
        noofbitcoins += numberOfCoinsFound
        workUnitsDone += 1
        if(workUnitsDone == totalWorkUnits){
          println("One Batch of work done. Getting more work from Server")
          self ! Messages.GetMoreWork
        }
        
      case Messages.GetMoreWork =>
        workUnitsDone = 0
        totalWorkUnits = 0
        serverGuardian ! Messages.AssignMoreWorkToClient(numWorkers, noofbitcoins)
        noofbitcoins=0
        
      case Messages.Finished =>
        context.system.shutdown()
            
     }
  }
 

}
