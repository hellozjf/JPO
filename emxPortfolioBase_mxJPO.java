//
// $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/base/${CLASSNAME}.java 1.3.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
//
/*
 ** emxPortfolioBase
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 *
 */


import  java.util.HashMap;
import  java.util.List;
import  java.util.Map;

import  com.matrixone.apps.domain.DomainConstants;
import  com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import  com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;

import  matrix.db.Context;
import  matrix.db.JPO;
import  matrix.util.StringList;

/**
 * This JPO class has some method pertaining to Portfolio type
 * @version ProductCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxPortfolioBase_mxJPO extends emxDomainObject_mxJPO {

    /**
     * Create a new emxPortfolio object from a given id
     *
     * @param context context for this request
     * @param arg[0] the objectid
     * @return a emxPortfolio object
     * @exception Exception when unable to find object in the ProductCentral
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */

    protected static final String WILD_CHAR = "*";
    public emxPortfolioBase_mxJPO (Context context, String[] args) throws Exception
    {
        super(context, args);
    }

    /**
     * Main entry point
     *
     * @param context context for this request
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @exception Exception when problems occurred in the ProductCentral
     * @since ProductCentral 10.6
     * @grade 0
     */
    public int mxMain (Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            String language = context.getSession().getLanguage();
            String strContentLabel = EnoviaResourceBundle.getProperty(context,"ProductLine","emxProduct.Alert.FeaturesCheckFailed",language);
            throw  new Exception(strContentLabel);
        }
        return  0;
    }

    /**
     * Method call to get all the Portfolios in the data base.
     *
     * @param context context for this request
     * @param args-Holds the parameters passed from the calling method
     * @return Object - MapList containing the id of all Product Line objects related to context User's Company.
     * @exception Exception when problems occurred in the ProductCentral
     * @since ProductCentral 10.6
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllPortFolios (Context context, String[] args) throws Exception {
       MapList mapIds = getPortFolios(context, null,null);
        return  mapIds;
    }

    /**
     * Get the list of all owned Portfolios.
     *
     * @param context context for this request
     * @param args-Holds the parameters passed from the calling method
     * @return Object - MapList containing the id of all owned Portfolio objects by the context User.
     * @exception Exception when problems occurred in the ProductCentral
     * @since ProductCentral 10.6
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getOwnedPortFolios (Context context, String[] args) throws Exception {
        //Instantiating a StringList for fetching value of Company Id
        StringList strCompany = (StringList)ProductLineUtil.getUserCompanyIdName(context);
        // forming the Owner Pattern clause
        String strOwnerCondition = context.getUser();
        //Calls the protected method to retrieve the data
        MapList mapIds = getPortFolios(context, null,strOwnerCondition);
        return  mapIds;
    }

    /**
     * Get the list of Portfolios
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args-Holds the parameters passed from the calling method
     * @return MapList - MapList containing the id of Portfolios objects based on whereCondition .
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6
     * @grade 0
     */
    protected MapList getPortFolios (Context context, String strWhereCondition, String strOwnerCondition) throws Exception {
        //String list initialized to retrieve data for the Product Lines
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        String strType = ProductLineConstants.TYPE_PORTFOLIO;
        //The findobjects method is invoked to get the list of products
        MapList mapIds = findObjects(context, strType, null,null,strOwnerCondition,null,strWhereCondition,true, objectSelects);
        return  mapIds;
    }
     /**
         * Get the list of all Models on the context.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns bus ids  and rel ids of Test Cases
         * @throws Exception if the operation fails
         * @since ProductCentral 10.6
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public MapList getContents (Context context, String[] args) throws Exception {
            MapList relBusObjPageList = new MapList();
            StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
            StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String strObjectId = (String)programMap.get("objectId");
            this.setId(strObjectId);
            short sRecursionLevel = 1;
            String strRelName = ProductLineConstants.RELATIONSHIP_PORTFOLIO;
            //Getting the related
            relBusObjPageList = this.getRelatedObjects(context, strRelName, WILD_CHAR,
                    objectSelects, relSelects, false, true, sRecursionLevel, "",
                    "");
            return  relBusObjPageList;
        }

    /**
     * Get the list of all owned ProductLines.
     *
     * @param context context for this request
     * @param args-Holds the parameters passed from the calling method
     * @return Object - MapList containing the id of all owned Product Line objects by the context User.
     * @exception Exception when problems occurred in the ProductCentral
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */
       public String decrementItemCount (Context context, String[] args) throws Exception {
         //The Product object id sent by the emxTriggerManager is retrieved here.
        String objectId = args[0];
        //The object id is set to the context
        setId(objectId);
        String strCount=getAttributeValue(context,ProductLineConstants.ATTRIBUTE_COUNT);
        int iCount=Integer.parseInt(strCount)-1;
        strCount=String.valueOf(iCount);
        setAttributeValue(context,ProductLineConstants.ATTRIBUTE_COUNT,strCount);
        return strCount;
        }
    /**
     * The Trigger for Incrementing the value of the Attribute 'Count'.
     *
     * @param context context for this request
     * @param args-Holds the parameters passed from the calling method
     * @return Object - MapList containing the id of all owned Product Line objects by the context User.
     * @exception Exception when problems occurred in the ProductCentral
     * @since ProductCentral 10.6
     * @grade 0
     */
       public String incrementItemCount (Context context, String[] args) throws Exception {
         //The Product object id sent by the emxTriggerManager is retrieved here.
        String objectId = args[0];
        //The object id is set to the context
        setId(objectId);
        String strCount=getAttributeValue(context,ProductLineConstants.ATTRIBUTE_COUNT);
        int iCount=Integer.parseInt(strCount)+1;
        strCount=String.valueOf(iCount);
        setAttributeValue(context,ProductLineConstants.ATTRIBUTE_COUNT,strCount);
        return strCount;
        }

        /**
         * This method is used to display the roadmap for selected Portfolios in the
         * Portfolio List Page. It fecthes objectids of all the Products present under
         * selected portfolios. If the selected portfolio contains a model connected to
         * it then ids of all the products present under that model are also returned.
         *
         * @param context The ematrix context of the request.
         * @param args The string array containing following packed arguments:
         *                programMap - contains RequestValuesMap
         *                RequestValuesMap - cotains emxTableRowId of the selected
         *                                               portfolios.
         * @return MapList containing the object ids of the Products under selected
         *                          portfolios.
         * @throws Exception
         * @since ProductCentral10.6
         */
        @com.matrixone.apps.framework.ui.ProgramCallable
        public List getPortfolioProducts(
                                     Context context,
                                     String args[]) throws Exception {
        //The packed argument send from the JPO invoke method is unpacked to
        // retrive the HashMap.
        Map programMap = (HashMap) JPO.unpackArgs(args);

        //The RequestValuesMap is got through programMap.
        Map requestValuesMap = (HashMap) programMap.get("RequestValuesMap");

        //Getting the Selected Product Ids
        String[] strProductIds = (String[]) requestValuesMap
                .get("emxTableRowId");
        //Calling the method to separate the ObjectIds from Relationship Ids.
        Map reqMap = ProductLineUtil.getObjectIdsRelIds(strProductIds);

        //Getting the selected Object Ids into an array.
        String[] strPortfolioIds = (String[]) reqMap.get("ObjId");

        return getPortfolioProductIds(
                                      context,
                                      strPortfolioIds);
    }

    /**
     * This method returns all the Products connected to the Portfolios by
     * "Portfolio" relationship. If portfolio contains any Model connected to it
     * then all the Products connected to that Model by "Products" relationship
     * are also returned. Any duplicate Product is removed from the final MapList.
     *
     * @param context The ematrix context of the request.
     * @param strPortfolioIds The string array containing the object ids of
     *                                    portfolios.
     * @return MapList containing object ids of the Products.
     * @since ProductCentral10.6
     */
    public List getPortfolioProductIds(
                                       Context context,
                                       String[] strPortfolioIds)
            throws Exception {

        DomainObject domPortfolio = new DomainObject();

        //Form the Relationship pattern.
        StringBuffer sbRelPattern = new StringBuffer();
        sbRelPattern
                .append(ProductLineConstants.RELATIONSHIP_PORTFOLIO);
        sbRelPattern.append(",");
        sbRelPattern
                .append(ProductLineConstants.RELATIONSHIP_PRODUCTS);

        //Form the objects select list
        List lstObjSelects = new StringList(DomainObject.SELECT_ID);

        //Get the child types of Products to form the Post filter type pattern.
        List lstProductChildTypes = (StringList) ProductLineUtil
                .getChildrenTypes(
                                  context,
                                  ProductLineConstants.TYPE_PRODUCTS);
        StringBuffer sbPostPattern = new StringBuffer();
        for (int i = 0; i < lstProductChildTypes.size(); i++) {
            sbPostPattern.append(lstProductChildTypes.get(i));
            sbPostPattern.append(",");
        }
        sbPostPattern.append(ProductLineConstants.TYPE_PRODUCTS);

        List lstProducts = new MapList();
        List lstProductIds = new MapList();

        //Get all the Products connected to each Portfolio.
        for (int i = 0; i < strPortfolioIds.length; i++) {
            domPortfolio.setId(strPortfolioIds[i]);

            lstProducts = domPortfolio
                    .getRelatedObjects(
                                       context,
                                       sbRelPattern.toString(),
                                       DomainConstants.QUERY_WILDCARD,
                                       false,
                                       true,
                                       (short) 0,
                                       (StringList) lstObjSelects,
                                       null,
                                       null,
                                       null,
                                       null,
                                       sbPostPattern.toString(),
                                       null);

            //merge the Products list to remove duplicates
            mergeProductsLists(
                               (MapList) lstProductIds,
                               (MapList) lstProducts);
        }
        return lstProductIds;
    }

    /**
     * This method is used to merge the two MapLists by removeing the duplicate
     * Maps having same value for the key DomainConstants.SELECT_ID.
     *
     * @param lstDestList The maplist to be merged.
     * @param lstSourceList The final merged maplist which will be returned.
     * @return merged MapList.
     * @since ProductCentral10.6
     */
    public void mergeProductsLists(
                                   MapList lstDestList,
                                   MapList lstSourceList) {

        String strSourceProdId = DomainConstants.EMPTY_STRING;
        String strDestProdId = DomainConstants.EMPTY_STRING;
        Map mapProduct = new HashMap();
        boolean bProdExists = false;

        for (int i = 0; i < lstSourceList.size(); i++) {
            Map mapSourceProd = (Map) lstSourceList.get(i);
            strSourceProdId = (String)mapSourceProd.get(DomainConstants.SELECT_ID);
            bProdExists = false;
            for (int j = 0; j < lstDestList.size(); j++) {
                strDestProdId = (String)((Map) lstDestList.get(j))
                        .get(DomainConstants.SELECT_ID);
                if (strSourceProdId.equals(strDestProdId)) {
                    bProdExists = true;
                    break;
                }
            }
            if (!bProdExists) {
                mapProduct = new HashMap();
                mapProduct.put(DomainConstants.SELECT_ID,
                              (String)mapSourceProd.
                                  get(DomainConstants.SELECT_ID));
                mapProduct.put(DomainConstants.SELECT_LEVEL, "1");
                lstDestList.add(mapProduct);
            }
        }
    }

    /**
     * This method is used to get all the ids of all the Products
     * from the selected content amoung particular portfolio. If
     * the selected content is model then all the Products present
     * under that Model are retriebed.
     *
     * @param context The ematrix context object.
     * @param args String array containing the packed arguments.
     * @return List of product ids.
     * @throws Exception
     * @since ProductCentral10.6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public List getPortfolioContentProducts(
                                            Context context,
                                            String[] args) throws Exception {
        //The packed argument send from the JPO invoke method is unpacked to
        //retrive the HashMap.
        Map programMap = (HashMap) JPO.unpackArgs(args);

        //The RequestValuesMap is got through programMap.
        Map requestValuesMap = (HashMap) programMap.get("RequestValuesMap");

        //Getting the Selected Product Ids
        String[] strProductIds = (String[]) requestValuesMap
                .get("emxTableRowId");
        //Calling the method to separate the ObjectIds from Relationship Ids.
        Map reqMap = ProductLineUtil.getObjectIdsRelIds(strProductIds);

        //Getting the selected Object Ids into an array.
        String[] strContentIds = (String[]) reqMap.get("ObjId");

        return getPortfolioContentProductIds(
                                             context,
                                             strContentIds);
    }

    /**
     * This method is used to get all the ids of all the Products
     * from the selected content amoung particular portfolio. If
     * the selected content is model then all the Products present
     * under that Model are retriebed.
     *
     * @param context The ematrix context object.
     * @param strContentIds String array containing selected content ids.
     * @return List of product ids.
     * @throws Exception
     * @since ProductCentral10.6
     */
    public List getPortfolioContentProductIds(
                                              Context context,
                                              String[] strContentIds)
            throws Exception {

        DomainObject domContent = new DomainObject();

        //Form the Relationship pattern.
        StringBuffer sbRelPattern = new StringBuffer();
        sbRelPattern
                .append(ProductLineConstants.RELATIONSHIP_PORTFOLIO);
        sbRelPattern.append(",");
        sbRelPattern
                .append(ProductLineConstants.RELATIONSHIP_PRODUCTS);

        //Form the objects select list
        List lstObjSelects = new StringList(DomainObject.SELECT_ID);

        List lstProducts = new MapList();
        List lstProductIds = new MapList();
        Map mapProduct = new HashMap();

        String strContentType = DomainConstants.EMPTY_STRING;
        String strTypePattern = ProductLineConstants.TYPE_PRODUCTS+","+ProductLineConstants.TYPE_PORTFOLIO;
        //Get all the Products connected to each Portfolio.
        for (int i = 0; i < strContentIds.length; i++) {
            domContent.setId(strContentIds[i]);
            strContentType = domContent.getInfo(
                                                context,
                                                DomainConstants.SELECT_TYPE);

            if (!strContentType
                    .equals(ProductLineConstants.TYPE_MODEL)) {
                mapProduct = new HashMap();
                lstProducts = new MapList();
                mapProduct.put(DomainConstants.SELECT_ID, strContentIds[i]);
                mapProduct.put(DomainConstants.SELECT_LEVEL, "1");
                lstProducts.add(mapProduct);
            } else {

                lstProducts = domContent
                        .getRelatedObjects(
                                           context,
                                           sbRelPattern.toString(),
                                           strTypePattern,
                                           (StringList) lstObjSelects,
                                           null,
                                           false,
                                           true,
                                           (short) 0,
                                           null,
                                           null,0);
            }

                //merge the Products list to remove duplicates
                mergeProductsLists(
                                   (MapList) lstProductIds,
                                   (MapList) lstProducts);
        }
        return lstProductIds;
    }
}
