package jpo.plmprovider;
// QueryBase.java
//
// Created on Oct 11, 2006
//
// Copyright (c) 2005-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import java.math.BigDecimal;

import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UITable;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.plmprovider.FilterClause;
import com.matrixone.apps.plmprovider.NameValue;
import com.matrixone.apps.plmprovider.NodeType;
import com.matrixone.apps.plmprovider.QueryRequest;
import com.matrixone.apps.plmprovider.QueryResponse;
import com.matrixone.apps.plmprovider.QueryResult;
import com.matrixone.apps.plmprovider.QueryMultiRootRequest;
import com.matrixone.apps.plmprovider.QueryMultiRootResponse;

import matrix.db.Context;
import matrix.util.StringList;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.RelationshipWithSelectList;
import matrix.db.RelationshipWithSelect;
import matrix.db.JPO;

/**
 * @author bucknam
 *
 * The <code>QueryBase</code> class provides web services associated with database queries.
 *
 * @version AEF 10.7.1.0 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class QueryBase_mxJPO extends jpo.plmprovider.Mat3DLive_mxJPO
{
    private static final String SELECT_ID = "id";
    private static final String SELECT_FIRST_ID = "first.id";
    private static final String SELECT_TYPE = "type";
    private static final String SELECT_REVISION_INDEX = "revindex";
    private static final String SELECT_LEVEL = "level";
    private static final String SELECT_MINOR_ID = "from[Active Version].to.id";
    private static final String SELECT_RELATIONSHIP_ID = "id[connection]";
    private static final String SELECT_RELATIONSHIP_TYPE = "type[connection]";
    private static final String RELATIONSHIP_VERSIONOF 		= PropertyUtil.getSchemaProperty("relationship_VersionOf");
    private static final String SELECT_FROM_VERSIONOF_TO_ID = "from["  + RELATIONSHIP_VERSIONOF + "].to.id";
    private static final String SELECT_FROM_VERSIONOF_TO_TYPE = "from["  + RELATIONSHIP_VERSIONOF + "].to.type";;
    private static final String SELECT_FROM_VERSIONOF_TO_REVINDEX = "from["  + RELATIONSHIP_VERSIONOF + "].to.revindex";
    private BigDecimal targetCost;
    private static final String SELECT_ATTR_SPATIAL_LOCATION    = "attribute[" + PropertyUtil.getSchemaProperty("attribute_SpatialLocation") + "]";
    private static final String SELECT_ATTR_COMPONENT_LOCATION  = "attribute[" + PropertyUtil.getSchemaProperty("attribute_ComponentLocation") + "]";
    private static final String SELECT_ATTR_QUANTITY            = "attribute[" + PropertyUtil.getSchemaProperty("attribute_Quantity") + "]";
	private static final String catiaLinkTypePattern = "CATProduct,Versioned CATProduct,CATPart,Versioned CATPart,CATDrawing,Versioned CATDrawing,CATIA Design Table,Versioned CATIA Design Table,CATProcess,Versioned CATProcess,CATShape,Versioned CATShape,CATIA Catalog,Versioned CATIA Catalog,CATAnalysis,Versioned CATAnalysis";
    private static final String catiaLinkRelPattern  = "CAD SubComponent,CatDesignTable,CatAnalysis,CatProcess,CatCatalog,CatMML,CATIA V5 Knowledge Import,CATIA V5 Geometry Import";
    private static final String catiaLinkToRelPattern = "Associated Drawing";
    /**
     * Constructor.
     *
     * @since AEF 10.7.1.0
     */

    public QueryBase_mxJPO()
    {
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
     */
    public QueryResponse doQuery(String username, String password, String language, QueryRequest request)
    {
        return doQuery(getContext(username,password), language, request);
    }

    /**
     * This service performs a query.  This method is for testing purpose only.
     *
     * @param context the matrix context
     * @param args the array should contain language and QueryRequest
     * @return a QueryResponse that will contain the resulting objects
     * @see QueryRequest
     */
    public QueryResponse doQuery(Context context, String args[]) throws Exception
    {
        HashMap programMap   = (HashMap)JPO.unpackArgs(args);
        String language      = (String)programMap.get("language");
        QueryRequest request = (QueryRequest)programMap.get("request");

        return doQuery(context, language, request);
    }

    /**
     * This service performs a query.  All of the required information to
     * do the query are sent in the QueryRequest object.
     *
     * @param context the matrix context
     * @param language the language for string translations.
     * @param request the information required to do the expand
     * @return a QueryResponse that will contain the resulting objects
     * @see QueryRequest
     * @since AEF 10.7.1.0
     */
    private QueryResponse doQuery(Context context, String language, QueryRequest request)
    {
        QueryResponse response = null;

        log("Query.doQuery start");

        try
        {
            // get columns, column selects and column types for the selected type
            NodeType nodeType       = (NodeType)nodeMap.get(FrameworkUtil.getAliasForAdmin(context, "type", getTypeFromHash(request.getFromType()), true));
            MapList mlColumns       = nodeType.giveTableColumns();
            HashMap hmColumnSelects = nodeType.giveTableColumnSelects();    //Key is column hash name and value is select
            HashMap hmColumnTypes   = nodeType.giveTableColumnTypes();      //Key is column hash name and value is column type

            StringList selects      = new StringList(6);
            selects.add(SELECT_FIRST_ID);
            selects.add(SELECT_REVISION_INDEX);
            selects.add(SELECT_MINOR_ID);
            selects.add("from[VersionOf].to.first.id");
            selects.add(SELECT_ID);
            selects.add(SELECT_TYPE);

            log("Query.doQuery find objects start");
            String whereClause = buildWhereClause(context, request, hmColumnSelects,hmColumnTypes);

            if(whereClause == null){
                whereClause = "";
            }
            whereClause = "("+whereClause+") && (policy != Version && policy != 'Versioned Design TEAM Policy')";

            log("Query.doQuery type <<" + getTypeFromHash(request.getFromType()) + ">> where <<" + whereClause + ">>");

            MapList fullMl = DomainObject.findObjects(context,
                                                  getTypeFromHash(request.getFromType()),
                                                  null, null, null,
                                                  "*", whereClause,
                                                  true,     // return subtypes as well: IR A0596008
                                                  selects);
            log("Query.doQuery find objects complete, " + fullMl.size() + " objects found");


            // filter out objects for which type mapping is not defined
            MapList ml = new MapList();
            for (int i = 0; i < fullMl.size(); i++)
            {
               Map map = (Map) fullMl.get(i);
               if (getHashFromType((String) map.get(SELECT_TYPE)) != null)
               {
                 ml.add(map);
               }
            }
            log("Query.doQuery filter out objects complete, " + ml.size() + " objects found");

            // get column values for filtered objects
            MapList mlFinalList = getColumnValues(context,mlColumns,ml,language);


            // If necessary, sort result
            log("Query.doQuery request.getSortAttribute() " + request.getSortAttribute());

            String sortAttribute = (String) request.getSortAttribute();
            if (sortAttribute != null && sortAttribute.length() > 0)
            {
                // Back in MetaData.addAttribute() we mapped "real" to "double" for the 3DLive client.
                // Now we need to map "double" back to "real" to make sort work.

                String sortType = (String)hmColumnTypes.get(sortAttribute);

                if (sortType.equalsIgnoreCase("double"))
                {
                    sortType = "real";
                }
                String sortSelect = (String)hmColumnSelects.get(sortAttribute);
                log("Query.doQuery sort on <<" + sortSelect + ">> by <<" + request.getSortDirection() +
                    ">> type <<" + sortType + ">>");
                mlFinalList.sort(sortSelect, request.getSortDirection(), sortType);
            }

            // compute the size and index of the result
            int pageNumber = request.getPageNumber();
            int pageSize = request.getPageSize();
            int startIndex = 0;
            int queryCount = mlFinalList.size();

            // if the values are reasonable
            if (pageNumber > 0 && pageSize > 0)
            {
                // compute the starting index (page numbers start at 1)
                startIndex = (pageNumber - 1) * pageSize;
                queryCount = pageSize;

                // if the request puts the start beyond the end
                if (startIndex >= mlFinalList.size())
                {
                    // return nothing
                    startIndex = 0;
                    queryCount = 0;
                }
                else
                {
                    // if the page size puts it beyond the end
                    if ((startIndex + queryCount) > mlFinalList.size())
                    {
                        // adjust the query count
                        queryCount = mlFinalList.size() - startIndex;
                    }
                }
            }

            log("Query.doQuery page <<" + pageNumber + ">> page size <<" + pageSize + ">>");
            log("Query.doQuery starting index <<" + startIndex + ">> count <<" + queryCount + ">>");

            response = new QueryResponse();
            QueryResult[] results = new QueryResult[queryCount];
            QueryResult result;
            Map map;
            NameValue[] values;
            NameValue value;

            String strHashName      = null;
            String strColumnSelect  = null;
            String strColumnValue   = null;
            int iColumnSize         = hmColumnSelects.size();
            Iterator iterator       = null;

            log("Query.doQuery about to iterate");

            for (int i = 0; i < queryCount; i++)
            {
                map = (Map) mlFinalList.get(i + startIndex);
                log("Query.doQuery map="+map);
                result = new QueryResult();
                results[i] = result;

                // set the object id and object type hash name into the result
                result.setObjectId((String) map.get(SELECT_ID));
                result.setObjectTypeHashName(getHashFromType((String) map.get(SELECT_TYPE)));

                log("QUERY.doQuery type <<" + (String) map.get(SELECT_TYPE) + ">>");
                log("Query.doQuery " + iColumnSize + " attributes");

                if (hmColumnSelects != null && iColumnSize > 0)
                {
                    nodeType = (NodeType) nodeMap.get(FrameworkUtil.getAliasForAdmin(context, "type", (String) map.get(SELECT_TYPE), true));
                    if(nodeType != null && "FALSE".equalsIgnoreCase(nodeType.giveDocumentRelationshipPattern()))
                    {
                        value = new NameValue("PLMNavNoCATIADoc_Open", "0");
                    }
                    else
                    {
                        value = new NameValue("PLMNavNoCATIADoc_Open", "1");
                    }

                    values = new NameValue[iColumnSize + 4];
                    values[0] = value;
                    if(map.get("from[VersionOf].to.first.id") != null)
                    {
                        value =  new NameValue("logical_ID", (String) map.get("from[VersionOf].to.first.id"));
                    }
                    else
                    {
                        value =  new NameValue("logical_ID", (String) map.get(SELECT_FIRST_ID));
                    }
                    log("PHYSICAL ID:" + (String) map.get(SELECT_ID) + "  LOGICAL ID:" + value.getValue());
                    values[1] = value;

                    // insert revindex for V5-V6 coexistense
                    value =  new NameValue("revindex", (String) map.get(SELECT_REVISION_INDEX));
                    values[2] = value;

                    // insert minor id for V5-V6 coexistense
                    if(map.get(SELECT_MINOR_ID) != null)
                    {
                   	  String minor_ID = null;
                   	  try{
        				    StringList minor_IDList = (StringList)map.get(SELECT_MINOR_ID);
        				    if(minor_IDList != null)
        				    {
        				    	minor_ID = (String)minor_IDList.get(0);
        				    }
        				  }
        				  catch(ClassCastException cexp )
        				  {
        					 minor_ID = (String)map.get(SELECT_MINOR_ID);
        				  }

        				  values[3]  = new NameValue("minor_ID", minor_ID);
                    }
                    else
                    {
                    	values[3]  = new NameValue("minor_ID", null);
                    }



                    iterator = hmColumnSelects.keySet().iterator();

                    for(int j = 0; iterator.hasNext(); j++)
                    {
                        strHashName     = (String) iterator.next();
                        strColumnSelect = (String) hmColumnSelects.get(strHashName);
                        strColumnValue  = (String) map.get(strColumnSelect);

                        if (hmColumnTypes.get(strHashName).equals("date"))
                        {
                            if (strColumnValue != null && strColumnValue.length() > 0)
                            {
                                Date date = eMatrixDateFormat.getJavaDate(strColumnValue);
                                strColumnValue = "" + (date.getTime() / 1000);
                            }
                        }

                        // name is the attribute hash name
                        value = new NameValue(strHashName,strColumnValue);

                        values[j + 4] = value;
                        log("Query.doQuery result attribute<<" + strColumnSelect + ">> hash << " + strHashName + ">> value <<" + (String) map.get(strColumnSelect) + ">>");
                    }

                    result.setValues(values);
                }
            }
            log("Query.doQuery iteration done");
            response.setResults(results);
            log("Query.doQuery results set");
        }
        catch (Exception e)
        {
            log("Query.doQuery exception " + e);
            // do nothing
        }

        log("Query.doQuery complete");
        return (response);
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
     */
    public int getCount(String username, String password, QueryRequest request)
    {
        return getCount(getContext(username,password), request);
    }

    /**
     * This service performs a query and returns a count of the number of objects
     * found. This method is for testing purpose only.
     *
     * @param context the matrix context
     * @param args the array should contain language and QueryRequest
     * @return a QueryResponse that will contain the resulting objects
     * @see QueryRequest
     */
    public int getCount(Context context, String args[]) throws Exception
    {
        HashMap programMap   = (HashMap)JPO.unpackArgs(args);
        QueryRequest request = (QueryRequest)programMap.get("request");

        return getCount(context, request);
    }


    /**
     * This service performs a query and returns a count of the number of objects
     * found.  All of the required information to do the query are sent in the
     * QueryRequest object.
     *
     * @param context the matrix context
     * @param request the information required to do the query
     * @return a count of the resulting objects
     * @since AEF 10.7.1.0
     */
   private int getCount(Context context, QueryRequest request)
   {
       log("Query.getCount start");

       MapList ml = new MapList();

       try
       {
           // get columns, column selects and column types for the selected type
           NodeType nodeType       = (NodeType)nodeMap.get(FrameworkUtil.getAliasForAdmin(context, "type", getTypeFromHash(request.getFromType()), true));
           HashMap hmColumnSelects = nodeType.giveTableColumnSelects();     //Key is column hash name and value is select
           HashMap hmColumnTypes   = nodeType.giveTableColumnTypes();       //Key is column hash name and value is column type

           StringList selects = new StringList(2);
           selects.add(SELECT_ID);
           selects.add(SELECT_TYPE);

           String whereClause = buildWhereClause(context, request, hmColumnSelects,hmColumnTypes);

           if(whereClause == null){
               whereClause = "";
           }
           whereClause = "("+whereClause+") && (policy != Version && policy != 'Versioned Design TEAM Policy')";

           log("Query.getCount request.getFromType <<" + request.getFromType() + ">>");
           log("Query.getCount type <<" + getTypeFromHash(request.getFromType()) + ">> where <<" + whereClause + ">>");
           MapList fullMl = DomainObject.findObjects(context,
                   getTypeFromHash(request.getFromType()),
                   null, null, null,
                   "*", whereClause,
                   true,     // return subtypes as well: IR A0596008
                   selects);

           // filter out objects for which type mapping is not defined
           for (int i = 0; i < fullMl.size(); i++)
           {
             Map map = (Map) fullMl.get(i);
             if (getHashFromType((String) map.get(SELECT_TYPE)) != null)
             {
               ml.add(map);
             }
           }

       }
       catch (Exception e)
       {
           log("Query.getCount exception " + e);
           // do nothing
       }

       log("Query.getCount <<" + ml.size() + ">> objects found");
       return (ml.size());
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
     */

     public QueryResponse doGetParents(String username, String password, String language, QueryRequest request)
     {
        log("Query.doGetParents");
        // always expand 1 level for parents
        request.setExpandLevel(1);
        return doExpand(getContext(username,password),  language, request, "parents");
     }

     /**
      * This service performs an expand. This method is for testing purpose only.
      *
      * @param context the matrix context
      * @param args the array should contain language and QueryRequest
      * @return a QueryResponse that will contain the resulting objects
      * @see QueryRequest
      */
     public QueryResponse doGetParents(Context context, String args[]) throws Exception
     {
         HashMap programMap     = (HashMap)JPO.unpackArgs(args);
         String language        = (String)programMap.get("language");
         QueryRequest request   = (QueryRequest)programMap.get("request");
         request.setExpandLevel(1);
         return doExpand(context,language, request,"parents");
     }

    /**
     * This service performs multi root expand.  All of the required information to
     * do the expand are sent in the Query Multi Root Request (QueryRequest Array).
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param language the language for string translations.
     * @param multiRootRequest the QueryRequest array
     * @return a QueryMultiRootResponse the QueryResponse array
     * @see QueryMultiRootRequest & QueryMultiRootResponse
     * @since BPS R209
     */
    public QueryMultiRootResponse doMultiRootExpand(String username, String password, String language, QueryMultiRootRequest multiRootRequest)
    {
        log("Query.doMultiRootExpand() Start");

        QueryRequest[] requestArray                 = multiRootRequest.getRequestArray();
        QueryResponse[]responseArray                = new QueryResponse[requestArray.length];
        QueryMultiRootResponse multiRootResponse    = new QueryMultiRootResponse();
        Context context                             = getContext(username,password);

        log("Query.doMultiRootExpand() request length  " +requestArray.length);

        for (int i=0; i < requestArray.length ; i++)
        {
            responseArray[i] = doExpand(context,language,requestArray[i],null);
        }
        multiRootResponse.setResponseArray(responseArray);

        log("Query.doMultiRootExpand() response length  " +responseArray.length);
        return multiRootResponse;
    }


    /**
     * This service performs multi root expand. This method is for testing purpose only.
     *
     * @param context the matrix context
     * @param args the array should contain language and QueryMultiRootRequest
     * @return a QueryMultiRootResponse the QueryResponse array
     * @see QueryMultiRootRequest & QueryMultiRootResponse
     * @since BPS R209
     */
    public QueryMultiRootResponse doMultiRootExpand(Context context, String args[]) throws Exception
    {
        log("Query.doMultiRootExpand() Start");

        HashMap programMap                          = (HashMap)JPO.unpackArgs(args);
        String language                             = (String)programMap.get("language");
        QueryMultiRootRequest multiRootRequest      = (QueryMultiRootRequest)programMap.get("multiRootRequest");
        QueryRequest[] requestArray                 = multiRootRequest.getRequestArray();
        QueryResponse[]responseArray                = new QueryResponse[requestArray.length];
        QueryMultiRootResponse multiRootResponse    = new QueryMultiRootResponse();

        log("Query.doMultiRootExpand() request length  " +requestArray.length);

        for (int i=0; i < requestArray.length ; i++)
        {
            responseArray[i] = doExpand(context,language,requestArray[i],null);
        }
        multiRootResponse.setResponseArray(responseArray);

        log("Query.doMultiRootExpand() response length  " +responseArray.length);
        return multiRootResponse;
    }

    /**
     * This service performs a expand for CATIALinks.  This method is for testing purpose only.
     *
     * @param context the matrix context
     * @param args the array should contain language and QueryRequest
     * @return a QueryResponse that will contain the resulting objects - all objects associated with CATIA links relationship
     * @see QueryRequest
     */
    public QueryResponse doExpandCATIALinks(Context context, String args[]) throws Exception
    {
    	log("Query.doExpandCATIALinks in externallaly callable method start");
        HashMap programMap   = (HashMap)JPO.unpackArgs(args);
        String catiaLinkExpandMode      = "LatestVersion";
        String language      = (String)programMap.get("language");
        QueryRequest request = (QueryRequest)programMap.get("request");
        log("Query.doExpandCATIALinks in externallaly callable method end");
        return doExpandCATIALinks(context, language, catiaLinkExpandMode, request);

    }

    /**
     * This service performs an expand on CATIA links.  All of the required information to
     * do the expand are sent in the QueryRequest i.e. id of the object to expand
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param language the language for string translations.
     * @param request the information required to do the expand, mainly objectId for which CATIALinks is requested
     * @param catiaLinkExpandMode - LatestRevision, LatestVersion etc... this parameter is currently ignored, and assumes always
     *                              LatestVersion
     * @return a QueryResponse that will contain the resulting objects
     */
     public QueryResponse doExpandCATIALinks(String username, String password, String language, String catiaLinkExpandMode, QueryRequest request)
     {
    	 log("Query.doExpandCATIALinks start");
    	 request.putCatiaLinkExpand("true");
    	 request.putCatiaLinkExpandMode(catiaLinkExpandMode);
    	 request.setExpandLevel(-1);
    	 QueryResponse response = doExpand(getContext(username,password),language, request, null);
         log("Query.doExpandCATIALinks END");
    	 return response;
     }

     public QueryResponse doExpandCATIALinks(Context context, String language, String catiaLinkExpandMode, QueryRequest request)
     {
    	 log("Query.doExpandCATIALinks start");
    	 request.putCatiaLinkExpand("true");
    	 request.putCatiaLinkExpandMode(catiaLinkExpandMode);
    	 request.setExpandLevel(-1);
    	 QueryResponse response = doExpand(context,language, request, null);
         log("Query.doExpandCATIALinks END");
    	 return response;
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
     */
     public QueryResponse doExpand(String username, String password, String language, QueryRequest request)
     {
        return doExpand(getContext(username,password),language, request, null);
     }

     /**
      * This service performs an expand. This method is for testing purpose only.
      *
      * @param context the matrix context
      * @param args the array should contain language and QueryRequest
      * @return a QueryResponse that will contain the resulting objects
      * @see QueryRequest
      */
     public QueryResponse doExpand(Context context, String args[]) throws Exception
     {
         HashMap programMap     = (HashMap)JPO.unpackArgs(args);
         String language        = (String)programMap.get("language");
         QueryRequest request   = (QueryRequest)programMap.get("request");

         return doExpand(context,language, request,null);
     }

   /**
    * This service performs an expand.  All of the required information to
    * do the expand are sent in the Query Request.
    *
    * @param context the matrix context
    * @param language the language for string translations.
    * @param request the information required to do the expand
    * @return a QueryResponse that will contain the resulting objects
    * @since AEF 10.7.1.0
    */
    private QueryResponse doExpand(Context context, String language, QueryRequest request, String direction)
    {
        QueryResponse response = null;

        log("Query.doExpand start");

        try
        {
            // get columns, column selects and column types for the selected type
            NodeType node           = (NodeType)nodeMap.get(FrameworkUtil.getAliasForAdmin(context, "type", getTypeFromHash(request.getFromType()), true));
            MapList mlColumns       = node.giveTableColumns();
            HashMap hmColumnSelects = node.giveTableColumnSelects();    //Key is column hash name and value is select
            HashMap hmColumnTypes   = node.giveTableColumnTypes();      //Key is column hash name and value is column type

            String AttributeQuantity = null;
            String AttributeRefDesignator = null;
            String selectAttributeQuantity = null;
            String selectAttributeRefDesignator = null;
            StringList slPositionMatrixSelects  = null;
            Iterator itr;

            log("Query.doExpand expand object start OID : " + request.getObjectId());
            DomainObject object = new DomainObject(request.getObjectId());
            int expandLevel = request.getExpandLevel();
            String  catiaLinkExpand     = request.giveCatiaLinkExpand();
            String  catiaLinkExpandMode = request.giveCatiaLinkExpandMode();   //this parameter is not used now, just for future. Currently expand always considers "Latest Version"

            StringList selects = new StringList(6);
            selects.add(SELECT_FIRST_ID);
            selects.add("from[VersionOf].to.first.id");
            selects.add(SELECT_MINOR_ID);
            selects.add(SELECT_REVISION_INDEX);
            selects.add(SELECT_ID);
            selects.add(SELECT_TYPE);

            // if designer central environment(for CAD Models only), we need to test if object is
            // finalized or a work in progress
            String activeId = null;
            boolean isWIP   = false;
            /* if (expandLevel != 0 )
            { */
                activeId = getActiveObjectID(context, request.getObjectId());
                if(!activeId.equals(request.getObjectId()))
                {
                    isWIP   = true;
                    object  = new DomainObject(activeId);
                    selects.add(SELECT_FROM_VERSIONOF_TO_ID);
                    selects.add(SELECT_FROM_VERSIONOF_TO_TYPE);
                    selects.add(SELECT_FROM_VERSIONOF_TO_REVINDEX);
                }
            /* } */


            MapList ml = new MapList();


            // boolean skipIntermediateNodes = ("true".equalsIgnoreCase(request.getSkipIntermediateNodesDuringExpand())) ? true : false;
            boolean skipIntermediateNodes = false;
            log("QUERY.doExpand request.getFromType <<" + request.getFromType() + ">>");
            log("QUERY.doExpand request.getFromType <<" + FrameworkUtil.getAliasForAdmin(context, "type", getTypeFromHash(request.getFromType()), true) + ">>");

            skipIntermediateNodes = (node.getSkipIntermediateNodeDuringExpand() != null && "true".equalsIgnoreCase(node.getSkipIntermediateNodeDuringExpand())) ? true : false;

            // if the expand is to get children: use ExpandRelationshipPattern, ExpandTypePattern
            // if the expand is to get children: use GetParentsRelationshipPattern, GetParentsTypePattern
            //          if the above are null, then use corresponding properties of defined for "get children"
            String typePattern = (!"parents".equals(direction) || direction == null) ? node.getExpandTypePattern() : ((node.getGetParentsTypePattern() != null && node.getGetParentsTypePattern().length() > 0) ? node.getGetParentsTypePattern() : node.getExpandTypePattern());
            String relationshipPattern = (!"parents".equals(direction) || direction == null) ? node.getExpandRelationshipPattern() : ((node.getGetParentsRelationshipPattern() != null && node.getGetParentsRelationshipPattern().length() > 0) ? node.getGetParentsRelationshipPattern() : request.getExpandRelationshipPattern());

            StringList relSelects = null;
            log("QUERY.doExpand LEVEL <<" + expandLevel + ">>");

            //for doExpandCATIALinks, adjust the rel pattern, type pattern, expand levels
            //so that required relationships are navigated
            boolean toDirection   =  (!"parents".equals(direction) || direction == null) ? false : true;
            boolean fromDirection =  (!"parents".equals(direction) || direction == null) ? true : false;
            StringList toDirectionSelectsforCatiaLinks = new StringList();

            if(catiaLinkExpand != null)
            {
            	// for CATIALinks we need to expand all with CATIAlinks rels in both direction,
               	if("true".equalsIgnoreCase(catiaLinkExpand))
            	{
            		relationshipPattern = populateCATIALinks(context);
            		typePattern   = catiaLinkTypePattern;
                    toDirection   = false;
                	fromDirection = true;

                    StringList toRels = FrameworkUtil.split(catiaLinkToRelPattern, ",");
                    for(int i=0;i<toRels.size();i++){
                        String toDirectionSelectforCatiaLinks = "to["+((String)toRels.get(i)).trim()+"].from.id";
                        DomainConstants.MULTI_VALUE_LIST.add(toDirectionSelectforCatiaLinks);
                        toDirectionSelectsforCatiaLinks.add(toDirectionSelectforCatiaLinks);
                        selects.add(toDirectionSelectforCatiaLinks);
                    }
            	}
            }
            // zero expand level means get info on the object only
            if (expandLevel == 0)
            {
                Map map = object.getInfo(context, selects);
                ml = new MapList(1);
                ml.add(map);
            }
            else
            {
                 String whereClause = buildWhereClause(context, request, hmColumnSelects,hmColumnTypes);

                // -1 expand level means expand all for the request
                // but for M1 zero means expand all
                if (expandLevel == -1)
                {
                    expandLevel = 0;
                }

                // if skipIntermediateNodeDuringExpand is set to true
                // always expand 2 levels, even if expand all

                if(skipIntermediateNodes)
                {
                    expandLevel = 2;
                }

                relSelects = new StringList(6);
                relSelects.add(SELECT_RELATIONSHIP_ID);
                relSelects.add(SELECT_RELATIONSHIP_TYPE);
                AttributeQuantity = PropertyUtil.getSchemaProperty(context,
                        DomainSymbolicConstants.SYMBOLIC_attribute_Quantity);
                AttributeRefDesignator = PropertyUtil.getSchemaProperty(context,
                        DomainSymbolicConstants.SYMBOLIC_attribute_ReferenceDesignator);
                selectAttributeQuantity = "attribute["+AttributeQuantity+"]";
                selectAttributeRefDesignator = "attribute["+AttributeRefDesignator+"]";
                relSelects.add(selectAttributeQuantity);
                relSelects.add(selectAttributeRefDesignator);
                slPositionMatrixSelects = getPositionMatrixSelects(context, request);
                relSelects.addAll(slPositionMatrixSelects);

                log("Query.doExpand Quantity selectable <<" + selectAttributeQuantity + ">>");
                log("Query.doExpand rel pattern <<" + node.getExpandRelationshipPattern() + ">>");
                log("Query.doExpand type pattern <<" + node.getExpandTypePattern() + ">>");
                if(relationshipPattern != null && relationshipPattern.length() > 0)
                {
                    if (isPartFromVPLMSync(context,request.getObjectId()))  {
                        context.setApplication(APPLICATION_VPLM);
                    }
                    ml = object.getRelatedObjects(context,
                            relationshipPattern,            // relationship pattern
                            typePattern,                    // type pattern
                            selects,                        // object Selects
                            relSelects,                     // relationship selects
                            toDirection,                    // to
                            fromDirection,                  // from
                            (short) expandLevel,            // no.of levels to expand
                            whereClause,                    // object where
                            null,                           // relationship where
                            (short)0,                       // limit
                            DomainObject.CHECK_HIDDEN,      // check hidden
                            false,                          // prevent duplicates
                            (short)DomainObject.PAGE_SIZE,  // page Size
                            null,                           // include type Pattern
                            null,                           // include relationship pattern
                            null,                           // include map
                            null);                          // relation key prefix
                }
                log("Query.doExpand selects="+selects
                        +" rel pattern="+node.getExpandRelationshipPattern()
                        +" type pattern="+node.getExpandTypePattern()
                        +" relSelects="+relSelects
                        +" expandLevel="+expandLevel
                        +" whereClause="+whereClause);

                // insert parent info in the list
                Map map = object.getInfo(context, selects);
                ml.add(0, map);

                if(catiaLinkExpand != null && "true".equalsIgnoreCase(catiaLinkExpand) && toDirectionSelectsforCatiaLinks.size() > 0)
                {
                    // for CATIALinks we need to get the info for to side relationships seperately,
                   Iterator mlItr = ml.iterator();
                   MapList newMapList = new MapList();
                   while(mlItr.hasNext()){
                       map = (Map)mlItr.next();

                       newMapList.add(new HashMap(map));

                       Set mapKeySet = map.keySet();
                       mapKeySet.retainAll(toDirectionSelectsforCatiaLinks);

                       if(mapKeySet.size() >0){
                           StringList toSideObjectIds = new StringList();
                           Iterator mapKeySetItr = mapKeySet.iterator();
                           while(mapKeySetItr.hasNext()){
                        	   StringList toList =new StringList();
                        	   Object obj = map.get(mapKeySetItr.next());
                        	   try {
                        		   toSideObjectIds.addAll((StringList)obj);
                        	   }catch(ClassCastException e) {
                        		   toList.add((String)obj);
                        		   toSideObjectIds.addAll(toList);
                           }
                           }
                           String[] toSideObjectIdsArray = (String[])toSideObjectIds.toArray(new String[toSideObjectIds.size()]);

                           MapList toSideObjectsInfo = DomainObject.getInfo(context, toSideObjectIdsArray, selects);
                           // manually set the level of "to" side obejcts as level "1"
                           Iterator toSideObjItr = toSideObjectsInfo.iterator();
                           while(toSideObjItr.hasNext()){
                               Map toSideMap = (Map) toSideObjItr.next();
                               toSideMap.put(SELECT_LEVEL, "1");
                           }
                           newMapList.addAll(toSideObjectsInfo);
                       }
                   }
                   ml = newMapList;
                }
            }

            //If environment is design Central, we need to reset the object Id and Object Type
            if(isWIP)
            {
                itr            = ml.iterator();
                MapList mlTemp = new MapList();

                for (int icnt = 0; itr.hasNext(); icnt++)
                {
                    Map map = (Map) itr.next();

                    if(isWIP && map.get(SELECT_FROM_VERSIONOF_TO_ID) != null)
                    {
                    	map.put(SELECT_MINOR_ID, map.get(SELECT_ID));
                        map.put(SELECT_ID,map.get(SELECT_FROM_VERSIONOF_TO_ID));
                        map.put(SELECT_TYPE,map.get(SELECT_FROM_VERSIONOF_TO_TYPE));
                        map.put(SELECT_REVISION_INDEX, map.get(SELECT_FROM_VERSIONOF_TO_REVINDEX));
                    }
                    mlTemp.add(map);
                }

                ml = mlTemp;
            }

            //Get Column Values for all obejcts
            MapList mlFinalList = getColumnValues(context,mlColumns,ml,language);

            log("Query.doExpand find objects complete, " + ml.size() + " objects found");

            response = new QueryResponse();

            Map map;
            Map intermediateMap = new HashMap();
            NameValue[] values;
            NameValue value;
            log("Query.doExpand about to iterate");

            itr = mlFinalList.iterator();
            int myLevel = 0;
            log("Query.doExpand result size prior to skip inter nodes :" + ml.size());
            log("Query.doExpand skipIntermediateNodes :" + Boolean.valueOf(skipIntermediateNodes).toString());
            Vector resultsVector = new Vector(mlFinalList.size());
            // remove intermediate nodes
            if(skipIntermediateNodes)
            {
                for (int i = 0; itr.hasNext(); i++)
                {
                    map = (Map) itr.next();
                    try
                    {
                      myLevel = Integer.parseInt((String) map.get(SELECT_LEVEL));
                    }
                    catch (NumberFormatException e)
                    {
                      myLevel = 0;
                    }
                    resultsVector.add(map);
                    if(myLevel == 1)
                    {
                        intermediateMap = map;
                    }
                    if(myLevel == 2)
                    {
                        resultsVector.remove(intermediateMap);
                        map.put(SELECT_LEVEL, "1");
                    }
                }
            }
            else
            {
                for (int i = 0; itr.hasNext(); i++)
                {
                    map = (Map) itr.next();
                    resultsVector.add(map);
                }
            }
             log("Query.doExpand result size after skip inter nodes :" + resultsVector.size());

             myLevel                            = 0;
             int prevFailedLevel                = 0;
             int iQuantity                      = 0;
             int tempNumber                     = 0;
             int iColumnSize                    = hmColumnSelects.size();
             boolean isFiltered                 = false;
             String positionMatrix              = null;
             String strHashName                 = null;
             String strColumnSelect             = null;
             String strColumnValue              = null;
             String strQuantity                 = null;
             String strObjectID             	= null;
             String strObjectTypeHashName   	= null;
             String strParentId             	= null;
             String strRelationShipType     	= null;
             Enumeration mlEnum                 = resultsVector.elements();
             ArrayList alQueryResults           = new ArrayList();
             StringList slPositionMatrixValues  = new StringList();
             QueryResult result                 = null;
             QueryResult[] results;

             for (int i = 0; i< resultsVector.size(); i++)
             {
                 values                         = new NameValue[iColumnSize + 5];
                 map                            = (Map)resultsVector.get(i);

                 log("Query.doExpand map="+map);

                 try
                 {
                     myLevel = Integer.parseInt((String) map.get(SELECT_LEVEL));
                 }
                 catch (NumberFormatException e)
                 {
                     myLevel = 0;
                 }

                 prevFailedLevel = (prevFailedLevel >= myLevel) ? 0 : prevFailedLevel;

                 if (getHashFromType((String) map.get(SELECT_TYPE)) != null)
                 {
                     isFiltered     = true;

                     if( prevFailedLevel == 0 || myLevel <= prevFailedLevel)
                     {
                         strObjectID            = (String) map.get(SELECT_ID);
                         strObjectTypeHashName  = getHashFromType((String) map.get(SELECT_TYPE));
                         strParentId            = (String) map.get("strParentId");
                         if(strParentId == null){
                             strParentId        = (String) map.get(SELECT_RELATIONSHIP_ID);
                         }
                         strRelationShipType    = (String) map.get(SELECT_RELATIONSHIP_TYPE);

                         try
                         {
                             strQuantity        = (String) map.get(selectAttributeQuantity);
                             iQuantity          = (new Float(strQuantity )).intValue();                         }
                         catch (Exception exp)
                         {
                        	 iQuantity        	= 1;
                         }


                         log("QUERY.doQuery type <<" + (String) map.get(SELECT_TYPE) + ">>");
                         log("Query.doExpand oid <<" + strObjectID + ">> rel type <<" + strRelationShipType + ">>");
                         log("Query.doExpand Quantity="+iQuantity);

                         if (hmColumnSelects != null && iColumnSize > 0)
                         {
                             node    = (NodeType) nodeMap.get(FrameworkUtil.getAliasForAdmin(context, "type", (String) map.get(SELECT_TYPE), true));

                             //values[0]
                             if(node != null && "FALSE".equalsIgnoreCase(node.giveDocumentRelationshipPattern()))
                             {
                                 values[0] = new NameValue("PLMNavNoCATIADoc_Open", "0");
                             }
                             else
                             {
                                 values[0] = new NameValue("PLMNavNoCATIADoc_Open", "1");
                             }

                             //values[1]
                             if(map.get("from[VersionOf].to.first.id") != null)
                             {
                                 values[1] = new NameValue("logical_ID", (String) map.get("from[VersionOf].to.first.id"));
                             }
                             else
                             {
                                 values[1]  =  new NameValue("logical_ID", (String) map.get(SELECT_FIRST_ID));
                             }

                             log("PHYSICAL ID:" + strObjectID + "  LOGICAL ID:" + values[1].getValue());

                             //values[2]
                             if(myLevel > 0 && map.get(selectAttributeRefDesignator) != null)
                             {
                                 values[2] = new NameValue("reference_Designator", (String) map.get(selectAttributeRefDesignator));
                             }
                             else
                             {
                                 values[2] = new NameValue("reference_Designator", "");
                             }

                             // insert the revindex for V5-V6 coexistense
                             values[3]  = new NameValue("revindex", (String) map.get(SELECT_REVISION_INDEX));

                             log("Rev Index" + values[3].getValue());

                             // insert the minor id for V5-V6 coexistense
                             if(map.get(SELECT_MINOR_ID) != null)
                             {
                            	 String minor_ID = null;
                            	 try{
                 				    StringList minor_IDList = (StringList)map.get(SELECT_MINOR_ID);
                 				    if(minor_IDList != null)
                 				    {
                 				    	minor_ID = (String)minor_IDList.get(0);
                 				    }
                 				  }
                 				  catch(ClassCastException cexp )
                 				  {
                 					 minor_ID = (String)map.get(SELECT_MINOR_ID);
                 				  }

                            	  values[4]  = new NameValue("minor_ID", minor_ID);
                             }
                             else
                             {
                            	 values[4]  = new NameValue("minor_ID", null);
                             }

                             log("Minor Id" + values[4].getValue());
                             log("Query.doExpand " + hmColumnSelects.size() + " attributes");

                             itr = hmColumnSelects.keySet().iterator();

                             for(int j = 0; itr.hasNext(); j++)
                             {
                                 strHashName     = (String) itr.next();
                                 strColumnSelect = (String) hmColumnSelects.get(strHashName);
                                 strColumnValue  = (String) map.get(strColumnSelect);

                                 if (hmColumnTypes.get(strHashName).equals("date"))
                                 {
                                     if (strColumnValue != null && strColumnValue.length() > 0)
                                     {
                                         Date date = eMatrixDateFormat.getJavaDate(strColumnValue);
                                         strColumnValue = "" + (date.getTime() / 1000);
                                     }
                                 }

                                 // name is the attribute hash name
                                 values[j + 5] = new NameValue(strHashName,strColumnValue);
                                 log("Query.doExpand result attribute<<" + strColumnSelect + ">> value <<" + strColumnValue + ">>");
                             }
                         }

                         // the following code is to set position matrix values.
                         slPositionMatrixValues     = getPositionMatrixValues(context,map,slPositionMatrixSelects);

                         MapList repeatingBom = new MapList();
                         int j = i+1;
                         if(iQuantity >1){
                        	 int mlLength = resultsVector.size();
                        	 for(;j< mlLength;j++){
                        		 Map childMap = (Map)resultsVector.get(j);
                        		 int childLevel = 0;
                        		 try{
                        		     childLevel = Integer.parseInt((String) childMap.get(SELECT_LEVEL));
                        		 }catch(NumberFormatException e){
                        			 childLevel = 0;
                        		 }

                        		 if(childLevel <= myLevel){
                        			 break;
                        		 }
                        		 repeatingBom.add(new HashMap(childMap));
                        	 }
                         }
                         int indexAfterRepeatingChildBom = j;

                         for (int iQtyItr = 0; iQtyItr < iQuantity ; iQtyItr++)
                         {
                            if (iQtyItr == 0)
                            {
                            	result  	= new QueryResult();
                                result.setLevel(myLevel);
                                result.setObjectId(strObjectID);
                                result.setObjectTypeHashName(strObjectTypeHashName);
                                result.setQuantity("1");
                                result.setValues(values);
                                result.setRelationshipType(strRelationShipType);

                                log("Query.doExpand Quantity Iterator  <<" + iQtyItr + ">>");

                                result.setParentId(strParentId);

                                if(slPositionMatrixValues != null && slPositionMatrixValues.size() > 0)
                                {
                                    try
                                    {
                                        positionMatrix  = (String)slPositionMatrixValues.get(iQtyItr);
                                        if(positionMatrix != null && positionMatrix.length() > 0 && positionMatrix.indexOf(",") > 0 )
                                        {
                                            result.setPositionMatrix(positionMatrix);
                                        }
                                        else
                                        {
                                            log("Query.doExpand error setting position matrix - data has invalid charecters ");
                                        }
                                    }
                                    catch(Exception ex)
                                    {
                                        log("Query.doExpand exception in setting position matrix" +ex.getMessage());
                                    }
                                }
                                log("Query.doExpand position Matrix <<" + result.getPositionMatrix() + ">>");

                                alQueryResults.add(result);

                            }
                            else
                            {
                            	HashMap repeatedPartMap = new HashMap(map);
                            	repeatedPartMap.put(selectAttributeQuantity,"1.0");
                            	repeatedPartMap.put("strParentId",strParentId+"."+iQtyItr);
                                if(slPositionMatrixValues != null){
                                    repeatedPartMap.put("positionMatrix", slPositionMatrixValues.get(iQtyItr));
                                }
                                MapList repeatedBOM = new MapList();
                                repeatedBOM.add(repeatedPartMap);
                                repeatedBOM.addAll(repeatingBom);

                                resultsVector.addAll(indexAfterRepeatingChildBom, repeatedBOM);

                                indexAfterRepeatingChildBom +=repeatedBOM.size();

                            }
                            log("Query.doExpand parent(rel) id  <<" + result.getParentId() + ">>");
                         }//end of for loop
                     }//end of pre failed level check

                 }
                 else
                 {
                     isFiltered = false;
                 }

                 if(!isFiltered)
                 {
                     prevFailedLevel = (prevFailedLevel < myLevel && prevFailedLevel > 0) ? prevFailedLevel : myLevel;
                 }
             }

             results = (QueryResult[])alQueryResults.toArray(new QueryResult[alQueryResults.size()]);

             response.setResults(results);
             log("Query.doExpand total objects " + results.length);
         }
        catch (Exception e)
        {
            log("Query.doExpand exception " + e);
            e.printStackTrace(System.out);
            // do nothing
        }

        log("Query.doExpand complete");
        return (response);
    }

    /*
     * Calculates the CATIA Links by reading the GCO.
     * for now, these links are hard coded based on current GCO setting
     *
     *  @return - relationship pattern to expand for doExpandCATIAlinks call
     */

    private String populateCATIALinks(Context context)
    {
    	String relPattern = "";
    	//DomainObject gcObject = new DomainObject("MxCATIAV5-GlobalConfig", "CatiaV5NewArch", "1", context.getVault());
    	// gcObject.getInfo(context, select);
    	relPattern = catiaLinkRelPattern;
    	return relPattern;
    }


    /**
     * This service gets the hash name of the type for the given business object.
     * This is necessary because the client is stateless and only keeps the
     * PLMID for Matrix objects.  We need the type on the client when requesting
     * an expand so the correct "View Table" is sent to the expand service.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param oid the id of the object
     * @return the hash name of the type
     * @since AEF 10.7.1.0
     */

    public String getTypeHashName(String username, String password, String oid)
    {
        return getTypeHashName(getContext(username,password),oid);
    }

    /**
     * This service gets the hash name of the type for the given business object.
     * This method is for testing purpose only.
     *
     * @param context the matrix context
     * @param args the array should contain object ID
     * @return the hash name of the type
     */
    public String getTypeHashName(Context context, String args[]) throws Exception
    {
        HashMap programMap   = (HashMap)JPO.unpackArgs(args);
        String objectId      = (String)programMap.get("objectId");

        return getTypeHashName(context, objectId);
    }

   /**
    * This service gets the hash name of the type for the given business object.
    * This is necessary because the client is stateless and only keeps the
    * PLMID for Matrix objects.  We need the type on the client when requesting
    * an expand so the correct "View Table" is sent to the expand service.
    *
    * @param context the matrix context
    * @param oid the id of the object
    * @return the hash name of the type
    * @since AEF 10.7.1.0
    */
   private String getTypeHashName(Context context, String oid)
   {
      String typeHashName = null;
      log("Query.getTypeHashName start");

      try
      {
          log("Query.getTypeHashName oid <<" + oid + ">>");

          StringList selects = new StringList(2);
          selects.add(SELECT_ID);
          selects.add(SELECT_TYPE);

          DomainObject object = new DomainObject(oid);
          Map map = object.getInfo(context, selects);

          typeHashName = getHashFromType((String) map.get(SELECT_TYPE));
      }
      catch (Exception e)
      {
          log("Query.getTypeHashName exception " + e);
          // do nothing
      }

      log("Query.getTypeHashName complete");
      return (typeHashName);
   }

   /**
    * Build a where clause that can be used for a query or expand.
    * The tableSelects are needed because the attributes in the
    * filters are hashed.
    *
    * @param context the the eMatrix <code>Context</code> object
    * @param request the Query Request
    * @param hmColumnSelects the hash map of Column selects keyed by column hash names.
    * @param hmColumnTypes the hash map of Column types keyed by column hash names.
    * @return the formulated where clause
    * @throws Exception
    * @since AEF 10.7.1.0
    */
   protected String buildWhereClause(Context context, QueryRequest request, HashMap hmColumnSelects,HashMap hmColumnTypes ) throws Exception
   {

       String logicalExpression     = request.getLogicalOperator();
       FilterClause[] filters       = request.getFilters();

       if (logicalExpression == null || logicalExpression.length() == 0)
       {
           return "";
       }
       // client sends one more attribute "Case_Sensitive" along with
       // other attributes for case sensitivity. Get this value(true/false)
       // to enable/disable case sensitivity.
       String caseSenstive  = "false";
       for (int i = 0; i < filters.length; i++)
       {
           if (filters[i].getAttributeName() != null && !"".equals(filters[i].getAttributeName()) &&
                   (filters[i].getAttributeName()).equals("Case_Sensitive"))
           {
               caseSenstive = filters[i].getValue();
               break;
           }
       }

       StringBuffer where = new StringBuffer(512);
       String whereStr = null;
       String prevToken = null;
       boolean isEndToken = false;
       String attributeName;
       String operator;
       String attributeValue;
       String strHashName;

       int index = 0;
       StringTokenizer st = new StringTokenizer(logicalExpression, "#", true);
       while (st.hasMoreTokens()) {

           String token = st.nextToken();

           if (token.equals("#"))
           {
               strHashName      = (String)filters[index].getAttributeName();
               attributeName    = (String)hmColumnSelects.get(strHashName);
               operator         = filters[index].getOperator();
               if (hmColumnTypes.get(strHashName).equals("date"))
               {
                       // Date date = eMatrixDateFormat.getJavaDate(filters[index].getValue());
                       // attributeValue = "" + (date.getTime() / 1000);
                       String timeInSeconds = filters[index].getValue();
                       long time = new Long(timeInSeconds).longValue() * 1000;
                       Date date = new Date(time);
                       DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
                       DateFormat tf = DateFormat.getTimeInstance(DateFormat.MEDIUM);
                       String attributeDateValue = df.format(date);
                       String attributeTimeValue = tf.format(date);
                       // add this code, until we get timezone info from client
                       // this code assumes client, server time zones as same
                       // TO DO
                       // once the client is enhanced to provide its time zone, this should be modified
                       TimeZone tz = df.getTimeZone();
                       int offsetInMilliSeconds = tz.getRawOffset();
                       double offsetInHours = offsetInMilliSeconds / (-1000 * 60 * 60);
                       attributeValue = eMatrixDateFormat.getFormattedInputDateTime(context, attributeDateValue,attributeTimeValue,offsetInHours);
               }
               else
               {
               attributeValue = filters[index].getValue();
               }

               if (operator.equals("like"))
               {
                   operator = "smatchlong";     // cases sensitive is false (default)
                   if(caseSenstive != null && !"".equals(caseSenstive) && caseSenstive.equals("true"))
                   {
                       operator = "~=";         // cases sensitive is true
                   }
               }
               // following is a workaround for handling a single star
               if (attributeValue.equals("*"))
               {
                   attributeValue = "**";
               }
               // if its just *, it is already taken care in the query API by means
               // of temp Query Bus Type * *
               // this greatly improves the performance as well
               // logical expression sent by the client will always be in one of te follwoing three ways
               // #
               // (# and # and #)
               // (# or # or #)
               if(logicalExpression.indexOf("and") > 0 && (attributeName.equals("type") || attributeName.equals("name") || attributeName.equals("revision")) && operator.equals("smatchlong") && attributeValue.equals("**"))
               {
                   // remove previous token from where clause
                   if("(".equals(prevToken))
                   {
                       isEndToken = true;
                   }
                   else
                   {
                       if(prevToken != null && where.toString().lastIndexOf(prevToken) > 0 && where.toString().endsWith(prevToken))
                       {
                           int dummy = where.toString().lastIndexOf(prevToken);
                           where = new StringBuffer(where.toString().substring(0, where.toString().lastIndexOf(prevToken)));
                       }
                   }
               }
               else if(logicalExpression.equals("#") && (attributeName.equals("type") || attributeName.equals("name") || attributeName.equals("revision")) && operator.equals("smatchlong") && attributeValue.equals("**"))
               {
                   // do nothing
               }
               else if(logicalExpression.indexOf("or") > 0 && (attributeName.equals("type") || attributeName.equals("name") || attributeName.equals("revision")) && operator.equals("smatchlong") && attributeValue.equals("**"))
               {
                   // Fix to IR C0597636    asterick i.e. * in the type or name or revision field in the search dialog should include blank value as well
                   // this is bcz * means everything
                   // there is no need to add type, name, revision to the where clause

                   where.append("(");
               where.append(attributeName);
               where.append(" ").append(operator);
               where.append(" \"").append(attributeValue).append("\"");

                   where.append(" or ");

                   where.append(attributeName);
                   where.append(" ").append(operator);
                   where.append(" \"\"");
                   where.append(")");
               }
               else
               {
                   where.append(attributeName);
                   where.append(" ").append(operator);
                   where.append(" \"").append(attributeValue).append("\"");
               }

               index++;
           }
           else
           {
               if(!isEndToken)
               {
               where.append(token);
           }
               else
               {
                   isEndToken = false;
               }
           }

           prevToken = token;
       }
       //exclude system generated documents(i.e versioned documents), if the
       //from type is a kind of DOCUMNETS and not a kind of MCAD*
       String fromType                   = getTypeFromHash(request.getFromType());
       NodeType nodeType                 = (NodeType)nodeMap.get(FrameworkUtil.getAliasForAdmin(context, "type", fromType, true));
       boolean excludeVersionedDocuments = nodeType.giveExcludeVersionedDocuments();
       if(excludeVersionedDocuments)
       {
            if(! "".equals(where.toString()) && where.toString().length() > 0)
            {
                where.append(" and ");
            }
            where.append("(attribute[");
            where.append(PropertyUtil.getSchemaProperty(context,"attribute_IsVersionObject"));
            where.append("] != 'True')");
       }
       if("()".equals(where.toString()) || where.toString().length() == 0)
       {
           whereStr = null;
       }
       else
       {
           whereStr = where.toString();
       }

       log("Query.buildWhereClause:" + where.toString());
       return (whereStr);
   }

   /**
    * Returns a hashmap of select clauses, keyed by attribute hash name.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param tableName the name of the table object
    * @param assignments the assignments of the current user
    * @param language the language of the resulting values
    * @return a hashmap of select clauses, keyed by attribute hash name
    * @throws Exception
    * @deprecated
    * @since AEF 10.7.1.0
    */
   protected static HashMap getSelects(Context context, String tableName, Vector assignments, String language)
       throws Exception
   {
       HashMap selects = null;

       // get user assignments
       if (assignments == null)
       {
           assignments = new Vector(1);
           // mpk - temporary to get this working
           assignments.add("all");
       }

       UITable table = new UITable();
       MapList columns = UITable.getColumns(context, tableName, assignments);

       if (columns == null)
       {
           String[] formatArgs = {tableName};
           String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.QueryBase.UnableToFindTable",formatArgs);
           throw new Exception(message);
       }

       HashMap tableControlMap = new HashMap();
       HashMap requestMap = new HashMap();
       requestMap.put(PARAM_LANGUAGE, language);
       table.processColumns(context, tableControlMap, columns, requestMap);

       // build the expressions
       selects = new HashMap();

       HashMap column;
       String select;
       Iterator columnItr = columns.iterator();
       for (int i = 0; columnItr.hasNext(); i++)
       {
           column = (HashMap) columnItr.next();

           if (isEmptyColumn(column) == false)
           {
               select = getColumnSelect(context, column);
               String displayName = UITable.getLabel(column);
               selects.put(getHashFromAttribute(displayName), select);
               //selects.put(getHashName(tableName + UITable.getName(column)), select);
           }
       }

       return (selects);
   }

   /**
    * Returns a hashmap of basic select clauses, keyed by attribute hash name.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param tableName the name of the table object
    * @param assignments the assignments of the current user
    * @param language the language of the resulting values
    * @return a hashmap of select clauses, keyed by attribute hash name
    * @throws Exception
    * @deprecated
    * @since AEF 10.7.1.0
    */
   protected static HashMap getBasicSelects(Context context, String tableName, Vector assignments, String language)
       throws Exception
   {
       HashMap selects = null;

       // get user assignments
       if (assignments == null)
       {
           assignments = new Vector(1);
           // mpk - temporary to get this working
           assignments.add("all");
       }

       UITable table = new UITable();
       MapList columns = UITable.getColumns(context, tableName, assignments);

       if (columns == null)
       {
           String[] formatArgs = {tableName};
           String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.QueryBase.UnableToFindTable",formatArgs);
           throw new Exception(message);
       }

       HashMap tableControlMap = new HashMap();
       HashMap requestMap = new HashMap();
       requestMap.put(PARAM_LANGUAGE, language);
       table.processColumns(context, tableControlMap, columns, requestMap);

       // build the expressions
       selects = new HashMap();

       HashMap column;
       String select;
       Iterator columnItr = columns.iterator();
       for (int i = 0; columnItr.hasNext(); i++)
       {
           column = (HashMap) columnItr.next();

           // if this is a basic attribute
           if ("true".equalsIgnoreCase(UITable.getSetting(column, SETTING_BASIC_ATTRIBUTE)))
           {
               if (isEmptyColumn(column) == false)
               {
                   select = getColumnSelect(context, column);
                   String displayName = UITable.getLabel(column);
                   selects.put(getHashFromAttribute(displayName), displayName);
                   //selects.put(getHashName(tableName + UITable.getName(column)), select);
               }
           }
       }

       return (selects);
   }

    /**
     * @deprecated
     *
     */
    protected void addBasicSelectsFromMaster(StringList selects)
    {
        StringList selectsFromMaster = new StringList(selects.size() + 3);
        Iterator selectItr = selects.iterator();
        String select;
        for (int i = 0; selectItr.hasNext(); i++)
        {
            select = (String) selectItr.next();
            selectsFromMaster.add(getBasicSelectsFromMaster(select));
        }
        if(!selects.contains("id"))
        {
            selectsFromMaster.add(getBasicSelectsFromMaster("id"));
        }
        if(!selects.contains("first.id"))
        {
            selectsFromMaster.add(getBasicSelectsFromMaster("first.id"));
        }
        if(!selects.contains(SELECT_REVISION_INDEX))
        {
            selectsFromMaster.add(getBasicSelectsFromMaster(SELECT_REVISION_INDEX));
        }

        Iterator selectsFromMasterItr = selectsFromMaster.iterator();
        for (int i = 0; selectsFromMasterItr.hasNext(); i++)
        {
            select = (String) selectsFromMasterItr.next();
            selects.add(select);
        }
        // return selects;
    }

    /**
     * @deprecated
     */
    protected String getBasicSelectsFromMaster(String select)
    {
        log("Query.getBasicSelectsFromMaster: before modification: " + select);
        if(select.equals("first.id"))
        {
            select = "from[VersionOf].to." + select;
        }
        else if(!select.startsWith("evalaute[if"))
        {
            select = "to[Active Version].from." + select;
        }
        else
        {
            select = "evaluate[if (to[Active Version].from.revision==last) then ('2') else ('1')]";
        }
        log("Query.getBasicSelectsFromMaster: after modification: " + select);
        return select;
    }

    /**
     * Returns Column Values
     *
     * This method is for Testing purpose only
     *
     * @param context the matrix context
     * @param args the array should contain Maplist of column details, object Id List and language
     * @return MapList of Column Values
     * @since R208
     */
    public MapList getColumnValues(Context context, String args[]) throws Exception
    {
        HashMap programMap          = (HashMap)JPO.unpackArgs(args);
        MapList mlProcessedColumns  = (MapList)programMap.get("columns");
        MapList mlObjectIDList      = (MapList)programMap.get("objectIdList");
        String strLanguage          = (String)programMap.get("language");
        return getColumnValues(context, mlProcessedColumns,mlObjectIDList,strLanguage);
    }



    /**
     * Returns Column Values
     *
     * This method gets Column Values Map from UITable for a given ObjectIDList.
     * From these Values, it will form a MapList in which each map contains
     * the column data for each object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param mlProcessedColumns the MapList of Column details
     * @param mlObjectIDList the MapList of Object Ids
     * @param strLanguage the Language
     * @return MapList of Column Values
     * @since R208
     */

    private MapList getColumnValues(Context context,MapList mlProcessedColumns, MapList mlObjectIDList,String strLanguage)
    throws Exception
    {
        log("Query.getColumnValues.Start");

        HashMap hmRequestMap    = new HashMap();    //Request Map
        HashMap hmTableData     = new HashMap();    //Table Data
        MapList mlFinalList     = new MapList();    //Final MapList
        UITable uiTable         = new UITable();    //UItable Reference

        try{

            hmRequestMap.put(PARAM_LANGUAGE, strLanguage);
            hmTableData.put("RequestMap",hmRequestMap);

            //Get Column Values From UITable
            HashMap hmColumnValuesMap           = uiTable.getColumnValuesMap(context,mlProcessedColumns,mlObjectIDList,hmTableData,false);

            //Get the BusinessObject, Relationship and Program Results from Column Values Map
            BusinessObjectWithSelectList bwsl   = (BusinessObjectWithSelectList)hmColumnValuesMap.get("Businessobject");
            RelationshipWithSelectList rwsl     = (RelationshipWithSelectList)hmColumnValuesMap.get("Relationship");
            Vector[] programResult              = (Vector[])hmColumnValuesMap.get("Program");

            int iColumnSize                     = mlProcessedColumns.size();  //No.of Columns
            int iObjectListSize                 = mlObjectIDList.size();      //No.of Objects

            String strColumnType                = null;                       //Column Type(i.e Program/Business Object..)
            String strColumnValue               = null;                       //Column Value
            String strColumnSelect              = null;                       //Column Select
            String strColumnLabel               = null;                       //Column Label
            String strKey                       = null;                       //Map Key
            StringList slColValueList           = null;                       //Column Value List

            Map objectMap                       = null;                       //Object Map
            HashMap hmColumnMap                 = null;                       //Column Map for each column
            HashMap hmFinalMap                  = null;                       //Final Map for each object (Key: Column Lable & Value:Column Value)

            Iterator iterator                   = null;                       //Iterator for Object Map


            //For each row(i.e for each object),get the corresponding column Values
            //from hmColumnValuesMap and put the values in hmFinalMap
            for (int i = 0; i < iObjectListSize; i++)
            {
                hmFinalMap  = new HashMap();

                 for (int k = 0; k < iColumnSize; k++)
                 {

                    hmColumnMap     = (HashMap)mlProcessedColumns.get(k);
                    strColumnType   = UITable.getSetting(hmColumnMap, "Column Type");
                    strColumnLabel  = UITable.getLabel(hmColumnMap);

                    if (strColumnType.equals("program"))
                    {
                    	HashMap hmProgram	= (HashMap)programResult[k].get(i);
                    	strColumnValue  	= (String)hmProgram.get("DisplayValue");
                        hmFinalMap.put(strColumnLabel,strColumnValue);
                    }
                    else if(strColumnType.equals("businessobject"))
                    {
                        strColumnSelect = UITable.getBusinessObjectSelect(hmColumnMap);
                        slColValueList  = (StringList)(bwsl.getElement(i).getSelectDataList(strColumnSelect));

                        if(slColValueList != null)
                        {
                            strColumnValue = (String)slColValueList.firstElement();
                            hmFinalMap.put(strColumnSelect,strColumnValue);
                        }
                     }
                     else if (strColumnType.equals("relationship") )
                     {
                        strColumnSelect    = UITable.getRelationshipSelect(hmColumnMap);
                        slColValueList     = (StringList)(((RelationshipWithSelect)rwsl.elementAt(i)).getSelectDataList(strColumnSelect));

                        if(slColValueList != null)
                        {
                            strColumnValue = (String)slColValueList.firstElement();
                            hmFinalMap.put(strColumnSelect,strColumnValue);
                        }
                     }
                 }

                 //If ObjectIDList contains any select/Relationship data then add them to hmFinalMap
                 objectMap  = (Map)mlObjectIDList.get(i);
                 iterator   = objectMap.keySet().iterator();

                 while (iterator.hasNext())
                 {
                     strKey     = (String) iterator.next();
                     hmFinalMap.put(strKey,objectMap.get(strKey));
                 }

                mlFinalList.add(hmFinalMap);
            }

        }
        catch(Exception ex)
        {
            log("Query.getColumnValues.Exception" +ex.getMessage());
        }

        log("Query.getColumnValues.Map Lise size<<" +mlFinalList.size() +">>");

        return(mlFinalList);
    }

    /**
     * Returns the Target Cost of given Object Id's
     * @param context the eMatrix <code>Context</code> object
     * @param args holds Object ID List
     * @return a vector of Target Costs
     * @throws Exception if the operation fails
     * @since R208
     *
     */
    public Vector getPartTargetCost(Context context, String[] args) throws Exception
    {
        HashMap hmProgramMap= (HashMap)JPO.unpackArgs(args);
        MapList mlObjList   = (MapList)hmProgramMap.get("objectList");
        Vector vec          = new Vector(mlObjList.size());

        try
        {
            String strObjectID  = "";
            Iterator itr        = mlObjList.iterator();

            while (itr.hasNext())
            {
                Map map     = (Map) itr.next();
                strObjectID = (String) map.get("id");

                // get Target Cost for each Object ID
                targetCost  = new BigDecimal("0.0");
                targetCost  = calculateTargetCost(context, strObjectID);
                vec.add(String.valueOf(targetCost));

            }

        }
        catch (Exception ex)
        {
            System.out.println("Exception in getPartTargetCost:  " + ex.getMessage());
            throw ex;
        }
        return vec;
    }

    /**
     *  Returns the Target Cost of given Object Id
     *
     * It calculates the target Cost as follows
     * First it checks, whether the given Part has any children or not.
     * If it has children, then it will get the sub children of that and so on.
     * Finally the target cost of any Part is calculated as sum of target cost
     * of all its children & sub children which does not have any child Parts.
     *
     * For instance, If Part0 has BOM like below
     *    Part0
     *        |_Part1
     *             |_ Part4
     *             |_ Part5
     *        |_Part2
     *             |_ Part6
     *        |_Part3
     *
     * then TargeCost of Part0 = TargetCost of Part4+ TargetCost of Part5 +
     *                           TargetCost of Part6+ TargetCost of Part3
     *
     *      TargeCost of Part1 = TargetCost of Part4+ TargetCost of Part5
     *
     *      TargeCost of Part2 = TargetCost of Part6
     *
     *      TargeCost of Part3 = TargetCost of Part3
     *
     * @param context the eMatrix <code>Context</code> object
     * @param strObjectID the Object ID
     * @return target cost of given object ID
     * @throws Exception if the operation fails
     * @since R208
     *
     */

    private BigDecimal calculateTargetCost(Context context, String strObjectID) throws Exception
    {

        try
        {
            MapList mlChildrenList  = getChildren(context,strObjectID);
            Iterator itr            = mlChildrenList.iterator();

            if (mlChildrenList != null && mlChildrenList.size()>0)
            {
                for (int icnt = 0; itr.hasNext(); icnt++)
                {
                    Map map             = (Map) itr.next();
                    String strChildId   = (String) map.get("id");
                    calculateTargetCost(context, strChildId);

                }
            }
            else
            {
                DomainObject object         = new DomainObject(strObjectID);
                String attributeTargetCost  = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_TargetCost") + "]";
                String strTargetCost        = object.getInfo(context, attributeTargetCost);
                targetCost                  = targetCost.add(new BigDecimal(strTargetCost));
            }
        }
        catch(Exception ex)
        {
            System.out.println ("Exception in calculateTargetCost:  "+ex.getMessage());
            throw ex;
        }

        return targetCost;

    }

    /**
     *  Returns the children List of given Object Id
     * @param context the eMatrix <code>Context</code> object
     * @param strObjectID the Object ID
     * @return MapList of Children List
     * @throws Exception if the operation fails
     * @since R208
     */

    private MapList getChildren(Context context, String strObjectID) throws Exception
    {
        MapList mlChildrenList  = null;


        try
        {

            StringList selects  = new StringList(1);
            selects.add("id");

            DomainObject dmObj  = new DomainObject(strObjectID);

            // expand the object to get the children list
            mlChildrenList      = dmObj.getRelatedObjects(context,
                                                "EBOM",     // relationship pattern
                                                "Part",     // type pattern
                                                selects,    // Object selects
                                                null,       // relationship selects
                                                false,      // from
                                                true,       // to
                                                (short)1,   //expand level
                                                null,       // object where
                                                null,       // relationship where
                                                0);         // limit

        }
        catch(Exception ex)
        {
            System.out.println ("Exception in getChildren:  "+ex.getMessage());
            throw ex;
        }

        return mlChildrenList;

    }
    /**
    * Returns a Position Matrix Selects
    *
    * @param context the eMatrix <code>Context</code> object
    * @param request the QueryRequest
    * @return Position Matrix Selects
    * @since BPS R209
    */
    private StringList getPositionMatrixSelects(Context context, QueryRequest request)
    {

        log("Query.getPositionMatrixSelects.start");
        StringList slPostionMatrixSelects  = new StringList();

        // TODO get attribute names using PropertyUtil.getSchemaProperty
        String ATTR_VPLM_V_MATRIX_1     = "attribute[LPAbstractInstance.V_matrix_1]";
        String ATTR_VPLM_V_MATRIX_2     = "attribute[LPAbstractInstance.V_matrix_2]";
        String ATTR_VPLM_V_MATRIX_3     = "attribute[LPAbstractInstance.V_matrix_3]";
        String ATTR_VPLM_V_MATRIX_4     = "attribute[LPAbstractInstance.V_matrix_4]";
        String ATTR_VPLM_V_MATRIX_5     = "attribute[LPAbstractInstance.V_matrix_5]";
        String ATTR_VPLM_V_MATRIX_6     = "attribute[LPAbstractInstance.V_matrix_6]";
        String ATTR_VPLM_V_MATRIX_7     = "attribute[LPAbstractInstance.V_matrix_7]";
        String ATTR_VPLM_V_MATRIX_8     = "attribute[LPAbstractInstance.V_matrix_8]";
        String ATTR_VPLM_V_MATRIX_9     = "attribute[LPAbstractInstance.V_matrix_9]";
        String ATTR_VPLM_V_MATRIX_10    = "attribute[LPAbstractInstance.V_matrix_10]";
        String ATTR_VPLM_V_MATRIX_11    = "attribute[LPAbstractInstance.V_matrix_11]";
        String ATTR_VPLM_V_MATRIX_12    = "attribute[LPAbstractInstance.V_matrix_12]";
        String SELECT_REL_VPLMPROJECTION_TO_REL= "frommid[VPLMInteg-VPLMProjection].torel.";
        try
        {
            String strObjectID                 = request.getObjectId();
            String strObjectType               = getTypeFromHash(request.getFromType());
            String strPositionMatrixSelect     = "";

            if (isPartFromVPLMSync(context,strObjectID))
            {//selected object type is EC Part and synchronized from VPLM

                log("Query.getPositionMatrixSelects. object is synchronized from VPLM");
                slPostionMatrixSelects.add(SELECT_REL_VPLMPROJECTION_TO_REL + ATTR_VPLM_V_MATRIX_1);
                slPostionMatrixSelects.add(SELECT_REL_VPLMPROJECTION_TO_REL + ATTR_VPLM_V_MATRIX_2);
                slPostionMatrixSelects.add(SELECT_REL_VPLMPROJECTION_TO_REL + ATTR_VPLM_V_MATRIX_3);
                slPostionMatrixSelects.add(SELECT_REL_VPLMPROJECTION_TO_REL + ATTR_VPLM_V_MATRIX_4);
                slPostionMatrixSelects.add(SELECT_REL_VPLMPROJECTION_TO_REL + ATTR_VPLM_V_MATRIX_5);
                slPostionMatrixSelects.add(SELECT_REL_VPLMPROJECTION_TO_REL + ATTR_VPLM_V_MATRIX_6);
                slPostionMatrixSelects.add(SELECT_REL_VPLMPROJECTION_TO_REL + ATTR_VPLM_V_MATRIX_7);
                slPostionMatrixSelects.add(SELECT_REL_VPLMPROJECTION_TO_REL + ATTR_VPLM_V_MATRIX_8);
                slPostionMatrixSelects.add(SELECT_REL_VPLMPROJECTION_TO_REL + ATTR_VPLM_V_MATRIX_9);
                slPostionMatrixSelects.add(SELECT_REL_VPLMPROJECTION_TO_REL + ATTR_VPLM_V_MATRIX_10);
                slPostionMatrixSelects.add(SELECT_REL_VPLMPROJECTION_TO_REL + ATTR_VPLM_V_MATRIX_11);
                slPostionMatrixSelects.add(SELECT_REL_VPLMPROJECTION_TO_REL + ATTR_VPLM_V_MATRIX_12);
            }
            else
            {//add default position Matrix selects

                slPostionMatrixSelects.add(SELECT_ATTR_SPATIAL_LOCATION);
                slPostionMatrixSelects.add(SELECT_ATTR_COMPONENT_LOCATION);
            }
        }
        catch(Exception ex)
        {
            log("Exception in getPositionMatrixSelects:  "+ex.getMessage());
        }

        log("Query.getPositionMatrixSelects.end");
        return slPostionMatrixSelects;
    }



   /**
    * Returns a Position Matrix Values
    *
    * @param context the eMatrix <code>Context</code> object
    * @param map the containing each object details
    * @param slPositionMatrixSelects the Position Matrix selects
    * @return Position Matrix Values
    * @since BPS R209
    */
    private StringList getPositionMatrixValues(Context context, Map map, StringList slPositionMatrixSelects)
    {

        log("Query.getPositionMatrixValues.start");

        int iBOMLevel                       = 0;                // BOM Level
        int iQuantity                       = 1;                // Quantity
        String strQuantity                  = null;             // Quantity
        String strPositionValue             = null;             // Position value
        String strPositionMatrixValue       = null;             // Position Matrix Value
        StringList slPositionValues         = new StringList(); // Position Values
        StringList slPositionMatrixValues   = new StringList(); // Position Matrix Values

        String calculatedPosMtrx = (String)map.get("positionMatrix");
        if(calculatedPosMtrx != null){
        	slPositionMatrixValues.add(calculatedPosMtrx);
        	return slPositionMatrixValues;
        }

        // get quantity
        try
        {
            strQuantity     = (String) map.get(SELECT_ATTR_QUANTITY);
            iQuantity       = (new Float(strQuantity )).intValue();
        }
        catch (Exception exp)
        {
            iQuantity = 1;
        }

        // get BOM Level
        try
        {
            iBOMLevel = Integer.parseInt((String) map.get(SELECT_LEVEL));
        }
        catch (NumberFormatException e)
        {
            iBOMLevel = 0;
        }

        log("Query.getPositionMatrixValues.Qunatity = " +iQuantity +" BOM Level = "+iBOMLevel);
        if(iBOMLevel == 0)
        {
            slPositionMatrixValues.add("0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0");
        }
        else
        {
            try
            {
                if (slPositionMatrixSelects.size() == 12)
                {// either VPM Product or EC Part synchronized from VPM

                    log("Query.getPositionMatrixValues. EC Part synchronized from VPM");

                    for (int iQtyItr = 0; iQtyItr < iQuantity ; iQtyItr++)
                    {
                        strPositionMatrixValue  = "";

                        for(int i = 0; i < 12; i++)
                        {
                            if (iQuantity == 1)
                            {
                                strPositionValue    = (String)map.get(slPositionMatrixSelects.get(i));
                            }
                            else
                            {
                                slPositionValues    = (StringList)map.get(slPositionMatrixSelects.get(i));
                                strPositionValue    = (String)slPositionValues.get(iQtyItr);
                            }

                            // VPLM position values will be in meters. Convert the attributes
                            // V_Matrix_10, V_Matrix_11 & V_Matrix_12(only) into millimeters.
                            // The other attributes represents rotations and they should not be converted.
                            if(i == 9 || i == 10 || i == 11)
                            {
                                strPositionValue    = convertPositionValue(strPositionValue);
                            }

                            strPositionMatrixValue += strPositionValue+",";
                        }

                        // remove comma at end
                        strPositionMatrixValue      = strPositionMatrixValue.substring(0,strPositionMatrixValue.length()-1);

                        slPositionMatrixValues.add(strPositionMatrixValue);
                    }
                }
                else
                {
                    log("Query.getPositionMatrixValues. not a VPM Product");

                    strPositionMatrixValue          = (String) map.get(SELECT_ATTR_SPATIAL_LOCATION);
                    if(strPositionMatrixValue == null || ("").equals(strPositionMatrixValue))
                    {
                        strPositionMatrixValue      = (String) map.get(SELECT_ATTR_COMPONENT_LOCATION);
                    }

                    if(iQuantity == 1)
                    {
                        slPositionMatrixValues.add(convertPrositionMatrixFromSLWtoCT5(strPositionMatrixValue));
                    }
                    else if (strPositionMatrixValue.indexOf('|') != -1)
                    {// TODO this will work only when EBOM sync from Designer Central updates the
                     // component location value correctly. (the number of position matrix values
                     // should be equal to quantity and they should be separated with some delimiter)

                        StringList slPositionMatrixValuesTmp  =  FrameworkUtil.split(strPositionMatrixValue, "|");
                        Iterator pmItr = slPositionMatrixValuesTmp.iterator();

                        while(pmItr.hasNext()){
                        	slPositionMatrixValues.add(convertPrositionMatrixFromSLWtoCT5((String)pmItr.next()));
                        }

                    }else if(UIUtil.isNullOrEmpty(strPositionMatrixValue)){
                        for(int i=0;i<iQuantity;i++){
                            slPositionMatrixValues.add("");
                        }
                    }
                }
            }
            catch(Exception exp)
            {
                slPositionMatrixValues = null;
                log("Query.getPositionMatrixValues error in position matrix " + exp.getMessage());
            }

        }

        log("Query.getPositionMatrixValues:End PositionMatrix Values" +slPositionMatrixValues);
        return slPositionMatrixValues;
    }

    private String convertPrositionMatrixFromSLWtoCT5(String strPositionMatrixValue){
    	/*
	        4x4 position matrix looks like

                            R1 R2 R3 T1
                            R4 R5 R6 T2
                            R7 R8 R9 T3
                            0  0  0  1

                        spatial location for CT5 and SLW models would be

                        CT5     - R1,R4,R7,0,R2,R5,R8,0,R3,R6,R9,0,T1,T2,T3,1
                        SLW     - R1,R4,R7,R2,R5,R8,R3,R6,R9,T1,T2,T3,1,0,0,0

                        SLW position matrix (if it ends with 0 or 0.0), needs to be transformed to CT5 model.
                            - the last 4 digits can be ignored since they are not used
                                (Note: 3Dlive client will recognise both 12 digit and 16 digit position matrices)
                            - T1,T2,T3 should be multiplied by 1000 (CT5 unit :mm, SLW unit: m)
                    */

    	if (strPositionMatrixValue.endsWith("0.0")
    			|| (strPositionMatrixValue.endsWith("0")&& !strPositionMatrixValue.endsWith(".0"))) {
    		StringList slpostionMatrixValues = FrameworkUtil.split(strPositionMatrixValue, ",");
    		slpostionMatrixValues.setSize(12);
    		slpostionMatrixValues.set(9, convertPositionValue((String)slpostionMatrixValues.get(9)));
    		slpostionMatrixValues.set(10, convertPositionValue((String)slpostionMatrixValues.get(10)));
    		slpostionMatrixValues.set(11, convertPositionValue((String)slpostionMatrixValues.get(11)));
    		strPositionMatrixValue = FrameworkUtil.join(slpostionMatrixValues, ",");
    	}
    	return strPositionMatrixValue;
    }


    /**
     * Returns a Position Matrix Values in millimeters
     *
     * @param value the position matrix value in meters
     * @return Position Matrix Value in millimeters
     * @since BPS R209
     */
    private static String convertPositionValue(String value)
    {
        String newValue = value;

        if (!value.equals("0.0") && !value.equals("1.0"))
        {
            double temp = Double.parseDouble(value);
            temp        = temp * 1000.0;
            newValue    = Double.toString(temp);
        }
        return newValue;
    }

}
