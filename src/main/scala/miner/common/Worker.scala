package miner.common

import akka.actor.Actor
import java.security.MessageDigest
import scala.util.control.TailCalls.Done
import scala.util.control.TailCalls.Done

/**
 * @author user
 */
 class Worker() extends Actor {
  
    var matchString = "" 

    def generateZeroString(numberOfZeroes: Int) = {
      if(matchString != ""){
        matchString
      }
      var zeroString = ""
      for (i <- 1 to numberOfZeroes) {
        zeroString = zeroString + "0"
      }
      matchString = zeroString
      zeroString
    }

    def hash256(in: String): String = {

      val sha = MessageDigest.getInstance("SHA-256")
      var hashedString: String = sha.digest(in.getBytes).foldLeft("")((in: String, b: Byte) => in + Character.forDigit((b & 0xf0) >> 4, 16) + Character.forDigit(b & 0x0f, 16))
      hashedString
    }

    def sha256Hash(workSize: Int, numberOfZeroes: Int, randomStrLen: Int) = {
      var numberOfCoinsFound = 0
      for (j <- 0 to workSize) {

        def randomStringArray(length: Int) = {

          val r = new scala.util.Random
          val a = new Array[Char](length)
          val sb = new StringBuilder
          for (i <- 0 to length - 1) {
            a(i) = r.nextPrintableChar
          }
          a.mkString
        }
        var preHashString = new StringBuilder("Satbeersl")
        preHashString.append(randomStringArray(randomStrLen))
        var hashedString = hash256(preHashString.toString())
        var zeroString: String = generateZeroString(numberOfZeroes)
        if (hashedString.startsWith(zeroString)) {
          println(preHashString.toString() + "\t" + hashedString)
          numberOfCoinsFound = numberOfCoinsFound + 1
        }
      }
      Messages.Done(numberOfCoinsFound)
    }

    def receive = {
      case Messages.Work(workSize, numZeros, randomStrLen) =>
        sender ! sha256Hash(workSize, numZeros, randomStrLen)
    }

  }
