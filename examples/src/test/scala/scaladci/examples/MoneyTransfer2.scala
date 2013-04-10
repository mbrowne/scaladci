package scaladci
package examples.MoneyTransfer2
import scala.language.reflectiveCalls
import DCI._

case class LedgerEntry(message: String, amount: Int)

case class Account(account: String, initialLedgers: List[LedgerEntry]) {
  private val ledgers = new {
    var ledgerList = initialLedgers
    def addEntry(message: String, amount: Int) { ledgerList = ledgerList :+ new LedgerEntry(message, amount) }
    def getBalance = ledgerList.foldLeft(0)(_ + _.amount)
  }

  def balance = ledgers.getBalance
  def increaseBalance(amount: Int) { ledgers.addEntry("depositing", amount) }
  def decreaseBalance(amount: Int) { ledgers.addEntry("withdrawing", -amount) }
}

class MoneyTransfer(Source: Account, Destination: Account, amount: Int) extends Context {

  def transfer() {
    Source.transfer
  }

  role(Source) {
    def withdraw() {
      Source.decreaseBalance(amount)
    }
    def transfer() {
      println("Source balance is: " + Source.balance)
      println("Destination balance is: " + Destination.balance)
      Destination.deposit()
      withdraw()
      println("Source balance is now: " + Source.balance)
      println("Destination balance is now: " + Destination.balance)
    }
  }

  role(Destination) {
    def deposit() {
      Destination.increaseBalance(amount)
    }
  }
}

object MoneyTransferMarvinTest extends App {
  val source      = Account("salary", List(LedgerEntry("start", 0), LedgerEntry("first deposit", 1000)))
  val destination = Account("budget", List())
  val context     = new MoneyTransfer(source, destination, 245)
  context.transfer()
}

/* prints:

Source balance is: 1000
Destination balance is: 0
Source balance is now: 755
Destination balance is now: 245

*/