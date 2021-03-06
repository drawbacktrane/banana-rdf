package org.w3.banana

import java.io._

import scalaz.Validation
import scalaz.Validation._

trait RDFReaderFactory[Rdf <: RDF] {
  def RDFXMLReader: RDFReader[Rdf, RDFXML]
  def TurtleReader: RDFReader[Rdf, Turtle]
}