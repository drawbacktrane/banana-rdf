package org.w3.banana

import org.w3.banana.diesel._
import org.scalatest._
import org.scalatest.matchers._

abstract class SparqlQueryOnStoreTest[Store, Rdf <: RDF, Sparql <: SPARQL](
  ops: RDFOperations[Rdf],
  dsl: Diesel[Rdf],
  iso: GraphIsomorphism[Rdf],
  queryBuilder: SPARQLQueryBuilder[Rdf, Sparql],
  underlyingStore: Store,
  storeFunc: Store => RDFStore[Rdf],
  queryEngineFunc: Store => RDFQuery[Rdf, Sparql]
) extends WordSpec with MustMatchers with BeforeAndAfterAll {

  import iso._
  import ops._
  import dsl._
  import queryBuilder._

  val store = storeFunc(underlyingStore)
  val queryEngine = queryEngineFunc(underlyingStore)

  val foaf = FOAFPrefix(ops)

  val graph: Rdf#Graph = (
    bnode("betehess")
      -- foaf.name ->- "Alexandre".lang("fr")
      -- foaf.title ->- "Mr"
  ).graph

  val graph2: Rdf#Graph = (
    bnode("betehess")
      -- foaf.name ->- "Alexandre".lang("fr")
      -- foaf.knows ->- (
        uri("http://bblfish.net/#hjs")
          -- foaf.name ->- "Henry Story"
          -- foaf.currentProject ->- uri("http://webid.info/")
      )
  ).graph

  override def beforeAll(): Unit = {
    store.addNamedGraph(IRI("http://example.com/graph"), graph)
    store.addNamedGraph(IRI("http://example.com/graph2"), graph2)
  }

  "betehess must know henry" in {

    val query = AskQuery("""
prefix foaf: <http://xmlns.com/foaf/0.1/>

ASK {
  GRAPH <http://example.com/graph2> {
    [] foaf:knows <http://bblfish.net/#hjs>
  }
}
""")

    val alexKnowsHenry = queryEngine.executeAskQuery(query)

    alexKnowsHenry must be (true)

  }

}
