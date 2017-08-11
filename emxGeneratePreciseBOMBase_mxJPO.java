/*
 * emxGeneratePreciseBOMBase.java
 *
 * Copyright (c) 2004-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/base/${CLASSNAME}.java 1.9.2.3.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental$
 *
 * Last Updated On for Precise BOM enhancements and calculate expression signature change: 04th November 2003.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.LogicalFeature;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.productline.ProductLineConstants;


/**
 * This JPO class has some method pertaining to Precise BOM relationship.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxGeneratePreciseBOMBase_mxJPO extends emxDomainObject_mxJPO
{


    
    /**
     * Default constrctor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */

    public emxGeneratePreciseBOMBase_mxJPO (Context context, String[] args)
        throws Exception
    {
       super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     * @grade 0
     */

    public int mxMain(Context context, String[] args)
    throws Exception
    {
        String strLanguage = context.getSession().getLanguage();
        String strDesktopClientFailed = EnoviaResourceBundle.getProperty(context,"Configuration","emxProduct.Alert.DesktopClientFailed",strLanguage);
        if (!context.isConnected())
            throw  new Exception(strDesktopClientFailed);
        return  0;
    }



   
         /* Code for Clone Part  Starts here*/

        /**
         * This method will get the checkboxes for the Clone Part Functionality
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args program arguments
         * @return MapList
         * @throws Exception if the operation fails
         * @since Feature Configuration X3
         */

        public MapList dynamicColumnForCheckboxesClonePart(Context context, String[] args) throws Exception
        {
            if (args.length == 0 )
           {
             throw new IllegalArgumentException();
           }           
           MapList returnMap =new MapList();
           try{
                   Map mainMap = new HashMap();
                   Map updateMap = new HashMap();

                   mainMap.put("Registered Suite", "Configuration");
                   mainMap.put("Group Header", "emxProduct.ListHeading.Features");
                   mainMap.put("Column Type", "programHTMLOutput");
                   mainMap.put("function", "getCheckboxes");
                   mainMap.put("program", "emxGeneratePreciseBOM");
                   mainMap.put("Width", "100");

                   updateMap.put("settings", mainMap);
                   updateMap.put("label","emxProduct.Heading.Select");
                   updateMap.put("name", "Feature Selection");

                   returnMap.add(updateMap); 
                   return returnMap;

            }
            catch(Exception e){
                e.printStackTrace();
            return returnMap;
          }
        }

        /**
         * This method will get the checkboxes for the Clone Part Functionality
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args program arguments
         * @return Vector
         * @throws Exception if the operation fails
         * @since Feature Configuration X3
         */
        public Vector getCheckboxes(Context context,String[] args) throws Exception
        {
            HashMap programMap  = (HashMap)JPO.unpackArgs(args);
            MapList objList     = (MapList)programMap.get("objectList");
            
            int objListSize = objList.size();
            Vector columnVals   = new Vector(objListSize);
            String strObjId=null;
            String strIdLevel=null;
            String strParentId = null;
            DomainObject featureObject;
            Iterator itr;
            StringBuffer strbuf =null;
            Map objMap = null;
            try{
                for (int k=0; k < objListSize; k++)
                {
                    strbuf = new StringBuffer();
                   // Hashtable objMap =  (Hashtable) objList.get(k);
                    objMap = (Map) objList.get(k);
                    strObjId = (String)objMap.get("id");
                    strIdLevel = (String)objMap.get("id[level]");
                    
                    featureObject = new DomainObject(strObjId);
                    StringList strlistLogicalFeature = featureObject.getInfoList(context, "from["+ConfigurationConstants.RELATIONSHIP_LOGICAL_FEATURES+"].to.id");
    
                    if (!(strlistLogicalFeature.isEmpty())) 
                    {
                       itr = strlistLogicalFeature.iterator();
                       while(itr.hasNext())
                        {
                           //featureListObject = new DomainObject((String)itr.next());
                          // String strChildFeature = featureListObject.getInfo(context, "from["+ProductLineConstants.RELATIONSHIP_FEATURE_LIST_TO+"].to.id");
                           //childFeatureObject = new DomainObject(strChildFeature);
                           //String childObjectId = childFeatureObject.getId();
                           strbuf.append((String)itr.next());
                           strbuf.append(',');
                        }
                    }
                    strParentId = (String)objMap.get("id[parent]");
                    StringBuffer sbHref  = new StringBuffer();
                    sbHref.append("<div align=\"center\"><input type=\"checkbox\" name=\"");
                    sbHref.append(strObjId);
                    sbHref.append("|");
                    sbHref.append(strIdLevel);
                    sbHref.append("\" id=\"");
                    sbHref.append(strObjId);
                    sbHref.append("\" parentObjId=\"");
                    sbHref.append(strParentId);
                    sbHref.append("\" rowID=\"");
                    sbHref.append(strIdLevel);
                    sbHref.append("\"  childObjId=\"");
                    sbHref.append(strbuf.toString());
                    sbHref.append("\" onclick=\"selectedFeature('");
                    sbHref.append(strObjId);
                    sbHref.append("');\"/>");
                    sbHref.append("<input type=\"hidden\" name=\"hiddenObjectId\" id=\"");
                    sbHref.append(strObjId);
                    sbHref.append("\"/></div>");
                    columnVals.add(sbHref.toString());
                 }
                return columnVals;
            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
          }


        
        /**
         * This method displays Part number in Preview BOM dialog.
         * @param context the eMatrix Context object
         * @param args holds the following input arguments:
         *        paramMap a HashMap
         *        requestMap a HashMap
         * @return Vector Object which holds following parameter
         *        part number
         * @throws Exception if the operation fails
         * @since PRC BX3
         */
        public Vector displayPartNumber(Context context, String[] args) throws Exception
        {
            
            HashMap parametersMap = (HashMap)JPO.unpackArgs(args);
            MapList objectList = (MapList)parametersMap.get("objectList");
            HashMap paramList = (HashMap) parametersMap.get("paramList");  
            String strContextPartId = (String)paramList.get("contextPartId");
            DomainObject domPart = new DomainObject(strContextPartId);
            String contextPartName = domPart.getInfo(context,DomainObject.SELECT_NAME);
            StringBuffer stbPartName = new StringBuffer();
            StringBuffer stbPartImage = new StringBuffer();
            Vector vPartName = new Vector();
            for(int i = 0; i < objectList.size(); i++)
            {
                Map objectMap = (Map)objectList.get(i);
                String strDuplicatePartName = "";
                String strObjectLevel = (String)objectMap.get("id[level]");
                stbPartName.delete(0, stbPartName.length());
                stbPartImage.delete(0, stbPartImage.length());
                if(!(strObjectLevel.equalsIgnoreCase("0"))){
                strDuplicatePartName = "???";
                stbPartImage = stbPartImage.append("<img src=\"../common/images/iconSmallPart.gif").append("\"").append("/>");
                }
                else{
                    strDuplicatePartName = contextPartName;
                    stbPartImage = stbPartImage.append("<img src=\"../common/images/iconSmallPart.gif").append("\"").append("/>");
                }
                vPartName.add(stbPartImage+" "+strDuplicatePartName);
                
            }
            return vPartName;
        }
        
        /**
         * This method displays visual cue column
         * @param context
         * @param args
         * @return Vector
         * @throws Exception
         * @since PRC BX3
         */
        public Vector displayVisualCue(Context context, String args[]) throws Exception
        {
            HashMap parametersMap = (HashMap)JPO.unpackArgs(args);
            MapList objectList = (MapList)parametersMap.get("objectList");
            Vector visualCue = new Vector();
            
            for(int i = 0; i < objectList.size(); i++)
            {
                visualCue.add("");
            }
        return visualCue;
        }
              
        
        /**
         * This method displays Action Icons column.
         * @param context the eMatrix Context object
         * @param args holds the following input arguments:
         *        paramMap a HashMap
         *        requestMap a HashMap
         * @return Vector which holds following parameter
         *        html code to display icons
         * @throws Exception if the operation fails
         * @since PRC BX3
         */
        public Vector displayActionIcons(Context context, String args[]) throws Exception
        {
            HashMap parametersMap = (HashMap)JPO.unpackArgs(args);
            MapList objectList = (MapList)parametersMap.get("objectList");
            StringBuffer stbNameRev = new StringBuffer();
            Vector vNameRev = new Vector();
            String strObjectId = "";
            
            boolean displayGenerateIcon = false;
            for(int i = 0; i < objectList.size(); i++)
            {
                boolean isQuantityInvalid = false;
                stbNameRev.delete(0, stbNameRev.length());
                Map objectMap = (Map)objectList.get(i);
                strObjectId = (String)objectMap.get("id");
                String strObjectLevel = (String)objectMap.get("id[level]");
                //String strFeatureListId = (String)objectMap.get("featureListId");
                if(!(strObjectLevel.equalsIgnoreCase("0"))){
                //HashMap evaluatedParts = (HashMap)objectMap.get("evaluatedParts");
                String parentId = (String)objectMap.get("id[parent]");
                // code to check part family
                DomainObject domProductObject = new DomainObject(strObjectId);
                
                List gbomList = new MapList();
                StringList selectable = new StringList(DomainConstants.SELECT_ID);
               // selectable.addElement("from["+ConfigurationConstants.RELATIONSHIP_GBOM+"].to.id");
                
                StringBuffer stbWhereClause = new StringBuffer();
                stbWhereClause.append("from["+ConfigurationConstants.RELATIONSHIP_GBOM+"].to.type=='"+ProductLineConstants.TYPE_PART_FAMILY+"'");
                
                gbomList = domProductObject.getRelatedObjects(context,
                												ConfigurationConstants.RELATIONSHIP_GBOM,
                                                                "",
                                                                false,
                                                                true,
                                                                1,
                                                                selectable,
                                                                null,
                                                                stbWhereClause.toString(),
                                                                null,
                                                                null,
                                                                null,
                                                                null);

                
                //get list of parts for which part inclusion rule is evaluated to true
                try{
    
                
                        stbNameRev =  stbNameRev.append("<img src=\"../common/images/iconActionCreateNewPart.gif")
                                                .append("\" border=\"0\"  align=\"middle\" ")
                                                .append("TITLE=\"")
                                                .append("Create New")
                                                .append("\"")
                                                .append(" onclick=\"javascript:showDialog('../configuration/ClonePartUtil.jsp?featureId="+strObjectId+"&amp;level="+strObjectLevel+"&amp;isQuantityInvalid="+isQuantityInvalid+"&amp;displayGenerateIcon="+displayGenerateIcon+"&amp;mode=createpart"+"&amp;duplicate=false"+"&amp;parentId="+parentId+"&amp;generate=false"+"');\"")
                                                .append("/>");
                        stbNameRev = stbNameRev.append(" <img src=\"../common/images/iconActionAddExistingPart.gif")
                                                .append("\" border=\"0\"  align=\"middle\" ")
                                                .append("TITLE=\"")
                                                .append("Add Existing")
                                                .append("\"")
                                                .append(" onclick=\"javascript:showDialog('../configuration/ClonePartUtil.jsp?featureId="+strObjectId+"&amp;level="+strObjectLevel+"&amp;isQuantityInvalid="+isQuantityInvalid+"&amp;displayGenerateIcon="+displayGenerateIcon+"&amp;mode=addexisting"+"&amp;duplicate=false"+"&amp;parentId="+parentId+"&amp;generate=false"+"');\"")
                                                .append("/>");
                       if(gbomList.size()==1){
                           String strPartFamily = (String) ((Map)gbomList.get(0)).get(DomainConstants.SELECT_ID);
                           DomainObject objPartFamily = new DomainObject(strPartFamily);
                           String strPartFamilyState = objPartFamily.getInfo(context,ProductLineConstants.SELECT_CURRENT);
                           if(!("obsolete").equalsIgnoreCase(strPartFamilyState)){
                               stbNameRev = stbNameRev.append(" <img src=\"../common/images/iconActionGenerateFromPartFamily.gif")
                                                   .append("\" border=\"0\"  align=\"middle\" ")
                                                   .append("TITLE=\"")
                                                   .append("Generate")
                                                   .append("\"")
                                                   .append(" onclick=\"javascript:showDialog('../configuration/ClonePartUtil.jsp?featureId="+strObjectId+"&amp;level="+strObjectLevel+"&amp;isQuantityInvalid="+isQuantityInvalid+"&amp;displayGenerateIcon="+displayGenerateIcon+"&amp;mode=generate"+"&amp;duplicate=false"+"&amp;parentId="+parentId+"&amp;generate=true"+"');\"")
                                                   .append("/>");
                               }
                       }
                }
                catch(NullPointerException e){
                    e.printStackTrace();
                }
                }
                vNameRev.add(stbNameRev.toString());
        }
        return vNameRev;
        }


       

/**
* gets selected technical features
*
* @param context the eMatrix Context object
* @param args holds the following input arguments:
* @param SelectedFeature holds the selected features String from the calling method
* @return MapList 
* @throws Exception if the operation fails
* @since R212
  */
@com.matrixone.apps.framework.ui.ProgramCallable
public MapList getLogicalFeatureStructureForSelectFeatures(Context context, String[] args)
throws Exception {
   MapList mapLogicalStructure =null;
	int limit = 32767; //Integer.parseInt(FrameworkProperties.getProperty(context,"emxConfiguration.Search.QueryLimit"));
	
	try {
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		//Gets the objectids and the relation names from args
		String strObjectid = (String)programMap.get("objectId");
		//String strSelFeatureIds = (String)programMap.get("selectedObjectIds");
		String strSelFeatureIds = (String)programMap.get("SelectedFeature");
		
		int iLevel = ConfigurationUtil.getLevelfromSB(context,args);
		String filterExpression = (String) programMap.get("CFFExpressionFilterInput_OID");
		
		
		
		    StringBuffer strMatchList = new StringBuffer();
            strMatchList.append("\"");
            if (strSelFeatureIds != null && !strSelFeatureIds.equals("")) {
            	int start=0;
	            StringTokenizer FIdTokenizer = new StringTokenizer(
	                    strSelFeatureIds, "-");	            
	            while (FIdTokenizer.hasMoreTokens()){ 
	            	if (start!=0){
	                    strMatchList.append(",");
		             }
	            	 strMatchList.append((String) FIdTokenizer.nextToken());
	            	 start++;
	            }
                 
                  
            }
            strMatchList.append("\"");
		 StringBuffer sbWhereClause = new StringBuffer(200);
         sbWhereClause
                 .append("(id matchlist("
                         + strMatchList.toString() + ")\",\")");
		
		LogicalFeature cfBean = new LogicalFeature(strObjectid);
		mapLogicalStructure= (MapList)cfBean.getLogicalFeatureStructure(context,"", null, null, null, false,
			true,iLevel,limit, sbWhereClause.toString(), DomainObject.EMPTY_STRING, DomainObject.FILTER_STR_AND_ITEM,filterExpression);
		
		
		

		 if (mapLogicalStructure != null) {
             HashMap hmTemp = new HashMap();
             hmTemp.put("expandMultiLevelsJPO", "true");
             mapLogicalStructure.add(hmTemp);
         }
	}catch (Exception e) {
		// TODO: handle exception
	}
	return mapLogicalStructure;
}

}
