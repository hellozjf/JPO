package jpo.plmprovider;
// ${CLASSNAME}.java
//
// Created on 12-19-2006
//
// Copyright (c) 2006-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import com.matrixone.apps.plmprovider.FilterClause;
import com.matrixone.apps.plmprovider.NameValue;
import com.matrixone.apps.plmprovider.QueryRequest;
import com.matrixone.apps.plmprovider.QueryResponse;
import com.matrixone.apps.plmprovider.QueryResult;
import com.matrixone.apps.plmprovider.QueryMultiRootRequest;
import com.matrixone.apps.plmprovider.QueryMultiRootResponse;

import matrix.util.MatrixWrappedService;

/**
 * @author mkeirstead
 *
 * The <code>${CLASSNAME}</code> class provides web services associated with database queries.
 *
 * @version AEF 10.7.1.0 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class Query_mxJPO extends jpo.plmprovider.QueryBase_mxJPO implements MatrixWrappedService
{
    /**
     * Constructor.
     *
     * @since AEF 10.7.1.0
     */

    public Query_mxJPO()
    {
    }
    /**
     * This service performs an expand on CATIA links.  All of the required information to
     * do the expand are sent in the QueryRequest, mainly id of the object to expand 
     * this web service is called from V5-V6 coexistence engine only, not called from
     * regular 3DLive user actions
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param language the language for string translations.
     * @param request the information required to do the expand, mainly objectId for which CATIALinks is requested
     * @param catiaLinkExpandMode - LatestRevision, LatestVersion etc... this parameter is currently ignored, and assumes always 
     *                              LatestVersion 
     * @return a QueryResponse that will contain the resulting objects i.e. all objects connected with CATIA Links
     */    
    public QueryResponse doExpandCATIALinks(String username, String password, String language, String catiaLinkExpandMode, QueryRequest request)
    {
    	 return (super.doExpandCATIALinks(username, password, language, catiaLinkExpandMode, request));
    }
    /**
     * This service performs a query.  All of the required information to
     * do the query are sent in the QueryRequest object.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param language the language for string translations.
     * @param request the information required to do the expand
     * @return a QueryResponse that will contain the resulting objects
     * @see QueryRequest
     * @since AEF 10.7.1.0
     * */
    public QueryResponse doQuery(String username, String password, String language, QueryRequest request)
    {
        return (super.doQuery(username, password, language, request));
    }

    /**
     * This service performs a query and returns a count of the number of objects
     * found.  All of the required information to do the query are sent in the
     * QueryRequest object.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param request the information required to do the query
     * @return a count of the resulting objects
     * @since AEF 10.7.1.0
     */
    public int getCount(String username, String password, QueryRequest request)
    {
        return (super.getCount(username, password, request));
    }

    /**
     * This service performs an expand in reverse direction.  All of the required information to
     * do the expand are sent in the Query Request.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param language the language for string translations.
     * @param request the information required to do the expand
     * @return a QueryResponse that will contain the resulting objects
     * @since AEF X+2
     */
     public QueryResponse doGetParents(String username, String password, String language, QueryRequest request)
     {
        return (super.doGetParents(username, password, language, request));
     }


    /**
     * This service performs an expand.  All of the required information to
     * do the expand are sent in the Query Request.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param language the language for string translations.
     * @param request the information required to do the expand
     * @return a QueryResponse that will contain the resulting objects
     * @since AEF 10.7.1.0
     */

    public QueryResponse doExpand(String username, String password, String language, QueryRequest request)
    {
        return (super.doExpand(username, password, language, request));
    }


    /**
     * This service gets the hash name of the type for the given business object.
     * This is necessary because the client is stateless and only keeps the
     * PLMID for Matrix objects.  We need the type on the client when requesting
     * an expand so the correct "View Table" is sent to the expand service.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param oid the object id
     * @return a QueryResponse that will contain the resulting objects
     * @since AEF 10.7.1.0
     */
    public String getTypeHashName(String username, String password, String oid)
    {
        return (super.getTypeHashName(username, password, oid));
    }

     /**
      * This service performs multi root expand.  All of the required information to
      * do the expand are sent in the Query Multi Root Request (QueryRequest Array).
      *
      * @param username the matrix user name
      * @param password password of the user
      * @param language the language for string translations.
      * @param multiRootRequest the array of QueryRequest
      * @return a QueryMultiRootResponse that will contain the response objects
      * @see QueryMultiRootRequest & QueryMultiRootResponse
      * @since BPS R209
      */
     public QueryMultiRootResponse doMultiRootExpand(String username, String password, String language, QueryMultiRootRequest multiRootRequest)
     {
         return (super.doMultiRootExpand(username, password, language, multiRootRequest));
     }


    // The public unused* methods exist merely to expose the return type to ServiceGenerator,
    // for proper registration of the Axis serializer.
    /**
     * This method exists merely to expose the return type to ServiceGenerator,
     * for proper registration of the Axis serializer.
     *
     * @return QueryResult object
     * @since AEF 10.7.1.0
     */
    public QueryResult unusedQueryResult()
    {
        return null;
    }
    /**
     * This method exists merely to expose the return type to ServiceGenerator,
     * for proper registration of the Axis serializer.
     *
     * @return NameValue object
     * @since AEF 10.7.1.0
     */
    public NameValue unusedNameValue()
    {
        return null;
    }
    /**
     * This method exists merely to expose the return type to ServiceGenerator,
     * for proper registration of the Axis serializer.
     *
     * @return FilterClause object
     * @since AEF 10.7.1.0
     */
    public FilterClause unusedFilterClause()
    {
        return null;
    }
}
