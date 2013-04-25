package scaladci
package examples.dijksta_self

import collection.mutable
import scaladci.DCI._

// DCI implementation of the Dijkstra algorithm
// Here using "self" as a reference to the RolePlayer.

object Dijkstra_self extends App {

  // DCI Context
  class Dijkstra(
    City: ManhattanGrid,
    CurrentIntersection: Intersection,
    Destination: Intersection,
    TentativeDistances: mutable.HashMap[Intersection, Int] = mutable.HashMap[Intersection, Int](),
    Detours: mutable.Set[Intersection] = mutable.Set[Intersection](),
    shortcuts: mutable.HashMap[Intersection, Intersection] = mutable.HashMap[Intersection, Intersection]()
    ) extends Context {

    // Algorithm
    if (TentativeDistances.isEmpty) {
      TentativeDistances.initialize
      Detours.initialize
    }
    CurrentIntersection.calculateTentativeDistanceOfNeighbors
    if (Detours.contains(Destination)) {
      val nextCurrent = Detours.withSmallestTentativeDistance
      new Dijkstra(City, nextCurrent, destination, TentativeDistances, Detours, shortcuts)
    }

    // Context helper methods
    def pathTo(x: Intersection): List[Intersection] = { if (!shortcuts.contains(x)) List(x) else x :: pathTo(shortcuts(x)) }
    def shortestPath = pathTo(destination).reverse

    // Roles
    role(TentativeDistances) {
      def initialize {
        self.put(CurrentIntersection, 0)
        City.intersections.filter(_ != CurrentIntersection).foreach(self.put(_, Int.MaxValue / 4))
      }
    }
    role(Detours) {
      def initialize { self ++= City.intersections }
      def withSmallestTentativeDistance = { self.reduce((x, y) => if (TentativeDistances(x) < TentativeDistances(y)) x else y) }
    }
    role(CurrentIntersection) {
      def calculateTentativeDistanceOfNeighbors {
        City.eastNeighbor.foreach(updateNeighborDistance(_))
        City.southNeighbor.foreach(updateNeighborDistance(_))
        Detours.remove(self)
      }
      def updateNeighborDistance(neighborIntersection: Intersection) {
        if (Detours.contains(neighborIntersection)) {
          val newTentDistanceToNeighbor = currentDistance + lengthOfBlockTo(neighborIntersection)
          val currentTentDistToNeighbor = TentativeDistances(neighborIntersection)
          if (newTentDistanceToNeighbor < currentTentDistToNeighbor) {
            TentativeDistances.update(neighborIntersection, newTentDistanceToNeighbor)
            shortcuts.put(neighborIntersection, self)
          }
        }
      }
      def currentDistance = TentativeDistances(CurrentIntersection)
      def lengthOfBlockTo(neighbor: Intersection) = City.distanceBetween(CurrentIntersection, neighbor)
    }
    role(City) {
      def distanceBetween(from: Intersection, to: Intersection) = self.blockLengths(Block(from, to))
      def eastNeighbor = self.nextDownTheStreet.get(CurrentIntersection)
      def southNeighbor = self.nextAlongTheAvenue.get(CurrentIntersection)
    }
  }

  // Data
  case class Intersection(name: Char)
  case class Block(x: Intersection, y: Intersection)

  // Environment
  case class ManhattanGrid() {
    val intersections               = ('a' to 'i').map(Intersection(_)).toList
    val (a, b, c, d, e, f, g, h, i) = (intersections(0), intersections(1), intersections(2), intersections(3), intersections(4), intersections(5), intersections(6), intersections(7), intersections(8))
    val nextDownTheStreet           = Map(a -> b, b -> c, d -> e, e -> f, g -> h, h -> i)
    val nextAlongTheAvenue          = Map(a -> d, b -> e, c -> f, d -> g, f -> i)
    val blockLengths                = Map(Block(a, b) -> 2, Block(b, c) -> 3, Block(c, f) -> 1, Block(f, i) -> 4, Block(b, e) -> 2, Block(e, f) -> 1, Block(a, d) -> 1, Block(d, g) -> 2, Block(g, h) -> 1, Block(h, i) -> 2, Block(d, e) -> 1)

    //    a - 2 - b - 3 - c
    //    |       |       |
    //    1       2       1
    //    |       |       |
    //    d - 1 - e - 1 - f
    //    |               |
    //    2               4
    //    |               |
    //    g - 1 - h - 2 - i
  }
  val startingPoint = ManhattanGrid().a
  val destination   = ManhattanGrid().i
  val shortestPath  = new Dijkstra(ManhattanGrid(), startingPoint, destination).shortestPath
  println("Dijkstra (using 'self' reference):\n" + shortestPath.map(_.name).mkString(" -> "))
  // a -> d -> g -> h -> i
}