package org.openbmp.db_rest.resources;


import java.math.BigInteger;
import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.openbmp.db_rest.RestResponse;
import org.openbmp.db_rest.DbUtils;

@Path("/events")
public class Events {
    @Context
    ServletContext ctx;
    @Context
    UriInfo uri;

    private DataSource postgresql_ds;

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
            postgresql_ds = (DataSource) initctx.lookup("java:/comp/env/jdbc/MySQLDB");

        } catch (NamingException e) {
            System.err.println("ERROR: Cannot find resource configuration, check context.xml config");
            e.printStackTrace();
        }
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getEvents(@PathParam("id") BigInteger id) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM bmp_events WHERE id > ");
        queryBuilder.append(id);
        queryBuilder.append(" ORDER BY id DESC");
        return RestResponse.okWithBody(DbUtils.select_DbToJson(postgresql_ds, queryBuilder.toString()));
    }

}
