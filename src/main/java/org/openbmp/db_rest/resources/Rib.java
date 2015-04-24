/*
 * Copyright (c) 2014-2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.openbmp.db_rest.resources;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.openbmp.db_rest.DbColumnDef;
import org.openbmp.db_rest.RestResponse;
import org.openbmp.db_rest.DbUtils;

@Path("/rib")
public class Rib {
	@Context ServletContext ctx;
	@Context UriInfo uri;

	private DataSource mysql_ds;

	/**
	 * Initialize the class Sets the data source
	 * 
	 * @throws
	 */
	@PostConstruct
	public void init() {
		InitialContext initctx = null;
		try {

			initctx = new InitialContext();
			mysql_ds = (DataSource) initctx
					.lookup("java:/comp/env/jdbc/MySQLDB");

		} catch (NamingException e) {
			System.err
					.println("ERROR: Cannot find resource configuration, check context.xml config");
			e.printStackTrace();
		}
	}

	@GET
	@Produces("application/json")
	public Response getRib(@QueryParam("limit") Integer limit,
						   @QueryParam("where") String where,
						   @QueryParam("orderby") String orderby) {
		
		return RestResponse.okWithBody(
				DbUtils.selectStar_DbToJson(mysql_ds, "v_routes", limit, where, orderby));
	}
	
	@GET
	@Path("/peer/{peerHashId}")
	@Produces("application/json")
	public Response getRibByPeer( @PathParam("peerHashId") String peerHashId,
							@QueryParam("limit") Integer limit,
						   @QueryParam("where") String where,
						   @QueryParam("orderby") String orderby) {

		String where_str = "peer_hash_id = '" + peerHashId + "'";

		if (where != null)
			where_str += " and " + where;

		return RestResponse.okWithBody(
				DbUtils.selectStar_DbToJson(mysql_ds, "v_routes", limit, where_str, orderby));
	}

	@GET
	@Path("/asn/{ASN}")
	@Produces("application/json")
	public Response getRibAsn(@PathParam("ASN") Integer asn,
						   @QueryParam("limit") Integer limit,
						   @QueryParam("where") String where,
						   @QueryParam("orderby") String orderby) {
		
		String where_str = "Origin_AS = " + asn;
		
		if (where != null)
			where_str += " and " + where;
		
		return RestResponse.okWithBody(
				DbUtils.selectStar_DbToJson(mysql_ds, "v_routes", limit, where_str, orderby));
	}
	
	@GET
	@Path("/peer/{peerHashId}/asn/{ASN}")
	@Produces("application/json")
	public Response getRibAsnByPeer(@PathParam("ASN") Integer asn,
						   @PathParam("peerHashId") String peerHashId,
						   @QueryParam("limit") Integer limit,
						   @QueryParam("where") String where,
						   @QueryParam("orderby") String orderby) {
		
		String where_str = "Origin_AS = " + asn + " and peer_hash_id = '" + peerHashId + "'";
		
		if (where != null)
			where_str += " and " + where;
		
		return RestResponse.okWithBody(
				DbUtils.selectStar_DbToJson(mysql_ds, "v_routes", limit, where_str, orderby));
	}

	@GET
	@Path("/asn/{ASN}/count")
	@Produces("application/json")
	public Response getRibOriginASCount(@PathParam("ASN") Integer asn) {
		if (asn == null)
			return Response.status(400).entity(
					"{ \"error\": \"Missing required path parameter\" }").build();
		
		StringBuilder query = new StringBuilder();
		query.append("select count(Prefix) as PrefixCount FROM v_routes where Origin_AS = " + asn);
		
		System.out.println("QUERY: \n" + query.toString() + "\n");
		
		return RestResponse.okWithBody(
					DbUtils.select_DbToJson(mysql_ds, query.toString()));
	}

	@GET
	@Path("peer/{peerHashId}/asn/{ASN}/count")
	@Produces("application/json")
	public Response getRibOriginASCountByPeer(@PathParam("ASN") Integer asn,
			 						    @PathParam("peerHashId") String peerHashId) {
		if (asn == null)
			return Response.status(400).entity(
					"{ \"error\": \"Missing required path parameter\" }").build();
		
		StringBuilder query = new StringBuilder();
		query.append("select count(Prefix) as PrefixCount FROM v_routes where Origin_AS = " + asn);
		query.append(" and peer_hash_id = '" + peerHashId + "'");
		
		System.out.println("QUERY: \n" + query.toString() + "\n");
		
		return RestResponse.okWithBody(
					DbUtils.select_DbToJson(mysql_ds, query.toString()));
	}

	
	@GET
	@Path("/prefix/{prefix}")
	@Produces("application/json")
	public Response getRibByPrefix(@PathParam("prefix") String prefix,
						   @QueryParam("limit") Integer limit,
						   @QueryParam("where") String where,
						   @QueryParam("orderby") String orderby) {
		
		String where_str = "Prefix like '" + prefix + "%' ";
		
		if (where != null)
			where_str += " and " + where;
		
		return RestResponse.okWithBody(
				DbUtils.selectStar_DbToJson(mysql_ds, "v_routes", limit, where_str, orderby));
	}

	@GET
	@Path("/peer/{peerHashId}/prefix/{prefix}")
	@Produces("application/json")
	public Response getRibByPrefixByPeer(@PathParam("prefix") String prefix,
						   @PathParam("peerHashId") String peerHashId,
						   @QueryParam("limit") Integer limit,
						   @QueryParam("where") String where,
						   @QueryParam("orderby") String orderby) {
		
		String where_str = "peer_hash_id = '" + peerHashId + "' and Prefix like '" + prefix + "%' ";
		
		if (where != null)
			where_str += " and " + where;
		
		return RestResponse.okWithBody(
				DbUtils.selectStar_DbToJson(mysql_ds, "v_routes", limit, where_str, orderby));
	}

	
	@GET
	@Path("/prefix/{prefix}/{length}")
	@Produces("application/json")
	public Response getRibByPrefixLength(@PathParam("prefix") String prefix,
						   			     @PathParam("length") Integer length,
						   			     @QueryParam("limit") Integer limit,
						   			     @QueryParam("where") String where,
						   			     @QueryParam("orderby") String orderby) {
		
		if (length < 1 || length > 128)
			length = 32;
		
		String where_str = "Prefix = '" + prefix + "' and PrefixLen = " + length;
		
		if (where != null)
			where_str += " and " + where;
		
		
		return RestResponse.okWithBody(
				DbUtils.selectStar_DbToJson(mysql_ds, "v_routes", limit, where_str, orderby));
	}
	
	@GET
	@Path("/peer/{peerHashId}/prefix/{prefix}/{length}")
	@Produces("application/json")
	public Response getRibByPrefixLengthByPeer(@PathParam("prefix") String prefix,
										 @PathParam("peerHashId") String peerHashId,
						   			     @PathParam("length") Integer length,
						   			     @QueryParam("limit") Integer limit,
						   			     @QueryParam("where") String where,
						   			     @QueryParam("orderby") String orderby) {
		
		if (length < 1 || length > 128)
			length = 32;
		
		String where_str = "peer_hash_id = '" + peerHashId + "' and Prefix = '" + prefix + "' and PrefixLen = " + length;
		
		if (where != null)
			where_str += " and " + where;
		
		
		return RestResponse.okWithBody(
				DbUtils.selectStar_DbToJson(mysql_ds, "v_routes", limit, where_str, orderby));
	}
	
	@GET
	@Path("/lookup/{IP}")
	@Produces("application/json")
	public Response getRibByLookup(@PathParam("IP") String ip,
						   @QueryParam("limit") Integer limit,
						   @QueryParam("where") String where,
						   @QueryParam("orderby") String orderby) {
		
		StringBuilder query = new StringBuilder();
		
		// Query first for the prefix/len
		query.append("SELECT distinct prefix,prefix_len FROM rib force index (idx_range_prefix_bin)\n");
		query.append("        WHERE prefix_bcast_bin >= inet6_aton('" + ip + "')\n");
		query.append("               and prefix_bin <= inet6_aton('" + ip + "')\n");
        query.append("        ORDER BY prefix_bcast_bin limit 1\n");
		
		long startTime = System.currentTimeMillis();
		System.out.println("QUERY: \n" + query.toString() + "\n");
        
     	Map<String,List<DbColumnDef>> ResultsMap;
     	ResultsMap = DbUtils.select_DbToMap(mysql_ds, query.toString());
        
     	if (ResultsMap.size() <= 0) {
     		// No results to return
     		return RestResponse.okWithBody("{}");
     	} 
     	
     	else {
     		// Query v_routes for the prefix found
     		String prefix = ResultsMap.entrySet().iterator().next().getValue().get(0).getValue();
     		String prefix_len = ResultsMap.entrySet().iterator().next().getValue().get(1).getValue();
     		
     		StringBuilder where_str = new StringBuilder();
     		where_str.append("prefix = \"" + prefix + "\"");
     		where_str.append(" AND prefixlen = " + prefix_len);
     				
     		if (where != null)
				where_str.append(" and " + where);
			
			return RestResponse.okWithBody(
					DbUtils.selectStar_DbToJson(mysql_ds, "v_routes", limit, where_str.toString(), orderby,
							(System.currentTimeMillis() - startTime)));
     	}
	}
	
	@GET
	@Path("/peer/{peerHashId}/lookup/{IP}")
	@Produces("application/json")
	public Response getRibByLookupByPeer(@PathParam("IP") String ip,
						   @PathParam("peerHashId") String peerHashId,
						   @QueryParam("limit") Integer limit,
						   @QueryParam("where") String where,
						   @QueryParam("orderby") String orderby) {
		
		StringBuilder query = new StringBuilder();
		
		// Query first for the prefix/len
		query.append("SELECT distinct prefix,prefix_len FROM rib force index (idx_range_prefix_bin)\n");
		query.append("        WHERE prefix_bcast_bin >= inet6_aton('" + ip + "')\n");
		query.append("               and prefix_bin <= inet6_aton('" + ip + "')\n");
		query.append("               and peer_hash_id = '"+ peerHashId + "'\n");
        query.append("        ORDER BY prefix_bcast_bin limit 1\n");
		
		long startTime = System.currentTimeMillis();
		System.out.println("QUERY: \n" + query.toString() + "\n");
        
     	Map<String,List<DbColumnDef>> ResultsMap;
     	ResultsMap = DbUtils.select_DbToMap(mysql_ds, query.toString());
        
     	if (ResultsMap.size() <= 0) {
     		// No results to return
     		return RestResponse.okWithBody("{}");
     	} 
     	
     	else {
     		// Query v_routes for the prefix found
     		String prefix = ResultsMap.entrySet().iterator().next().getValue().get(0).getValue();
     		String prefix_len = ResultsMap.entrySet().iterator().next().getValue().get(1).getValue();
     		
     		StringBuilder where_str = new StringBuilder();
     		where_str.append("peer_hash_id = '" + peerHashId + "'\n");
     		where_str.append("and prefix = \"" + prefix + "\"");
     		where_str.append(" AND prefixlen = " + prefix_len);
     				
     		if (where != null)
				where_str.append(" and " + where);
			
			return RestResponse.okWithBody(
					DbUtils.selectStar_DbToJson(mysql_ds, "v_routes", limit, where_str.toString(), orderby,
							(System.currentTimeMillis() - startTime)));
     	}
	}
	
	@GET
	@Path("/history/{prefix}/{length}")
	@Produces("application/json")
	public Response getRibHistory(@PathParam("prefix") String prefix,
								  @PathParam("length") Integer length,
								  @QueryParam("limit") Integer limit,
								  @QueryParam("days") Integer days,
								  @QueryParam("where") String where,
								  @QueryParam("orderby") String orderby) {
		
		if (length < 1 || length > 128)
			length = 32;
		
		String	where_str = "Prefix = '" + prefix + "' and PrefixLen = " + length;
		
		if (days != null && days >= 15)
			where_str += " and LastModified >= date_sub(current_timestamp, interval " + days + " day)";		
		else
			where_str += " and LastModified >= date_sub(current_timestamp, interval 2 day)";
		
		where_str += " and LastModified <= current_timestamp ";
		
		if (where != null)
			where_str += " and " + where;
		
		return RestResponse.okWithBody(
				DbUtils.selectStar_DbToJson(mysql_ds, "v_routes_history", limit, where_str, orderby));
	}

	@GET
	@Path("/peer/{peerHashId}/history/{prefix}/{length}")
	@Produces("application/json")
	public Response getRibHistoryByPeer(@PathParam("prefix") String prefix,
			 					  @PathParam("peerHashId") String peerHashId,
								  @PathParam("length") Integer length,
								  @QueryParam("limit") Integer limit,
								  @QueryParam("days") Integer days,
								  @QueryParam("where") String where,
								  @QueryParam("orderby") String orderby) {
		
		if (length < 1 || length > 128)
			length = 32;
		
		String	where_str = "peer_hash_id = '"+ peerHashId + "' and Prefix = '" + prefix + "' and PrefixLen = " + length;
		
		if (days != null && days >= 15)
			where_str += " and LastModified >= date_sub(current_timestamp, interval " + days + " day)";		
		else
			where_str += " and LastModified >= date_sub(current_timestamp, interval 2 day)";
		
		where_str += " and LastModified <= current_timestamp ";
		
		if (where != null)
			where_str += " and " + where;
		
		return RestResponse.okWithBody(
				DbUtils.selectStar_DbToJson(mysql_ds, "v_routes_history", limit, where_str, orderby));
	}

	
	/**
	 * Cleanup
	 */
	/**
     @PreDestroy 
     void destory() { 
     	System.err.println("DESTORY"); 
     }
	 */
}
