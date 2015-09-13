/**
 * Authors: Srinivas Narne, Satbeer Lamba
 * Bitcoin Miner Entry Implementation
 */

package miner.main

import miner.server.Server
import miner.client.Client
object BitcoinMiner extends App {

  println("Running app..")

  if (args(0).contains(".")) {
    //contains ip address , must be client
    //start client
    Client.assign(args(0))
    Client.run()
  } else {
    if (args.length == 3) {
      //params specified : numberofZeros, scalingFactor, Length of random String
      Server.initialise(args(0).toInt, args(1).toInt, args(2).toInt)
    } else if (args.length == 2) {
      //params specified : numberofZeros, scalingFactor
      Server.initialise(args(0).toInt, args(1).toInt, Int.MinValue)
    } else if (args.length == 1) {
      //params specified : numberofZeros
      Server.initialise(args(0).toInt, Int.MinValue, Int.MinValue)
    } else if (args.length == 0) {
      //params specified : numberofZeros, scalingFactor
      Server.initialise(Int.MinValue, Int.MinValue, Int.MinValue)
    }
    Server.run()
  }

}