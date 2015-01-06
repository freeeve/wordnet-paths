package com.infoa;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.nio.charset.Charset;
import java.util.ArrayList;

@Path( "/" )
public class WordnetPaths
{
	private static final DynamicRelationshipType COMMENTS = DynamicRelationshipType.withName( "COMMENTS" );

	@GET
	@Produces( MediaType.TEXT_PLAIN )
	@Path( "/drinksAndMilkPaths" )
	public Response paths(@Context GraphDatabaseService db, @PathParam( "nodeId" ) long nodeId )
	{
		int length = 0;
		Transaction tx = db.beginTx();
		try {
			String[] startStrings = new String[]{"drink.v.01","drink.v.02","toast.v.02","drink_in.v.01","drink.v.05"};
			String[] endStrings = new String[]{"milk.n.01", "milk.n.02", "milk.n.03", "milk.n.04"};
			final ArrayList<Node> startNodes = new ArrayList<Node>();
			final ArrayList<Node> endNodes = new ArrayList<Node>();

			for(String name : startStrings) {
				for(Node n : db.findNodesByLabelAndProperty(DynamicLabel.label("synset"), "name", name)) {
					startNodes.add(n);
				}
			}

			for(String name : endStrings) {
				for(Node n : db.findNodesByLabelAndProperty(DynamicLabel.label("synset"), "name", name)) {
					endNodes.add(n);
				}
			}

			TraversalDescription td = db.traversalDescription()
				.depthFirst()
				.evaluator(new Evaluator() {
					@Override
					public Evaluation evaluate(org.neo4j.graphdb.Path path) {
						Boolean endNodeIsTarget = endNodes.contains(path.endNode());
						return Evaluation.of(endNodeIsTarget, path.length() < 4);
					}
				}).uniqueness(Uniqueness.NODE_PATH);


			ResourceIterable<org.neo4j.graphdb.Path> paths = td.traverse(startNodes);

			// serialize paths here...

			tx.failure();
		} catch(Exception ex) {
			tx.failure();
		}
		return Response.status( Status.OK ).entity(
			("" + length).getBytes(Charset.forName("UTF-8"))).build();
	}
}