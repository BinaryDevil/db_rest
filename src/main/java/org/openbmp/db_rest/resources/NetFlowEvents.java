/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.openbmp.db_rest.resources;

import org.openbmp.db_rest.DbUtils;
import org.openbmp.db_rest.RestResponse;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.sql.Timestamp;

@Path("/netflow")
public class NetFlowEvents {
    @Context
    ServletContext ctx;
    @Context
    UriInfo uri;

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
                    .lookup("java:/comp/env/jdbc/PostgreSQL");

        } catch (NamingException e) {
            System.err
                    .println("ERROR: Cannot find resource configuration, check context.xml config");
            e.printStackTrace();
        }
    }

    /**
     * Get netflow events
     *
     * @param startTimestamp if given and not null, will only respond with events after this timestamp
     * @return Rest response
     */
    @GET
    @Path("/events")
    @Produces("application/json")
    public Response getAllEvents(@QueryParam("eventid") String eventID,
                                 @QueryParam("startTs") String startTimestamp) {

        StringBuilder query = new StringBuilder();

        if (eventID != null && eventID.equals("null"))
            eventID = null;
        if (startTimestamp != null && startTimestamp.equals("null"))
            startTimestamp = null;

        query.append("SELECT * FROM netflow_events\n");
        if (eventID != null || startTimestamp != null)
            query.append("      WHERE\n");
        if (eventID != null) {
            eventID = "'" + eventID + "'";
            query.append("      event_id = " + eventID + "\n");
        }
        if (startTimestamp != null) {
            startTimestamp = "'" + startTimestamp + "'";
            query.append("      AND event_timestamp > " + startTimestamp + "\n");
        }

        System.out.println("QUERY: \n" + query.toString() + "\n");

        return RestResponse.okWithBody(
                DbUtils.select_DbToJson(mysql_ds, query.toString()));
    }
}
