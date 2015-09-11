/* 
 * Authors: Srinivas Narne, Satbir Lamba
 * Bitcoin Miner Server Implementation
*/

package server
package miner
import akka.actor._
import akka.event.Logging
import akka.event.LoggingAdapter
import java.security.MessageDigest
import akka.routing.RoundRobinRouter


sealed trait BitcoinMinerMessages
case object Start extends BitcoinMinerMessages
case object FindBitcoins extends BitcoinMinerMessages 
case class Message(msg: String) extends BitcoinMinerMessages
case class Done (numberOfCoinsFound : Int) extends BitcoinMinerMessages
case class Work (initValue : Int , endValue : Int , numberOfZeroes : Int) extends BitcoinMinerMessages


object Server extends App {

	var numberOfZeroes = 0
	var numberOfCoinsFound = 0
	var bitcoinSet = 10000000

	val valueEntered = if (args.length == 3) args(0) else "INVALID"
	val scalingFactor = args(1)
	val randomStringLength = args(2)

	def userInput(valueEntered: String) = {

		if (valueEntered == "INVALID"){
			println ("Invalid Input. Please Try Again.")
			System.exit(-1)
		}
		else
			numberOfZeroes = valueEntered.toInt
			println("The Number of Zeroes Required: "+ valueEntered)
			println("The Scaling Factor Entered: "+ scalingFactor)
			println("The String Length Entered:"+ randomStringLength)
	}
	userInput (valueEntered)

	val serverSystem = ActorSystem("ServerSystem")
	val numberOfServerWorkers = ((Runtime.getRuntime().availableProcessors()) * 1.5).toInt
	val serverGuardian = serverSystem.actorOf(Props(new ServerGuardian(numberOfServerWorkers, bitcoinSet * scalingFactor.toInt, numberOfZeroes)), name = "ServerGuardian")
	serverGuardian ! FindBitcoins

	class ServerWorker extends Actor {

		def generateZeroString (numberOfZeroes: Int) = {
			var zeroString = ""
			for (i <- 1 to numberOfZeroes)
			{
				zeroString = zeroString + "0"
			}
			zeroString
		}

		def hash256(in : String): String = {

			val sha = MessageDigest.getInstance("SHA-256")
        	var hashedString:String=sha.digest(in.getBytes).foldLeft("")((in:String, b: Byte) => in + Character.forDigit((b & 0xf0) >> 4, 16) +Character.forDigit(b & 0x0f, 16))
        	hashedString
		}

		def sha256Hash (initValue: Int, endValue: Int, numberOfZeroes: Int) = {

			for (j <- initValue to endValue) {

				def randomStringArray(length: Int) = {

					val r = new scala.util.Random
                	val a = new Array[Char](length)
                	val sb = new StringBuilder
                	for (i <- 0 to length-1) {
                		a(i) = r.nextPrintableChar
                	}
                	a.mkString
				}
				var preHashString = "satbir" + randomStringArray(randomStringLength.toInt)
            	var hashedString = hash256(preHashString)
            	var zeroString: String = generateZeroString(numberOfZeroes)
            	if (hashedString.startsWith(zeroString)) {
            		println(preHashString + "\t" + hashedString)
            		numberOfCoinsFound = numberOfCoinsFound + 1
            	}
			}
			Done (numberOfCoinsFound)
		}

		def receive = {
			case Work(initValue, endValue, numberOfZeroes) => 
				sender ! sha256Hash(initValue, endValue, numberOfZeroes)
		}

	}

	class ServerGuardian (numberOfServerWorkers: Int, bitcoinSet: Int, numberOfZeroes: Int) extends Actor {

		var numberOfClients = 1
		var clientBlockInitValue = 10000000
		var clientBlockEndValue = 11000000
		var clientBlockSize = 1000000
		var totalChunks = (bitcoinSet/10000)
		var numberOfChunks = 1
		var bitcoinsFoundByClient = 0

		val numberOfClientWorkers = ((Runtime.getRuntime().availableProcessors()) * 1.2).toInt
		val workerRouter = context.actorOf(Props[ServerWorker].withRouter(RoundRobinRouter(numberOfServerWorkers)), name = "workerRouter")

		def receive = {

			case FindBitcoins =>

				for (i <-1 until totalChunks) {
					workerRouter ! Work((i-1) * 10000, i * 10000, numberOfZeroes)
				}

			case "Ready" =>

				sender ! clientBlockInitValue
				sender ! clientBlockEndValue
				sender ! numberOfZeroes
				sender ! numberOfClientWorkers

				clientBlockInitValue = clientBlockInitValue + clientBlockSize
				clientBlockEndValue = clientBlockEndValue +clientBlockSize


			case Done(numberOfCoinsFound) =>
				numberOfChunks = numberOfChunks + 1
				if (numberOfChunks == totalChunks) {
					println ("Total Number of Bitcoins Mined = " + (numberOfCoinsFound+bitcoinsFoundByClient))
					context.system.shutdown()
				}

			case msg: String => 
				numberOfClients = numberOfClients + 1
				println(msg.toString)

			case noofbitcoins: Int =>
				bitcoinsFoundByClient+=noofbitcoins
				println("Number of Bitcoins mined by client is: " + noofbitcoins)

		}
	}
}
