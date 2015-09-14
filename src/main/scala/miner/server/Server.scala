/* 
 * Authors: Srinivas Narne, Satbeer Lamba
 * Bitcoin Miner Server Implementation
*/
package miner.server
import akka.actor._
import akka.event.Logging
import akka.event.LoggingAdapter
import java.security.MessageDigest
import akka.routing.RoundRobinRouter
import java.net._
import miner.common.Messages
import miner.common.Worker
import miner.common.Messages.Work
import miner.common.Messages.Work

case class Message(msg: String)

object Server {

  var numberOfZeroes = 0
  var numberOfCoinsFound = 0
  var bitcoinSet = 10000000 //total number of works
  var workUnit = 500000 // amount of work each user gets in one request
  var scalingFactor = 1
  var randomStringLength = 0

  def initialise(numZeros: Int, scale: Int, randomStrLen: Int) {
    numberOfZeroes = numZeros
    scalingFactor = scale
    randomStringLength = randomStrLen
    validateInput()
  }
  def validateInput() = {

    if (numberOfZeroes == Int.MinValue) {
      numberOfZeroes = 3
      println("Number of zeros not defined. Using default=" + numberOfZeroes)
    }
    if (scalingFactor == Int.MinValue) {
      scalingFactor = 1
      println("Scaling not defined. Using scaling=" + scalingFactor)
    }
    if (randomStringLength == Int.MinValue) {
      randomStringLength = 20
      println("Random String length not defined. Using default length=" + randomStringLength)
    }
    bitcoinSet = bitcoinSet * scalingFactor
  }

  def run() = {
    val serverSystem = ActorSystem("ServerSystem")
    val serverGuardian = serverSystem.actorOf(Props(new ServerGuardian()), name = "ServerGuardian")
  }
 
  class ServerGuardian  extends Actor {

    var numberOfClients = 0
    var bitcoinsFound = 0    
    var totalUnitWorks = bitcoinSet/workUnit
    var unitsOfWorkDone = 0
    var unitsofWorkScheduled = 0 
    val clientLoadFactor = 5

    val numServerWorkers = ((Runtime.getRuntime().availableProcessors()) * 2.5).toInt
    println("NumberofWorkers =" + numServerWorkers)
    val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinRouter(numServerWorkers)), name = "workerRouter")
    for(i <- 0 to numServerWorkers-1){
      workerRouter ! new Work(workUnit, numberOfZeroes, randomStringLength)
      unitsofWorkScheduled +=1
    }
    
    def receive = {

      case Messages.Mine =>
        if(unitsofWorkScheduled < totalUnitWorks){           
          workerRouter ! new Work(workUnit, numberOfZeroes, randomStringLength)
          unitsofWorkScheduled +=1
        }

      case "Ready" =>      
        sender ! "NumberofWorkers"
        
      case Messages.NumWorkers(numWorkers) =>
         if(unitsofWorkScheduled < totalUnitWorks){
           unitsofWorkScheduled += clientLoadFactor*numWorkers
          sender ! Messages.WorkLoad(clientLoadFactor*numWorkers, workUnit, numberOfZeroes, randomStringLength)
        }
        
      case Messages.Done(numberOfCoinsFound) =>
        unitsOfWorkDone += 1
        bitcoinsFound += numberOfCoinsFound
        if (unitsofWorkScheduled < totalUnitWorks) {
           unitsofWorkScheduled +=1
            self ! Messages.Mine         
        }else if(totalUnitWorks <= unitsOfWorkDone){
          self ! Messages.Finished
        }

      case Messages.AssignMoreWorkToClient(numWorkers, bitCoinsMined) =>
        //Work from prev slot is done. Keeping a count of it
        unitsOfWorkDone += clientLoadFactor*numWorkers
        bitcoinsFound += bitCoinsMined
        if(unitsOfWorkDone < totalUnitWorks){
          sender ! Messages.WorkLoad(clientLoadFactor*numWorkers, workUnit, numberOfZeroes, randomStringLength)          
        }else if(totalUnitWorks <=unitsofWorkScheduled){
          sender ! Messages.Finished          
        }
        
      case Messages.Finished =>
          println("Total Number of Bitcoins Mined = " + bitcoinsFound)
          context.system.shutdown()
        

    }
  }
}
