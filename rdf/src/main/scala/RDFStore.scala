package org.w3.banana

/**
 * to manipulate named graph within a Store
 */
trait RDFStore[Rdf <: RDF] {

  def store: Rdf#Store

  def addNamedGraph(uri: Rdf#IRI, graph: Rdf#Graph): Rdf#Store

  def appendToNamedGraph(uri: Rdf#IRI, graph: Rdf#Graph): Rdf#Store

  def getNamedGraph(uri: Rdf#IRI): Rdf#Graph

  def removeGraph(uri: Rdf#IRI): Rdf#Store

}
