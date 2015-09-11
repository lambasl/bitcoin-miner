package miner.common

/**
 * @author user
 */
object Messages {
  case object handshake
  case class work(dataGen: (String) => Array[String], numLeadingZeros : Int)
  case class foundCoin(id: String, hash: String)
  case class terminate(msg: String)
}