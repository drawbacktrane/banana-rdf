package org.w3.linkeddata

import org.w3.banana._
import scalaz.Validation
import akka.util.Duration
import akka.util.duration._

object LinkedData {

  /**
   * Provides a default instance for LinkedData
   * <ul>
   * <li>it comes with an internal memory-based knowledge base to cache the graphs</li>
   * <li>LD is a Future-based implementation (totally asynchronous)</li>
   * <li>timbl() waits for the value to be there</li>
   * </ul>
   */
  def inMemoryImpl[Rdf <: RDF](
    ops: RDFOperations[Rdf],
    graphTraversal: RDFGraphTraversal[Rdf],
    utils: RDFUtils[Rdf],
    readerFactory: RDFReaderFactory[Rdf]): LinkedData[Rdf] =
    new LinkedDataMemoryKB(
      ops,
      graphTraversal,
      utils,
      readerFactory)

}

/**
 * The interface to the Web of Data
 */
trait LinkedData[Rdf <: RDF] {

  /**
   * The abstraction for a value coming from the interaction with the Web of Data
   */
  type LD[S] <: LDInterface[S]

  trait LDInterface[S] {

    /**
     * don't forget to invoke timbl in order to get the best out of your LD!
     */
    def timbl(atMost: Duration = 60.seconds): Validation[LDError, S]

    def map[A](f: S ⇒ A): LD[A]

    def flatMap[A](f: S ⇒ LD[A]): LD[A]

    def foreach(f: S => Unit): Unit

    def followIRI(predicate: Rdf#IRI)(implicit ev: S =:= Rdf#IRI): LD[Iterable[Rdf#Node]]

    def follow(predicate: Rdf#IRI, max: Int = 10, maxDownloads: Int = 10)(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[Rdf#Node]]

    def as[T](f: Rdf#Node => Option[T])(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[T]]

    def asURIs(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[Rdf#IRI]]

    def asStrings(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[String]]

    def asInts(implicit ev: S =:= Iterable[Rdf#Node]): LD[Iterable[Int]]

  }

  def shutdown(): Unit

  def point[S](s: S): LD[S]

  def goto(iri: Rdf#IRI): LD[Rdf#IRI]

  /* enhanced syntax */

  class IRIW(iri: Rdf#IRI) {
    def follow(predicate: Rdf#IRI): LD[Iterable[Rdf#Node]] =
      goto(iri).followIRI(predicate)
  }

  implicit def wrapIRI(iri: Rdf#IRI): IRIW = new IRIW(iri)

  class IRIsW(iris: Iterable[Rdf#Node]) {
    def follow(predicate: Rdf#IRI, max: Int = 10, maxDownloads: Int = 10): LD[Iterable[Rdf#Node]] = {
      val irisLD = point(iris)
      irisLD.follow(predicate, max, maxDownloads)
    }
  }

  implicit def wrapIRIs(iris: Iterable[Rdf#Node]): IRIsW = new IRIsW(iris)

}

