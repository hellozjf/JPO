/*   emxLicenseBase
**
**   Copyright (c) 2003-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the implementation of Administrator's Licensing
**
*/
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkLicenseUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.User;
import matrix.db.UserItr;
import matrix.util.LicenseUtil;
import matrix.util.MatrixException;
import matrix.util.StringList;

/**
 * @author ZWE
 *
 * The <code>emxLicenseBase</code> class/interface contains ...
 *
 * @version AEF 11.0.0.0 - Copyright (c) 2005, MatrixOne, Inc.
 */
public class emxLicenseBase_mxJPO extends emxDomainObject_mxJPO{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     */
    String strLicenseServerName = null;
    public emxLicenseBase_mxJPO(Context context, String[] args) throws Exception
    {
        super(context, args);
    }

    /**
     * Column program to show count of available licenses  called from Assign License by Product
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding Count of Available Licenses .
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public Vector getAvailableLicenses(Context context,String[] args) throws Exception
    {
        try
        {
            //  To get the Object id List
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            Vector columnVals = new Vector(objList.size());
            // availbleLicense = null;
            //Iteration through the Maplist of objectlist to retrieve the level of objects connected to context part by Derived relationship
            Iterator i = objList.iterator();
            while (i.hasNext())
            {

                Map m = (Map) i.next();
                String appsName = (String)m.get("name");
                String strObjectType = (String)m.get("objectType");
                String strTotalLicCnt  = (String)m.get(LicenseUtil.INFO_TOTAL_COUNT);
                if(strObjectType!=null && strObjectType.equalsIgnoreCase("product"))
                {
                    // Pass tthe name of Variable "appsName" to following method
                    columnVals.addElement(strTotalLicCnt);
                }else
                {
                    columnVals.addElement("");}
            }

            return columnVals;
        }
        catch(Exception e )
        {
            throw new Exception(e.toString());
        }
    }

    /**
     * Column program to show count of assigned  licenses  called from Assign License by Product
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding Count of assigned Licenses .
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public Vector getAssignedLicenses (Context context,String[] args) throws Exception
    {
        try
        {
            //  To get the Object id List
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            HashMap paramList = (HashMap) programMap.get("paramList");
            String exportFormat = (String)paramList.get("exportFormat");
            String reportFormat = (String)paramList.get("reportFormat");
            Vector columnVals = new Vector(objList.size());

            //Iteration through the Maplist of objectlist to retrieve the level of objects connected to context part by Derived relationship
            Iterator i = objList.iterator();
            while (i.hasNext())
            {
                Map m = (Map) i.next();
                String level = (String)m.get("level");
                String strLicName = (String)m.get("name");
                String strObjectType = (String)m.get("objectType");

                //if(strObjectType!=null && strObjectType.equalsIgnoreCase("product"))
                if(strObjectType!=null && !"".equals(strObjectType))
                {
                    // Pass the name of Person  to following method
                    String strAssignedLicenses = (String)m.get("Total License Assigned");
                    if("CSV".equals(exportFormat) || "HTML".equals(reportFormat)){
                      	 columnVals.addElement(XSSUtil.encodeForHTML(context,strAssignedLicenses));
                      }else{
                    columnVals.addElement("<a>"+XSSUtil.encodeForHTML(context,strAssignedLicenses)+"</a>");
                }
                }
                else
                {
                    columnVals.addElement("");}
            }
            return columnVals;
        }
        catch(Exception e )
        {
        throw new Exception(e.toString());
        }
    }

    /**
     * Column program to show count of Consumed   licenses by persons  called from Assign License by Product
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding Count of Consumed Licenses .
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public Vector getConsumedLicenses(Context context,String[] args) throws Exception
    {
        try
        {
            //  To get the Object id List
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            HashMap paramList = (HashMap) programMap.get("paramList");
            String exportFormat = (String)paramList.get("exportFormat");
            String reportFormat = (String)paramList.get("reportFormat");
            Vector columnVals = new Vector(objList.size());
            int cnt=0;
            //Iteration through the Maplist of objectlist to retrieve the level of objects connected to context part by Derived relationship
            Iterator i = objList.iterator();
            while (i.hasNext())
            {
                Map m = (Map) i.next();
                String level = (String)m.get("level");
                String strObjectType = (String)m.get("objectType");

                //if(strObjectType!=null && strObjectType.equalsIgnoreCase("product"))
                if(strObjectType!=null && !"".equals(strObjectType))
                {
                    String strInUseCount = (String)m.get(LicenseUtil.INFO_IN_USE_COUNT);
                    if("CSV".equals(exportFormat) || "HTML".equals(reportFormat)){
                   	 columnVals.addElement(XSSUtil.encodeForHTML(context, strInUseCount));
                   }else{
                    columnVals.addElement("<a>"+XSSUtil.encodeForHTML(context, strInUseCount)+"</a>");
                   }

                }else
                {
                    columnVals.addElement("");
                }
            }
            return columnVals;
        }
        catch(Exception e )
        {
            throw new Exception(e.toString());
        }
    }

    /**
     * Column program to get current state of person objects   licenses by persons  called from Assign License by Product
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding current state of person  .
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public Vector getState(Context context,String[] args) throws Exception
    {
        try
        {
            //  To get the Object id List
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            Vector columnVals = new Vector(objList.size());
            int cnt=0;
            //Iteration through the Maplist of objectlist to retrieve the level of objects connected to context part by Derived relationship
            Iterator i = objList.iterator();
            while (i.hasNext())
            {
                Map m = (Map) i.next();
                String level = (String)m.get("level");
                String name = (String)m.get("name");
                String id = (String)m.get("id");
                String objectType=(String)m.get("objectType");
                String personType = (String) m.get("PersonType");
                if(objectType!=null && objectType.equalsIgnoreCase("person") && !"Non-DB".equals(personType))
                {
                    DomainObject domPerson = new DomainObject(id);
                    Person peron = new Person(id);
                    String strState =  domPerson.getInfo(context,DomainConstants.SELECT_CURRENT);
                    columnVals.addElement(strState.toString());
                }
                else
                {
                    columnVals.addElement("");
                }

            }
            return columnVals;
        }
        catch(Exception e )
        {
            throw new Exception(e.toString());
        }
    }

    /**
     * Column program to get email Address  of person objects   licenses by persons  called from Assign License by Product
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding email Address of person  .
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public Vector getEmailAddress(Context context,String[] args) throws Exception
    {
        try
        {
            //To get the Object id List
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            StringBuffer sb = new StringBuffer();
            Vector columnVals = new Vector(objList.size());
            String emailId = PropertyUtil.getSchemaProperty(context,"attribute_EmailAddress");
            HashMap paramList = (HashMap) programMap.get("paramList");
            String exportFormat = (String)paramList.get("exportFormat");
            String reportFormat = (String)paramList.get("reportFormat");
            int cnt=0;
            String[] iLicenseserver = new String[5];
            //Iteration through the Maplist of objectlist to retrieve the level of objects connected to context part by Derived relationship
            Iterator i = objList.iterator();
            while (i.hasNext())
            {
                Map m = (Map) i.next();
                String level = (String)m.get("level");
                String name = (String)m.get("name");
                String id = (String)m.get("id");
                String personType = (String) m.get("PersonType");
                if(id.indexOf(".") != -1 && !"Non-DB".equals(personType))
                {
                    DomainObject domPerson = new DomainObject(id);
                    String emailAddressAttribute  =  domPerson.getAttributeValue(context,emailId);
                    if("CSV".equals(exportFormat) || "HTML".equals(reportFormat))
                    {
                   	 columnVals.addElement(emailAddressAttribute);
                    }
					else
                    {
                    String mailTO = "<a href=\"mailto:"+emailAddressAttribute+"\">"+XSSUtil.encodeForHTML(context,emailAddressAttribute)+"</a>";
                    columnVals.addElement(mailTO.toString());
                }
                  }
                else
                {
                    columnVals.addElement("");
                }

            }
            return columnVals;
        }catch(Exception e )
        {
            throw new Exception(e.toString());
        }
    }

    /**
    * Expand  program to get list of products in the license server and person connected to it
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds  arguments.
    * @return Vector holding product and perons names  .
    * @throws Exception if the operation fails.
    * @since R207.
    */
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getListOfLicenseServerProducts(Context context,String[] args) throws Exception
    {
        MapList prctMlist = new MapList();
        String strLang = context.getSession().getLanguage();
        try
        {
            //searchFramesetObject
            String    mqlCommand =null;
            StringList objectSelects =  new StringList();
            objectSelects.add(DomainConstants.SELECT_NAME);
            objectSelects.add(DomainConstants.SELECT_ID);
            String whereExpression = "";
            String userName = "";


            //To get the Object id List
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String parentId = (String) programMap.get("parentId");
            String objectId=(String)programMap.get("objectId");
           
            //objectId="MCC#CL";
            if(UIUtil.isNotNullAndNotEmpty(objectId))
            {
                DomainObject dObj = new DomainObject(objectId);
                 if(objectId.indexOf(".")== -1 && !dObj.exists(context) && "".equals(parentId))
                 {
                	StringList productInfo =  FrameworkUtil.split(objectId, "#");
 	            	String productName = (String) productInfo.get(0);
 	            	String productType = (String) productInfo.get(1);
 	            	boolean forCasualLicenses = FrameworkLicenseUtil.CASUAL_LICENSE.equals(productType); 	            	
 	            	objectId = productName;
                    StringList strlist = getConsumedUserList(context,objectId,strLicenseServerName, forCasualLicenses);
                    String title = getTitleValue(context,objectId,strLang);
                    //Added to check whether the product is defined in Kernel or not
                    if(!"".equals(title)){ 
                    	String strMQL = "";
                    	mqlCommand = "print product $1 select $2 dump $3";
	                	if(forCasualLicenses){	                		
	 	            		strMQL =  MqlUtil.mqlCommand(context, mqlCommand, true, productName, "casualhour[40].person", "|");
	 	            	}else {	 	            		
	 	            		strMQL =  MqlUtil.mqlCommand(context,mqlCommand, true, productName, "person", "|");
	 	            	}	                      
                     StringList strlLocalAssignedPersons = FrameworkUtil.split(strMQL,"|");
                     MapList personMList = null;
                     //Added to get the list of users who are not from local DB but consumed the licenses in license server
                     for(int i=0; i<strlist.size(); i++)
                     {
                        userName = (String) strlist.get(i);
                        Map m = new HashMap();
                        whereExpression = "name =='"+userName+"'";
                        personMList = DomainObject.findObjects(context,DomainConstants.TYPE_PERSON,"*",whereExpression,objectSelects);
//                      Added if condition to check whether the person is Application user of local DB
                        if(personMList != null && personMList.size() >0)
                        {
                            m=(Map)personMList.get(0);
                            m.put("Total License Assigned","No");
                            if(strlLocalAssignedPersons.contains(userName))
                            {
                                m.put("Total License Assigned","Yes");
                                strlLocalAssignedPersons.remove(userName);
                            }
                            else
                                m.put("hasShowColor","true");//Need to show the color with value Yes
                        }
                        else
                        {
                            m.put("id",userName);
                            m.put("name",userName);
                            m.put("Total License Assigned","No");
                            m.put("PersonType","Non-DB");
                        }
                        m.put("objectType","person");
                        m.put(LicenseUtil.INFO_IN_USE_COUNT,"Yes");
	                        // we dont need to list a casual person under product,if license is removed from that product
	                        if(!forCasualLicenses || "Yes".equals((String)m.get("Total License Assigned"))){
                        prctMlist.add(m);
                     }
                     }
                     //Added to get the list of local DB Users
                     for(int j=0; j<strlLocalAssignedPersons.size(); j++)
                     {
                        Map m = new HashMap();
                        userName = (String) strlLocalAssignedPersons.get(j);
                        whereExpression = "name =='"+userName+"'";
                        personMList = DomainObject.findObjects(context,DomainConstants.TYPE_PERSON,"*",whereExpression,objectSelects);
                        if(personMList != null && personMList.size() > 0)
                        {
                            m = (Map) personMList.get(0);
                        }
                        else
                        {
                            m.put("id",userName);
                            m.put("name",userName);
                            m.put("PersonType","Non-DB");
                        }
                        m.put("objectType","person");
                        m.put("Total License Assigned","Yes");
                        m.put(LicenseUtil.INFO_IN_USE_COUNT,"No");
                        prctMlist.add(m);

                     }

                     }// end loop of title
                 }// End if loop of person validation
            }else{
                List info = LicenseUtil.getLicenseInfo(context,null);
                String strTotalLicensesAssigned = "";
                Map prodtMap = null;
                for( int k=0, len=info.size(); k<len; k++ ) {
                    prodtMap = new HashMap();
                    strTotalLicensesAssigned = "";
                    HashMap rowmap = (HashMap)info.get(k);
                    Integer licCasualHours = (Integer)rowmap.get(LicenseUtil.INFO_CASUAL_HOUR);
                    String licTrigram = (String)   rowmap.get(LicenseUtil.INFO_LICENSE_NAME);
                    String title = getTitleValue(context,licTrigram,strLang);
                    if("".equals(title))
                    {
                        strTotalLicensesAssigned = "";
                        prodtMap.put("disableSelection","true");
                    }else{
                        String strCommand = "print product $1 select $2 dump $3";
                    	String strlistOfAssignees = "";
                    	if(licCasualHours > 0){                    		
                    		strlistOfAssignees = MqlUtil.mqlCommand(context, strCommand, true, licTrigram, "casualhour["+licCasualHours+"].person", "|");
                    	}else{                    		
                    		strlistOfAssignees = MqlUtil.mqlCommand(context, strCommand, true, licTrigram, "person", "|");
                    }
                        StringList slAssignees = null;
                        if (strlistOfAssignees != null && !"".equals(strlistOfAssignees)){
                            slAssignees = FrameworkUtil.split(strlistOfAssignees, "|");
                            if (slAssignees.size() != 0){
                                strTotalLicensesAssigned = String.valueOf(slAssignees.size());
                            }
                        }
                    }

                    prodtMap.put("name",licTrigram);
                    prodtMap.put("id",licTrigram + (licCasualHours>0?"#CL":"#FL"));
                    prodtMap.put("objectType","product");
                    Integer licTotalCount =  (Integer)  rowmap.get(LicenseUtil.INFO_TOTAL_COUNT) ;
                    Integer licInUseCount =(Integer)   rowmap.get(LicenseUtil.INFO_IN_USE_COUNT);
                    prodtMap.put(LicenseUtil.INFO_TOTAL_COUNT,licTotalCount.toString());
                    prodtMap.put(LicenseUtil.INFO_IN_USE_COUNT,licInUseCount.toString());
                    prodtMap.put(LicenseUtil.INFO_CASUAL_HOUR, licCasualHours.toString());
                    prodtMap.put("Total License Assigned", strTotalLicensesAssigned);
                    prctMlist.add(prodtMap);
                }
            }

        }catch(Exception e )
        {
            e.printStackTrace();
            if(e.toString().indexOf("License error") != -1)
            {
            	Locale strLocale = new Locale(strLang);
                throw new MatrixException(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",strLocale, "emxComponents.Common.LicenseError").toString());
            }
            else
                throw new Exception(e.toString());
        }
        prctMlist.sort("name","ascending","string");
        return prctMlist;
    }

    /**
     * Expand  program to get list of products in database and person connected to it.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding product and perons names  .
     * @throws Exception if the operation fails.
     * @since R207.
     */
	@com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getListOfDatabaseProducts(Context context,String[] args) throws Exception
    {
        MapList prctMlist = new MapList();
        String strLang = context.getSession().getLanguage();
        try{
            String    mqlCommand =null;
            String userName = "";
            String whereExpression = "";
            StringList objectSelects =  new StringList();
            objectSelects.add(DomainConstants.SELECT_NAME);
            objectSelects.add(DomainConstants.SELECT_ID);


            //To get the Object id List
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String objectId=(String)programMap.get("objectId");
            String parentId = (String) programMap.get("parentId");
            boolean findPerson =false;
            //objectId="MCC#CL";
            if(objectId!=null && !"".equalsIgnoreCase(objectId))
            {
                DomainObject dObj = new DomainObject(objectId);
                if(objectId.indexOf(".")== -1 && !dObj.exists(context) && "".equals(parentId))
                {
                	StringList productInfo =  FrameworkUtil.split(objectId, "#");
 	            	String productName = (String) productInfo.get(0);
 	            	String productType = (String) productInfo.get(1);
 	            	boolean forCasualLicenses = FrameworkLicenseUtil.CASUAL_LICENSE.equals(productType); 	            	
 	            	objectId = productName; 	            	
                    List info = LicenseUtil.getLicenseInfo(context,null);
                    StringList strlConsumedPersonList = new StringList();
                    for(int k=0; k<info.size(); k++)
                    {
                        HashMap rowmap = (HashMap) info.get(k);
                        String licTrigram = (String)   rowmap.get(LicenseUtil.INFO_LICENSE_NAME);
                        if(objectId.equals(licTrigram))
                        {
                            strlConsumedPersonList = getConsumedUserList(context,objectId,strLicenseServerName,forCasualLicenses);
                        }
                    }
                    String strMQL = "";
                    mqlCommand = "print product $1 select $2 dump $3";
                    if(forCasualLicenses){                		
 	            		strMQL =  MqlUtil.mqlCommand(context, mqlCommand, true, productName, "casualhour[40].person", "|");
 	            	}else { 	            		
 	            		strMQL =  MqlUtil.mqlCommand(context,mqlCommand, true, productName, "person", "|");
                    }
                    StringList strlLocalAssignedPersons = FrameworkUtil.split(strMQL,"|");
                    MapList personMList = null;
                    for(int i=0; i<strlConsumedPersonList.size(); i++)
                    {
                       userName = (String) strlConsumedPersonList.get(i);
                       Map m = new HashMap();
                       whereExpression = "name =='"+userName+"'";
                       personMList = DomainObject.findObjects(context,DomainConstants.TYPE_PERSON,"*",whereExpression,objectSelects);
                       //Added if condition to check whether the person is Application user of local DB
                       if(personMList != null && personMList.size() >0)
                       {
                           m=(Map)personMList.get(0);
                           m.put("Total License Assigned","No");
                           if(strlLocalAssignedPersons.contains(userName))
                           {
                               strlLocalAssignedPersons.remove(userName);
                               m.put("Total License Assigned","Yes");
                           }
                           else
                               m.put("hasShowColor","true");//Need to show the color with value Yes

                       }
                       else
                       {
                           m.put("id",userName);
                           m.put("name",userName);
                           m.put("Total License Assigned","No");
                           m.put("PersonType","Non-DB");
                       }
                       m.put("objectType","person");
                       m.put(LicenseUtil.INFO_IN_USE_COUNT,"Yes");
	                       if(!forCasualLicenses || "Yes".equals((String)m.get("Total License Assigned"))){
                       prctMlist.add(m);
	                       }
                    }
                    for(int j=0; j<strlLocalAssignedPersons.size(); j++)
                    {
                       Map m = new HashMap();
                       userName = (String) strlLocalAssignedPersons.get(j);
                       whereExpression = "name =='"+userName+"'";
                       personMList = DomainObject.findObjects(context,DomainConstants.TYPE_PERSON,"*",whereExpression,objectSelects);
                       if(personMList != null && personMList.size() >0)
                       {
                           m = (Map) personMList.get(0);
                       }
                       else
                       {
                           m.put("id",userName);
                           m.put("name",userName);
                           m.put("PersonType","Non-DB");
                       }
                       m.put("objectType","person");
                       m.put("Total License Assigned","Yes");
                       //Added to display No for consumed column if the product has licenses on license server else display nothing.(371663)
                       if(strlConsumedPersonList.size() > 0)
                           m.put(LicenseUtil.INFO_IN_USE_COUNT,"No");
                       prctMlist.add(m);

                    }

                 }
            }
            else
            {

                mqlCommand=  MqlUtil.mqlCommand(context,"list product $1 where $2", "*", "!technical");
                String strProductName = "";
                StringList strlProducts = FrameworkUtil.split(mqlCommand,"\n");
                List info = LicenseUtil.getLicenseInfo(context,null);
                String strTotalLicensesAssigned = "";

                for(int m=0; m<strlProducts.size(); m++)
                {
                    strProductName = (String) strlProducts.get(m);
                    Map prodtMap = null;
                    boolean isValidProduct = false;
                    for( int k=0, len=info.size(); k<len; k++ )
                    {
                    	prodtMap = new HashMap();
                        strTotalLicensesAssigned = "";
                        HashMap rowmap = (HashMap)info.get(k);
                        String licTrigram = (String)   rowmap.get(LicenseUtil.INFO_LICENSE_NAME);

                        if(strProductName.equals(licTrigram))
                        {
                            Integer licTotalCount =  (Integer)  rowmap.get(LicenseUtil.INFO_TOTAL_COUNT) ;
                            Integer licInUseCount =(Integer)   rowmap.get(LicenseUtil.INFO_IN_USE_COUNT);
                            Integer licCasualHours = (Integer)rowmap.get(LicenseUtil.INFO_CASUAL_HOUR);
                            prodtMap.put(LicenseUtil.INFO_TOTAL_COUNT,licTotalCount.toString());
                            prodtMap.put(LicenseUtil.INFO_IN_USE_COUNT,licInUseCount.toString());
                            prodtMap.put(LicenseUtil.INFO_CASUAL_HOUR,licCasualHours.toString());
                            
                            StringList slAssignees = null;
                            String strlistOfAssignees = "";
                            String strCommand = "print product $1 select $2 dump $3";
                            if(licCasualHours > 0){                            	
                            	strlistOfAssignees = MqlUtil.mqlCommand(context, strCommand, true, strProductName.trim(), "casualhour[40].person", "|");
                            }else{                            	
                            	strlistOfAssignees = MqlUtil.mqlCommand(context, strCommand, true, strProductName.trim(), "person", "|");
                            }

                            if (strlistOfAssignees != null && !"".equals(strlistOfAssignees)){
                                slAssignees = FrameworkUtil.split(strlistOfAssignees, ",");
                                if (slAssignees.size() != 0){
                                    strTotalLicensesAssigned = String.valueOf(slAssignees.size());
                                }
                            }
                            prodtMap.put("name",strProductName);
                            prodtMap.put("id",strProductName + (licCasualHours>0?"#CL":"#FL"));
                            prodtMap.put("objectType","product");
                            prodtMap.put("Total License Assigned", strTotalLicensesAssigned);
                            prctMlist.add(prodtMap);
                            isValidProduct = true;

                        }
                    }
                    if(!isValidProduct){
                    	prodtMap = new HashMap();
                    StringList slAssignees = null;
                    String strCommand = "print product $1 select $2 dump";
                    String strlistOfAssignees = MqlUtil.mqlCommand(context, strCommand, strProductName.trim(), "person");
                    if (strlistOfAssignees != null && !"".equals(strlistOfAssignees)){
                        slAssignees = FrameworkUtil.split(strlistOfAssignees, ",");
                        if (slAssignees.size() != 0){
                            strTotalLicensesAssigned = String.valueOf(slAssignees.size());
                        }
                    }
                    prodtMap.put("name",strProductName);
                    prodtMap.put("id",strProductName);
                    prodtMap.put("objectType","product");
                    prodtMap.put("Total License Assigned", strTotalLicensesAssigned);
                    prctMlist.add(prodtMap);
                    }

                }
            }

        }catch(Exception e )
        {
            e.printStackTrace();
            if(e.toString().indexOf("License error") != -1)
            {
            	Locale strLocale = new Locale(strLang);
                throw new MatrixException(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",strLocale, "emxComponents.Common.LicenseError").toString());             
            }
            else
                throw new Exception(e.toString());
        }
		prctMlist.sort("name","ascending","string");
        return prctMlist;
    }

    /**
     * Column program to get Name of products   called from Assign License by Product
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding name of product .
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public Vector getProductName(Context context,String[] args) throws Exception
    {
        try
        {
            //  To get the Object id List
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            Vector columnVals = new Vector(objList.size());
            HashMap paramList = (HashMap) programMap.get("paramList");
            String exportFormat = (String)paramList.get("exportFormat");
            String reportFormat = (String)paramList.get("reportFormat");
            int cnt=0;

            //Iteration through the Maplist of objectlist to retrieve the level of objects connected to context part by Derived relationship
            Iterator i = objList.iterator();
            String strType="";
            String strLang = context.getSession().getLanguage();
            String title = "";
            while (i.hasNext())
            {
                Map m = (Map) i.next();
                String name = (String)m.get("name");
                String strId = (String)m.get("id");
                String level=(String)m.get("level");
                String strObjectType=(String)m.get("objectType");
                String strPersonType = (String)m.get("PersonType");
                if(!level.equalsIgnoreCase("3"))
                {
                    if(name!=null && name.length() > 0 && strObjectType.equalsIgnoreCase("product"))
                    {
                        title = getTitleValue(context,name, strLang);
                        if("".equals(title))
                            title = name;
                        //START BUG 366255
                        if (title.indexOf("&") != -1){
                            title = FrameworkUtil.findAndReplace(title, "&", "&amp;");
                        }
                        //END BUG 366255
                        if("CSV".equals(exportFormat) || "HTML".equals(reportFormat)){
                        	 columnVals.addElement(title);
                        }else{
                        String strImage = "<img src=\"../common/images/iconSmallCommonLicensingApp.gif\" border=\"0\"></img>"+title;
                        columnVals.addElement("<a>"+strImage+"</a>");
                    }
                    }
                    else
                    {
                        StringBuffer sbEditIcon = new StringBuffer();
                        if(strId != null && !"Non-DB".equals(strPersonType))
                        {
                        sbEditIcon.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?");
                        sbEditIcon.append("objectId=" + strId+"','800', '600', 'false', 'popup', '')\">");
                        sbEditIcon.append("<img src=\"../common/images/iconSmallPerson.gif\" border=\"0\"></img></a>");
                        sbEditIcon.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?");
                        sbEditIcon.append("objectId=" + strId+"','800', '600', 'false', 'popup', '')\">");
                        sbEditIcon.append(XSSUtil.encodeForHTML(context,name)+"</a>");
                        }
                        else
                        {
                            sbEditIcon.append("<a><img src=\"../common/images/iconSmallPerson.gif\" border=\"0\"></img>");
                            sbEditIcon.append(XSSUtil.encodeForHTML(context,name));
                            sbEditIcon.append("</a>");
                        }
                        if("CSV".equals(exportFormat) || "HTML".equals(reportFormat))
                        {
                       	 columnVals.addElement(name);
                        }
                        else
                        {
                        columnVals.addElement(sbEditIcon.toString());
                    }
                     }
                }else{
                    columnVals.addElement("");
                    return columnVals;
                }
            }
            return columnVals;
        }
        catch(Exception e )
        {
            throw new Exception(e.toString());
        }
    }

    /**
     * Column program to total count of roles assign to person objects   called from Assign License by Product
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding count of total roles
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public Vector getRolesCount(Context context,String[] args) throws Exception
    {
        try
        {
            //  To get the Object id List
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objList = (MapList)programMap.get("objectList");
            Vector columnVals = new Vector(objList.size());
            StringList obselct = new StringList();
            obselct.add(DomainConstants.SELECT_ID);
            obselct.add(DomainConstants.SELECT_NAME);
            StringList relselct = new StringList();
            relselct.add(DomainConstants.SELECT_ID);
            relselct.add(DomainConstants.SELECT_NAME);
                //Iteration through the Maplist of objectlist to retrieve the level of objects connected to context part by Derived relationship
            Iterator i = objList.iterator();
            while (i.hasNext())
            {
                int count = 0;
                Map m = (Map) i.next();
                String obiD= (String)m.get("id");
                String strName = (String)m.get("name");

                String strObjectType=(String)m.get("objectType");

                String orgName = null;

                if(strObjectType!=null && strObjectType.equalsIgnoreCase("person"))
                {
                    matrix.db.Person mxDbPerson = new matrix.db.Person(strName);
                    mxDbPerson.open(context);

                    MapList roleMapList = new MapList();
                    MQLCommand mqlCmd = new MQLCommand();
                    mqlCmd.open(context);
                    UserItr userItr = new UserItr(mxDbPerson.getAssignments(context));
                    while(userItr.next()) {
                        User userObj = userItr.obj();
                        if(userObj instanceof matrix.db.Role)
                        {
                            String sUserName = userItr.obj().getName();
                            count++;
                        }
                    }
                    Integer  intCount = new Integer(count);
                    StringBuffer sbEditIcon = new StringBuffer();
                    sbEditIcon.append("<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?");
                    sbEditIcon.append("objectId=" + obiD+"&amp;DefaultCategory=Role','800', '600', 'false', 'popup', '')\">");
                    sbEditIcon.append(intCount.toString()+"</a>");
                    columnVals.addElement(sbEditIcon.toString());
                }
                else
                {
                    columnVals.addElement("");
                }
            }
            return columnVals;
        }
        catch(Exception e )
        {
            throw new Exception(e.toString());
        }
    }

    /**
     * Style function to show cell in different color if it exceeds value    called from Assign License by Product
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding name of style class
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public StringList getStyleForOverAllocation(Context context, String[]args)throws Exception {
        try {
            StringList slStyles = new StringList();
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            int iTotalLicenseAssigned = 0;
            int licenseAvaliable = 0;

            Iterator objectItr = objectList.iterator();
            while(objectItr.hasNext()){
                Map curObjectMap = (Map) objectItr.next();
                String strObjectType = (String)curObjectMap.get("objectType");
                //Added for Special Case
                String strShowColor = (String)curObjectMap.get("hasShowColor");
                if(strObjectType!=null && strObjectType.equalsIgnoreCase("product"))
                {
                    String strLicenseAssigned = (String)curObjectMap.get("Total License Assigned");
                    String strLicenseAvaliable = (String)curObjectMap.get(LicenseUtil.INFO_TOTAL_COUNT);

                    if (strLicenseAssigned == null || "".equals(strLicenseAssigned)){
                        strLicenseAssigned = "0";
                    }
                    if (strLicenseAvaliable == null || "".equals(strLicenseAvaliable)){
                        strLicenseAvaliable = "0";
                    }
                    iTotalLicenseAssigned = Integer.parseInt(strLicenseAssigned);
                    licenseAvaliable = Integer.parseInt(strLicenseAvaliable);
                    if (iTotalLicenseAssigned > licenseAvaliable){
                            slStyles.addElement("license-OverAllocation");
                    }
                    else {
                        slStyles.addElement("");
                    }
                }
                else
                {
                    slStyles.addElement("");
                }
            }
            return slStyles ;
        }
        catch (Exception exp){
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Expand program which extracts all the licenses and its avaliability, consumability etc required for the column programs.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Maplist holding all the information of the license.
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public  MapList getUserLicense (Context context, String[] args) throws Exception
    {
        String strLang = context.getSession().getLanguage();
      try {
            //To hold the table data
            MapList mlTableData = new MapList();
            java.util.List info = null;
            DomainObject dObj = null;

            //Get object list information from packed arguments
            Map programMap = (Map) JPO.unpackArgs(args);
            String strObjectId = (String)programMap.get("objectId");

            if(strObjectId != null && !"".equals(strObjectId));
                dObj = new DomainObject(strObjectId);

            if(dObj != null && !dObj.exists(context))
            {
                return mlTableData;
            }
            else
            {
                StringList objectSelects= new StringList();
                String selAttrCasualHours = DomainObject.getAttributeSelect(ATTRIBUTE_LICENSED_HOURS);
        		objectSelects.addElement(dObj.SELECT_NAME);
        		objectSelects.addElement(selAttrCasualHours);
        		Map personInfo = dObj.getInfo(context, objectSelects);
        		String strUserName = (String)personInfo.get(dObj.SELECT_NAME);
        		int iCasualHours = Integer.parseInt((String)personInfo.get(selAttrCasualHours));
        		boolean isCasualUser = (iCasualHours > 0) ;
        		
                // To get assigned products for this user
                String strAssginedProducts = "";
                String strCommand= "list person $1 select $2 dump $3";
                if(isCasualUser){
                	strAssginedProducts = MqlUtil.mqlCommand(context,strCommand, true, strUserName, "casualhour["+ iCasualHours +"].product", "|");                	
                }else{
                	strAssginedProducts = MqlUtil.mqlCommand(context,strCommand, true, strUserName, "product", "|");
                }
                StringList strlProductAssignedList = FrameworkUtil.split(strAssginedProducts,"|");
                info = LicenseUtil.getLicenseInfo(context, null);
                String strLicenseName = "";
                if (info != null)
                {
                    String strlistOfAssignees = null;
                    String strTotalLicensesAssigned = "";
                    StringList slAssignees = null;
                    String strlicenseAssigned = "false";
                    Map map = null;

                    for( int i=0, len=info.size(); i<len; i++ )
                    {
                        map = new HashMap();
                        strlistOfAssignees = null;
                        strTotalLicensesAssigned = "";
                        slAssignees = null;
                        strlicenseAssigned = "false";

                        HashMap rowmap = (HashMap)info.get(i);
                        Integer licCasualHours = (Integer)rowmap.get(LicenseUtil.INFO_CASUAL_HOUR);
                        if( isCasualUser && licCasualHours == 0){
                        	continue;
                        }
                        if(!isCasualUser && licCasualHours > 0){
                        	continue;
                        }                     
                        String strLicenseTrigram = (String)rowmap.get(LicenseUtil.INFO_LICENSE_NAME);
                        Integer LicCount = (Integer)rowmap.get(LicenseUtil.INFO_TOTAL_COUNT);
                        Integer LicInUse = (Integer)rowmap.get(LicenseUtil.INFO_IN_USE_COUNT);
                        String strTotalLicenseCount = LicCount.toString();
                        String strTotalLicenseInUse = LicInUse.toString();
                        String strLicenseExpiryDate = ((Date)rowmap.get(LicenseUtil.INFO_EXPIRE_DATE)).toString();
                        strLicenseName = getTitleValue(context,strLicenseTrigram,strLang);
                        if("".equals(strLicenseName))
                        {
                            strLicenseName = strLicenseTrigram;
                            strTotalLicensesAssigned = "";
                            map.put("RowEditable", "readonly");
                        }
                        else
                        {
                            String strUserCommand = "print product $1 select $2 dump $3";
                            if(licCasualHours > 0){
                            	strlistOfAssignees = MqlUtil.mqlCommand(context, strUserCommand, true, strLicenseTrigram, "casualhour["+licCasualHours+"].person", "|");
                            }else{                            	
                            	strlistOfAssignees = MqlUtil.mqlCommand(context, strUserCommand, true, strLicenseTrigram, "person", "|");
                            }                            
                        }

                        if (strLicenseName.indexOf("&") != -1){
                            strLicenseName = FrameworkUtil.findAndReplace(strLicenseName, "&", "&amp;");
                        }

                        if (strlistOfAssignees != null && !"".equals(strlistOfAssignees)){
                            slAssignees = FrameworkUtil.split(strlistOfAssignees, "|");
                            if (slAssignees.size() != 0){
                                strTotalLicensesAssigned = String.valueOf(slAssignees.size());
                            }
                           //Validate whether is actually assigned from local-DB.
                            if(strlProductAssignedList.contains(strLicenseTrigram))
                            {
                                strlicenseAssigned = "true";
                            }
                        }
                        map.put("License Trigram", strLicenseTrigram);
                        map.put("id", strLicenseTrigram);
                        map.put("License Name", strLicenseName);
                        map.put("License Assigned", strlicenseAssigned);
                        map.put(LicenseUtil.INFO_TOTAL_COUNT, strTotalLicenseCount);
                        map.put(LicenseUtil.INFO_IN_USE_COUNT, strTotalLicenseInUse);
                        map.put(LicenseUtil.INFO_CASUAL_HOUR, licCasualHours);
                        map.put("License Expiry Date", strLicenseExpiryDate);
                        map.put("Total License Assigned", strTotalLicensesAssigned);
                        map.put("objectType","product");
                        mlTableData.add(map);
                    }//End for loop
                }//End if info loop
            }
            return mlTableData;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            if(exp.toString().indexOf("License error") != -1)
            {
            	Locale strLocale = new Locale(strLang);
                throw new MatrixException(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",strLocale, "emxComponents.Common.LicenseError").toString());
            }
            else
                throw new Exception(exp.toString());
        } 
    }

    /**
     * Column program to get names of all the licenses avaliable in the license server.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding the names of licenses.
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public  Vector getNames(Context context, String[] args) throws Exception {
        try {
            Vector vecResult = new Vector();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            HashMap paramList = (HashMap) programMap.get("paramList");
            String exportFormat = (String)paramList.get("exportFormat");
            String reportFormat = (String)paramList.get("reportFormat");
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                Map map = (Map)itrObjects.next();
                String strLicenseName = (String)map.get("License Name");
                String strImage = "<a><img src=\"../common/images/iconSmallCommonLicensingApp.gif\" border=\"0\"></img>" + XSSUtil.encodeForHTML(context, strLicenseName)+"</a>";
                if("CSV".equals(exportFormat) || "HTML".equals(reportFormat)){
                	vecResult.addElement(XSSUtil.encodeForHTML(context, strLicenseName));
               }else{
                vecResult.add(strImage);
               }
            }
            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Column program displaying yes or no depending on whether the license is assigned to user context
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding yes or no for the context user for repective licenses.
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public  Vector getLicensesAssigned(Context context, String[] args) throws Exception {
        try {
            Vector vecResult = new Vector();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);

            Map paramList = (Map)programMap.get("paramList");
            MapList objectList = (MapList)programMap.get("objectList");
            String languageStr = (String) paramList.get("languageStr");
            i18nNow loc = new i18nNow();
            final String RESOURCE_BUNDLE = "emxComponentsStringResource";
            String strYes = loc.GetString(RESOURCE_BUNDLE, languageStr, "emxComponents.Common.Yes");
            String strNo = loc.GetString(RESOURCE_BUNDLE, languageStr, "emxComponents.Common.No");

            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                Map map = (Map)itrObjects.next();
                String strLicenseTrigram = (String)map.get("License Trigram");
                String strLicenseAssigned = (String)map.get("License Assigned");
                if ("true".equals(strLicenseAssigned)){
                    vecResult.add(strYes);
                }
                else {
                    vecResult.add(strNo);
                }
            }
            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**  To get the type of License, it can be Casual or Full
     * @param context
     * @param args
     * @return a vector
     * @throws Exception
     */
    public Vector getLicenseType(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objPageList = (MapList)programMap.get("objectList");
        HashMap paramMap = (HashMap)programMap.get("paramList");
        String strLanguage      = (String)paramMap.get("languageStr");

        Vector columnValues = new Vector(objPageList.size());
        try {
            String[] objIds = new String[objPageList.size()];
            for (int i = 0; i < objPageList.size(); i++) {
            	String sCasaulHours = (String)((HashMap)objPageList.get(i)).get(LicenseUtil.INFO_CASUAL_HOUR);
	            if(UIUtil.isNotNullAndNotEmpty(sCasaulHours)){
	            	Integer iCasualHours = Integer.parseInt(sCasaulHours);
	                if(iCasualHours > 0){                	
	                	columnValues.add(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(context.getSession().getLanguage()), "emxComponents.Common.Licensing.Casual"));
	                }else{                	                	
	                	columnValues.add(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(context.getSession().getLanguage()), "emxComponents.Common.Licensing.Full"));
	                }
	            }else{
	            	columnValues.add("");
	            }	                
            }
        }
        catch (Exception Ex){     
             throw Ex;
        }
        return columnValues;
    }

    /**
     * Column program to get Count of Available Licenses called from Assign License by User
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding Count of Available Licenses.
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public  Vector getAvailableUsersLicenses(Context context, String[] args) throws Exception {
        try {
            Vector vecResult = new Vector();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            Map paramList = (Map)programMap.get("paramList");
            MapList objectList = (MapList)programMap.get("objectList");

            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                Map map = (Map)itrObjects.next();
                String strAvaliableLicenses = (String)map.get(LicenseUtil.INFO_TOTAL_COUNT);
                vecResult.add(strAvaliableLicenses);
            }


            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Column program to get Count of Licenses Assigned called from Assign License by User
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding Count of Licenses Assigned.
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public  Vector getAssignedUsersLicenses(Context context, String[] args) throws Exception {
        try {
            Vector vecResult = new Vector();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            Map paramList = (Map)programMap.get("paramList");
            MapList objectList = (MapList)programMap.get("objectList");

            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                Map map = (Map)itrObjects.next();
                String strAssignedLicenses = (String)map.get("Total License Assigned");
                vecResult.add(strAssignedLicenses);
            }
            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     *  Column program to get licenses consumed called from Assign License by User
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding consumed licenses.
     * @throws Exception
     * @since R207
     */
    public  Vector getConsumedUsersLicenses(Context context, String[] args) throws Exception {
        try {
            Vector vecResult = new Vector();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            Map paramList = (Map)programMap.get("paramList");
            MapList objectList = (MapList)programMap.get("objectList");

            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                Map map = (Map)itrObjects.next();
                String strConsumedLicenses = (String)map.get(LicenseUtil.INFO_IN_USE_COUNT);
                vecResult.add(strConsumedLicenses);
            }
            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * update  function for updating the licenses that have been modified called from Assign License by User
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return boolean holding Values true/false
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public  void updateLicense(Context context, String[] args) throws Exception {
        String strLicenseTrigram = "";
        String strPersonName = "";
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap)programMap.get("paramMap");
            strLicenseTrigram = (String)paramMap.get("objectId");
            HashMap requestMap = (HashMap)programMap.get("requestMap");
            String strObjectId = (String)requestMap.get("objectId");
            String strUserCommand = "print bus $1 select $2 dump";
            strPersonName = MqlUtil.mqlCommand(context, strUserCommand, strObjectId, "name");
			DomainObject dObj = new DomainObject(strObjectId);
            Integer casualHours = Integer.parseInt(dObj.getInfo(context, DomainObject.getAttributeSelect(ATTRIBUTE_LICENSED_HOURS)));

            String strNewValue = (String)paramMap.get("New Value");
            String strResult = null;
            if ("Yes".equals(strNewValue)){
                //ContextUtil.pushContext(context, "User Agent", null, context.getVault().getName());
            	if(casualHours == 0){
            		strUserCommand = "modify product $1 add $2 $3";
					if("RWA".equalsIgnoreCase(strLicenseTrigram) && !isProductAssingned(context,"CSV",strPersonName)){
						strResult = MqlUtil.mqlCommand(context, strUserCommand, true, "CSV", "person", strPersonName);
					}
            		strResult = MqlUtil.mqlCommand(context, strUserCommand, true, strLicenseTrigram, "person", strPersonName);
            	}else{
            		strUserCommand = "modify product $1 add $2 $3 $4 $5";
					if("RWA".equalsIgnoreCase(strLicenseTrigram) && !isProductAssingned(context,"CSV",strPersonName)){
						strResult = MqlUtil.mqlCommand(context, strUserCommand, true, "CSV", "casualhour", casualHours.toString(), "person", strPersonName);
					}
            		strResult = MqlUtil.mqlCommand(context, strUserCommand, true, strLicenseTrigram, "casualhour", casualHours.toString(), "person", strPersonName);
            	}
            } else {
				//Validate whether that user has consumed license or not
                    java.util.List consumedList = LicenseUtil.getLicenseUsage(context,strLicenseTrigram,null);
					StringList strlConsumedPersonList = new StringList();
					for(int k=0; k<consumedList.size(); k++)
					{
						java.util.HashMap hm = (HashMap) consumedList.get(k);
						strlConsumedPersonList.add(hm.get(matrix.util.LicenseUtil.USAGE_USER_NAME));
					}            	   		
				if(strlConsumedPersonList.contains(strPersonName))
				{
	                LicenseUtil.releaseLicenses(context, strPersonName, new String[] {strLicenseTrigram}, null);
				}
				if(isProductAssingned(context,strLicenseTrigram,strPersonName ))
                {                
                    strUserCommand = "modify product $1 remove person $2";
					if("RWA".equalsIgnoreCase(strLicenseTrigram) && isProductAssingned(context,"CSV",strPersonName)){
						strResult = MqlUtil.mqlCommand(context, strUserCommand, true, "CSV", strPersonName);
					}
                    strResult = MqlUtil.mqlCommand(context, strUserCommand, true, strLicenseTrigram, strPersonName);
                }
                
            }
        }
        catch(Exception exp) {
            exp.printStackTrace();
            String strErrMsg = exp.getMessage();
            if(strErrMsg.indexOf("is not releasable") != -1)
            {            
                StringBuffer msg = new StringBuffer("'"+strLicenseTrigram+"' ");
                Locale strLocale = new Locale(context.getSession().getLanguage());
                msg.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",strLocale, "emxComponents.Common.LicenseError.NotReleasable"));
                msg.append(" "+strPersonName);
                throw new Exception(msg.toString());
            }
            else
                throw exp;
        }
    }

	private boolean isProductAssingned(Context context, String sLicenseTrigram, String sPersonName) throws Exception{
		boolean flag = false;
		String strPersonCommand = "print product $1 select $2 dump $3";
		String strPersonResult = MqlUtil.mqlCommand(context, strPersonCommand, true, sLicenseTrigram, "person", "|");
		StringList strResultList = FrameworkUtil.split(strPersonResult,"|");
		if(strResultList.contains(sPersonName)){  
			flag = true;
		}
		return flag;
	}

    /**
     * Range  function   called from Assign License by User
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Map holding Values Yes/No .
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public Map showCombobox (Context context, String[] args) throws Exception{
        try {
            Map map = new HashMap();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap)programMap.get("paramMap");
            String languageStr = (String) paramMap.get("languageStr");
            StringList fieldRangeValues = new StringList();
            StringList fieldDisplayRangeValues = new StringList();

            final String RESOURCE_BUNDLE = "emxComponentsStringResource";
            i18nNow loc = new i18nNow();
            fieldRangeValues.addElement("Yes");
            fieldRangeValues.addElement("No");


            String strYes = loc.GetString(RESOURCE_BUNDLE, languageStr, "emxComponents.Common.Yes");
            String strNo = loc.GetString(RESOURCE_BUNDLE, languageStr, "emxComponents.Common.No");
            fieldDisplayRangeValues.addElement(strYes);
            fieldDisplayRangeValues.addElement(strNo);

            map.put("field_choices", fieldRangeValues);
            map.put("field_display_choices", fieldDisplayRangeValues);

            return  map;
        }
        catch (Exception exp){
            exp.printStackTrace();
            throw exp;
        }

    }

     /**
     * Return the Title value of the product passed as argument from property file
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param name contains the Trigram of the product
     * @param language contains the current session language
     * @return String contains the title of the product
     * @throws Exception if the operation fails.
     * @since R207.
     */
    private String getTitleValue(Context context, String name, String language)throws Exception
    {
        String key = "emxFramework."+name+".Title";
        Locale strLocale = new Locale(language);
        String value = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, key);
        String title = "";
        //Added to check whether the product is add-on product or not
        try{
            title = MqlUtil.mqlCommand(context,"print product $1 select $2 dump $3", name, "title", "|");
        }
        catch(Exception e)
        {
            value = "";
            return value;
        }
        if(key.equals(value))
        {
            if(!"".equals(title))
                value = title;
            else
                value = name;
        }
        return value;
    }

    /**
     * Expand  program to get list of products in database under person context.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return MapList holding product and perons names  .
     * @throws Exception if the operation fails.
     * @since R207.
     */

    public MapList getListOfDatabaseProductsForPerson(Context context,String[] args) throws Exception
    {
        MapList prctMlist = new MapList();
        String strLang = context.getSession().getLanguage();
        try
        {
            String    mqlCommand =null;
            StringList objectSelects =  new StringList();
            objectSelects.add(DomainConstants.SELECT_NAME);
            objectSelects.add(DomainConstants.SELECT_ID);
            DomainObject dObj = null;

            //To get the Object id List
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String objectId=(String)programMap.get("objectId");
            if(objectId !=null && !"".equals(objectId))
                dObj = new DomainObject(objectId);
            if(dObj != null && !dObj.exists(context))
                return prctMlist;
            else
            {
                String personName = dObj.getInfo(context,dObj.SELECT_NAME);
                mqlCommand=  MqlUtil.mqlCommand(context,"list product $1 where $2", "*", "!technical");
                String strProductName = "";
                StringList strlProducts = FrameworkUtil.split(mqlCommand,"\n");
                List info = LicenseUtil.getLicenseInfo(context,null);
                String strTotalLicensesAssigned = "";
                String strlicenseAssigned = "";
                for(int m=0; m<strlProducts.size(); m++)
                {
                    strProductName = (String) strlProducts.get(m);
                    Map prodtMap = null;
                    prodtMap = new HashMap();
                    strlicenseAssigned = "false";
                    for( int k=0, len=info.size(); k<len; k++ )
                    {
                        strTotalLicensesAssigned = "";
                        HashMap rowmap = (HashMap)info.get(k);
                        String licTrigram = (String)   rowmap.get(LicenseUtil.INFO_LICENSE_NAME);

                        if(strProductName.equals(licTrigram))
                        {
                            Integer licTotalCount =  (Integer)  rowmap.get(LicenseUtil.INFO_TOTAL_COUNT) ;
                            Integer licInUseCount =(Integer)   rowmap.get(LicenseUtil.INFO_IN_USE_COUNT);
                            prodtMap.put(LicenseUtil.INFO_TOTAL_COUNT,licTotalCount.toString());
                            prodtMap.put(LicenseUtil.INFO_IN_USE_COUNT,licInUseCount.toString());
                            break;

                        }
                    }
                    StringList slAssignees = null;
                    String strCommand = "print product $1 select $2 dump";
                    String strlistOfAssignees = MqlUtil.mqlCommand(context, strCommand, strProductName.trim(), "person");
                    if (strlistOfAssignees != null && !"".equals(strlistOfAssignees)){
                        slAssignees = FrameworkUtil.split(strlistOfAssignees, ",");
                        if(slAssignees.contains(personName))
                            strlicenseAssigned = "true";
                        if (slAssignees.size() != 0){
                            strTotalLicensesAssigned = String.valueOf(slAssignees.size());
                        }
                    }
                    String strProductTitle = getTitleValue(context,strProductName,strLang);
                    if("".equals(strProductTitle))
                    {
                        strProductTitle = strProductName;
                        strTotalLicensesAssigned = "";
                        prodtMap.put("RowEditable", "readonly");
                    }
                    if (strProductTitle.indexOf("&") != -1){
                        strProductTitle = FrameworkUtil.findAndReplace(strProductTitle, "&", "&amp;");
                    }
                    prodtMap.put("License Name",strProductTitle);
                    prodtMap.put("id",strProductName);
                    prodtMap.put("objectType","product");
                    prodtMap.put("License Assigned",strlicenseAssigned);
                    prodtMap.put("Total License Assigned", strTotalLicensesAssigned);
                    prctMlist.add(prodtMap);

                }
            }
        }catch(Exception e )
        {
            e.printStackTrace();
            if(e.toString().indexOf("License error") != -1)
            {
            	Locale strLocale = new Locale(strLang);
                throw new MatrixException(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource",strLocale, "emxComponents.Common.LicenseError").toString());
            }
            else
                throw new Exception(e.toString());
        }
        return prctMlist;
    }

    /**
     * Program to invoke the corresponding program defined in the expandProgramMenu under person context.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return MapList holding products and assigned details for that person.
     * @throws Exception if the operation fails.
     * @since R207.
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAssignLicenseByUser(Context context, String args[]) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String selectedMenu = (String) programMap.get("selectedProgram");
        if(selectedMenu != null && selectedMenu.equals("emxLicense:getListOfLicenseServerProducts"))
            return getUserLicense(context,args);
        else
            return getListOfDatabaseProductsForPerson(context,args);

    }


    /**
     * Program to get the Consumed User List from the license server for the passed product trigram.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param prodId holds  product trigram.
     * @param licenseServername holds  name of the server where LUM Server is installed.
     * @return StringList holding the consumed person list.
     * @throws Exception if the operation fails.
     * @since R207.
     */


    public StringList getConsumedUserList(Context context, String prodId, String licenseServername, boolean forCasualLicenses ) throws Exception
    {
        List consumedList = LicenseUtil.getLicenseUsage(context,prodId,null);
        StringList strlist = new StringList();
        for(int i=0; i<consumedList.size(); i++)
        {
            HashMap hm = (HashMap) consumedList.get(i);
            Integer iCasaulHour = (Integer)hm.get(LicenseUtil.INFO_CASUAL_HOUR);
            if(forCasualLicenses && iCasaulHour >0){
            strlist.add(hm.get(LicenseUtil.USAGE_USER_NAME));
            }else if (!forCasualLicenses && iCasaulHour ==0){
            strlist.add(hm.get(LicenseUtil.USAGE_USER_NAME));
        }
        }
        return strlist;
    }



    /**
     * Style function to show cell in different color if it exceeds value    called from Assign License by Product
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds  arguments.
     * @return Vector holding name of style class
     * @throws Exception if the operation fails.
     * @since R207.
     */
    public StringList getStyleForNotAssigned(Context context, String[]args)throws Exception {
        try {
            StringList slStyles = new StringList();
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            int iTotalLicenseAssigned = 0;
            int licenseAvaliable = 0;

            Iterator objectItr = objectList.iterator();
            while(objectItr.hasNext()){
                Map curObjectMap = (Map) objectItr.next();
                String strObjectType = (String)curObjectMap.get("objectType");
                //Added for Special Case
                String strShowColor = (String)curObjectMap.get("hasShowColor");
                if(strShowColor!=null && strShowColor.equalsIgnoreCase("true"))
                {
                    slStyles.addElement("license-OverAllocation");
                }
                else
                {
                    slStyles.addElement("");
                }
            }
            return slStyles ;
        }
        catch (Exception exp){
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * This method will be used to reload the 'User Product License' structure browser data.
     * When a license is assigned or deassigned for a person,
     * we need to display the updated information against Licenses - Assigned and Consumed columns.
     * In this method we are updating the 'ObjectList' with the new information.
     * @param context
     * @param args
     * @return returns the hash map containing SB refresh action parameter (Action = refresh).
     *
     * @throws Exception
     */

    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map reloadUserProductLicense(Context context, String[] args) throws Exception{
        //This method is added to fix 371656
        Map programMap = (Map) JPO.unpackArgs(args);
        Map tableData = (Map) programMap.get("tableData");
        Map paramMap = (Map) programMap.get("paramMap");
        String personObjectId = (String) paramMap.get("objectId"); // Person's Object Id
        String strUserName = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", personObjectId, "name"); // Get the Person Name from object Id
        MapList ObjectList = (MapList) tableData.get("ObjectList"); // Get the Object List (data rendered in SB)
        updateObjectList(context, strUserName, ObjectList); // Update the SB data (object list) with the new values.
        Map returnMap = new HashMap();
        returnMap.put("Action", "refresh");
        return returnMap;
    }


    private void updateObjectList(Context context, String strUserName, MapList objectList) throws Exception {
        if(objectList.size() == 0)
            return;

        //First get the Product LIC info in License Server.
        //This is to get the inuse and total licensed info.
        List licenseServerInfo = LicenseUtil.getLicenseInfo(context, null);
        Map licSerInfoMap = new HashMap();
        for (Iterator iter = licenseServerInfo.iterator(); iter.hasNext();) {
            HashMap rowmap = (HashMap)iter.next();
            StringList licInfo = new StringList();
            Integer totCount = ((Integer)rowmap.get(LicenseUtil.INFO_TOTAL_COUNT));
            Integer inUseCount = ((Integer)rowmap.get(LicenseUtil.INFO_IN_USE_COUNT));
            licInfo.add(totCount.toString());
            licInfo.add(inUseCount.toString());
            licSerInfoMap.put(rowmap.get(LicenseUtil.INFO_LICENSE_NAME), licInfo);
        }

        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map product = (Map) iter.next();
            boolean isReadOnly = "readonly".equals(product.get("RowEditable"));
            if(isReadOnly)
                continue; // This is a read only row, data can't be updated by the user so need to get latest info.

            String licTriagram = (String) product.get(DomainConstants.SELECT_ID);
            int licCasualHours = ((Integer) product.get(LicenseUtil.INFO_CASUAL_HOUR));
            String strlistOfAssignees ="";
            String strCommand = "print product $1 select $2 dump $3";
            if(licCasualHours > 0){
            	strlistOfAssignees = MqlUtil.mqlCommand(context, strCommand, true, licTriagram, "casualhour["+licCasualHours+"].person", "|"); 
            }else{
            	strlistOfAssignees = MqlUtil.mqlCommand(context, strCommand, true, licTriagram, "person", "|");
            }
            StringList slAssignees = FrameworkUtil.split(strlistOfAssignees, "|");
            String isLicAssigned = slAssignees.contains(strUserName) ? "true" : "false";
            String totAssignedLic = slAssignees.size() > 0 ? String.valueOf(slAssignees.size()) : "";

            product.put("License Assigned", isLicAssigned);
            product.put("Total License Assigned", totAssignedLic);

            StringList licInfo = (StringList) licSerInfoMap.get(licTriagram);
            if(licInfo != null) {
                product.put(LicenseUtil.INFO_TOTAL_COUNT, licInfo.get(0));
                product.put(LicenseUtil.INFO_IN_USE_COUNT, licInfo.get(1));
            }
        }
    }
    
    public StringList isPersonInactive(Context context, String[] args) throws Exception{
        Map programMap = (Map) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String personId = (String)requestMap.get("objectId");
        DomainObject dom = DomainObject.newInstance(context,personId);
        String currentState = dom.getInfo(context, DomainConstants.SELECT_CURRENT);
        StringList returnStringList = new StringList(objectList.size());
        boolean hasAccess = true;
        if(currentState.equals(PropertyUtil.getSchemaProperty(context, "policy", PropertyUtil.getSchemaProperty(context, "policy_Person"), "state_Inactive"))){
        	hasAccess = false;
        }
        for (int i = 0; i<objectList.size(); i++) {
            returnStringList.add(Boolean.valueOf(hasAccess));
        }       
        return returnStringList;
    }

}
