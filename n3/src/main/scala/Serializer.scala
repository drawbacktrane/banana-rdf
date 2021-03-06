/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.banana.n3

import org.w3.banana._

/**
 * Async Parser for the simplest of all RDF encodings: NTriples
 * http://www.w3.org/TR/rdf-testcases/#ntriples
 *
 * This is using the nomo library that is being developed
 * here:  https://bitbucket.org/pchiusano/nomo
 *
 * @author bblfish
 * @since 02/02/2012
 */

class Serializer[Rdf <: RDF](ops: RDFOperations[Rdf]) {
  
  import ops._

  val xsd = XSDPrefix(ops)
  
//  val pimps = new Pimps(m)
//  import pimps._
  
  def asN3(graph: Rdf#Graph): String =
    graph map tripleAsN3 mkString "\n"

  def tripleAsN3(triple: Rdf#Triple): String = {
    val Triple(s, p, o) = triple
    "%s %s %s ." format (nodeAsN3(s), iriAsN3(p), nodeAsN3(o))
  }
  
  def nodeAsN3(node: Rdf#Node): String = Node.fold(node) (
    iriAsN3,
    { case BNode(bnode) => "_:" + bnode },
    { l: Rdf#Literal => literalAsN3(l) }
  )
  
  def iriAsN3(iri: Rdf#IRI): String = {
    val IRI(iriString) = iri
    "<" + NTriplesParser.toIRI(iriString) + ">"
  }
  
  def literalAsN3(literal: Rdf#Literal): String = Literal.fold(literal) (
    { typedLiteral: Rdf#TypedLiteral => typedLiteralAsN3(typedLiteral) },
    { case LangLiteral(lexicalForm, Lang(lang)) => "\"%s\"@%s" format (NTriplesParser.toAsciiLiteral(lexicalForm), lang) }
  )
  
  def typedLiteralAsN3(typedLiteral: Rdf#TypedLiteral): String = typedLiteral match {
    case TypedLiteral(lexicalForm, datatype) if datatype == xsd.string =>
      "\"%s\"" format NTriplesParser.toAsciiLiteral(lexicalForm)
    case TypedLiteral(lexicalForm, IRI(iri)) =>
      "\"%s\"^^<%s>" format (NTriplesParser.toAsciiLiteral(lexicalForm), NTriplesParser.toIRI(iri))
  }
  
}
