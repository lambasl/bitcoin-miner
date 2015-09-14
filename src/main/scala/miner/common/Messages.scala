package miner.common

import akka.actor.Actor

/**
 * @author user
 */
object Messages {
  
  case class Done(v:Int)
  case class Work(workSize: Int, numZeros: Int, randomStrLen: Int)
  case object Mine 
  case class NumWorkers(numWorkers: Int)
  case object GetMoreWork
  case class AssignMoreWorkToClient(numOfWorkers: Int, bitcoinsMined: Int)
  case class WorkLoad(numUnitWork: Int, workUnitSize: Int, numberOfZeroes: Int, randomStringLength: Int)
  case object Finished
}