/*
**   emxUtilBase
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import matrix.db.BusinessObjectProxy;
import matrix.db.Context;
import matrix.db.FileList;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.RelationshipType;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.fcs.common.ImageRequestData;
/**
 * The <code>emxUtilBase</code> jpo contains UI Table Component methods.
 *
 * @version AEF 10.0.1.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxUtilBase_mxJPO
{

	public static String sColorLink         = "#04A3CF";
	public static String[] sColorsCharts    = { "00b2a9", "329cee", "f6bd0f", "8BBA00", "ec0c41", "752fc3", "AFD8F8", "fad46c", "c9ff0d", "F984A1", "A66EDD" };


    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */

    public emxUtilBase_mxJPO (Context context, String[] args) throws Exception
    {
        //if (!context.isConnected())
            //throw new Exception("not supported no desktop client");
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     */

    public int mxMain(Context context, String[] args)  throws Exception
    {
        //if (!context.isConnected())
            //throw new Exception("not supported no desktop client");
        return 0;
    }

    /**
     * This utility method gets list of property names as an input
     * and returns ArrayList of admin names.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds an array of Strings
     * @return an ArrayList object
     * @throws Exception if the operation fails
     */

    public ArrayList getAdminNameFromProperties(Context context,  String[] args) throws Exception
    {
        // Registration Program Name.
        String regProgName = "eServiceSchemaVariableMapping.tcl";

        // return list for admin names
        ArrayList al = new ArrayList(args.length);

        // For each property name get admin name from registration program.
        String cmds[] = new String[1];
        StringBuffer cmdsBuffer = new StringBuffer(80);
        cmdsBuffer.append("print program '");
        cmdsBuffer.append(regProgName);
        cmdsBuffer.append("' select");
        for (int i = 0; i < args.length; i++)
        {
            cmdsBuffer.append(" property[");
            cmdsBuffer.append(args[i]);
            cmdsBuffer.append("].to");
        }
        cmdsBuffer.append(" dump |");
        cmds[0] = cmdsBuffer.toString();
        String adminTypeAndName = (String)(executeMQLCommands(context, cmds)).get(0);

        // Get only admin name from output of above command.
        StringTokenizer st1 = new StringTokenizer(adminTypeAndName, "|");
        while (st1.hasMoreTokens())
        {
            StringTokenizer st2 = new StringTokenizer(st1.nextToken());
            StringBuffer adminName = new StringBuffer(32);
            int count = 0;
            while (st2.hasMoreTokens())
            {
                String token = st2.nextToken();
                if (count != 0)
                {
                    adminName.append(" ");
                    adminName.append(token);
                }
                count++;
            }
            al.add((adminName.toString()).trim());
        }

        return al;
    }

    /**
     * This utility method gets list of property names of policy and states as an input
     * and returns ArrayList of state names.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds an array of Strings
     * @return an ArrayList object
     * @throws Exception if the operation fails
     */

    public ArrayList getStateNamesFromProperties(Context context,  String[] args) throws Exception
    {
        // Registration Program Name.
        String regProgName = "eServiceSchemaVariableMapping.tcl";

        // return list for admin names
        ArrayList al = new ArrayList(args.length/2);

        // For each property name get admin name from registration program.
        for (int i = 0; i < args.length/2; i++)
        {
            String adminName = null;

            if (args[i*2].startsWith("policy_"))
            {
                String props[] = new String[1];
                props[0] = args[i*2];
                adminName = (String)(getAdminNameFromProperties(context, props)).get(0);
            }
            else
            {
                adminName = args[i*2];
            }

            if (adminName.length() != 0)
            {
                String cmds[] = new String[1];
                StringBuffer cmdsBuffer = new StringBuffer(50);
                cmdsBuffer.append("print policy '");
                cmdsBuffer.append(adminName);
                cmdsBuffer.append("' select property[");
                cmdsBuffer.append(args[i*2+1]);
                cmdsBuffer.append("].value dump");
                cmds[0] = cmdsBuffer.toString();
                String value = (String)(executeMQLCommands(context, cmds)).get(0);
                al.add(value);
            }
            else
            {
                al.add(adminName.trim());
            }
        }

        return al;
    }

    /**
     * This utility method gets mql command as an input
     * and returns ArrayList of results after executing mql command.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        i - a String contains command
     * @return an ArrayList object
     * @throws Exception if the operation fails
     * @deprecated V6R2014, use MqlUtil.mqlCommand instead.
     */

    public ArrayList executeMQLCommands(Context context,  String[] args) throws Exception
    {
        // return Arraylist
        ArrayList al = new ArrayList();

        // execute each command passed as input argument
        // throw exception if any one of them fails.
        MQLCommand mc = new MQLCommand();
        mc.open(context);
        for(int i = 0; i < args.length; i++)
        {
            mc.executeCommand(context, args[i]);
            String sError = mc.getError().trim();
            if (sError.length() != 0)
            {
                Exception e = new Exception(sError);
                throw e;
            }
            if(mc.getResult().endsWith("\n")){
                al.add(mc.getResult().substring(0,mc.getResult().length()-1));
            }else{
                al.add(mc.getResult());
            }
        }
        mc.close(context);

        return al;
    }

    /**
     * This utility method gets key and base property file name as an input
     * and returns internationalized string.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *         0 - a String contains baseFileName value
     *        1 - a String contains a key
     * @return a String
     * @throws Exception if the operation fails
     */

    public String getI18NString(Context context,  String[] args) throws Exception
    {
        // this is the country
        String strContry = "";
        // this is the string id of the localize tag, if there is one
        String strLanguage = "";

        // Get base file name
        String baseFileName = args[0];
        // Get key
        String key = args[1];

        // Get locale
        String mqlCmds[] = new String[1];
        mqlCmds[0] = "print language";
        try
        {
            // ArrayList retArr = (ArrayList)JPO.invoke(context, "emxUtil", null, "executeMQLCommands", mqlCmds, ArrayList.class);
            // String result = (String)retArr.get(0);
            String result = (String)(executeMQLCommands(context, mqlCmds)).get(0);

            StringTokenizer st1 = new StringTokenizer(result, "\n");
            String localeInfo = st1.nextToken();
            String locale = localeInfo.substring(localeInfo.indexOf('\'') + 1, localeInfo.lastIndexOf('\''));

            int idxDash = locale.indexOf('-');
            int idxComma = locale.indexOf(',');
            int idxSemiColumn = locale.indexOf(';');
            if ((idxComma == -1) && (idxSemiColumn == -1) && (idxDash == -1))
            {
                strLanguage = locale;
            }
            else if (idxDash != -1)
            {
                boolean cont = true;
                if ((idxComma < idxDash) && (idxComma != -1))
                {
                    if ((idxSemiColumn == -1) || (idxComma < idxSemiColumn))
                    {
                        strLanguage = locale.substring(0, idxComma);
                        cont = false;
                    }
                }
                if ((cont) && (idxSemiColumn < idxDash) && (idxSemiColumn != -1) )
                {
                    if ((idxComma == -1) || (idxComma > idxSemiColumn))
                    {
                        strLanguage = locale.substring(0, idxSemiColumn);
                        cont = false;
                    }
                }
                else if (cont)
                {
                    boolean sec2 = true;
                    StringTokenizer st = new StringTokenizer(locale, "-");
                    if (st.hasMoreTokens()) {
                        strLanguage = st.nextToken();
                        if (st.hasMoreTokens()) {
                            strContry = st.nextToken();
                        } else {
                            sec2 = false;
                        }
                    } else {
                        sec2 = false;
                    }
                    int idx = strContry.indexOf(',');
                    if (idx != -1)
                    {
                        strContry = strContry.substring(0,idx);
                    }
                    idx = strContry.indexOf(';');
                    if (idx != -1)
                    {
                        strContry = strContry.substring(0,idx);
                    }
                    if (!sec2) {
                        System.out.println("MATRIX ERROR - LOCAL INFO CONTAINS WRONG DATA");
                    }
                }
            }
            else
            {
                if ((idxComma != -1) && ((idxComma < idxSemiColumn) || (idxSemiColumn == -1)))
                {
                    strLanguage = locale.substring(0, idxComma);
                }
                else
                {
                    strLanguage = locale.substring(0, idxSemiColumn);
                }
            }
        }
        catch (Exception e)
        {
            strLanguage = "";
            strContry = "";
        }

        // Get Resource bundle.
        Locale loc = new Locale(strLanguage, strContry);
        try
        {
            ResourceBundle messages = EnoviaResourceBundle.getBundle(context, baseFileName, loc.getLanguage());
            return (messages.getString(key));
        }
        catch (Exception e)
        {
            return key;
        }
    }

    /**
     * This utility method clears the Person cache in the RMI VM.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an int: 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     */
    public static int clearPersonCache(Context context, String[] args)  throws Exception
    {
        try
        {
            PersonUtil.clearCache(context);
        }
        catch (FrameworkException Ex)
        {
            throw Ex;
        }
        return 0;
    }

    /**
     * This utility method clears the Person property cache in the RMI VM.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *        userName - a String  of the person name
     *        propertyName - a String of the property name
     * @return an int: 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     */

    public static int clearPersonCacheProperty(Context context, String[] args)  throws Exception
    {
       HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       String userName = (String) paramMap.get("userName");
       String propertyName = (String) paramMap.get("propertyName");

        try
        {
            PersonUtil.clearUserCacheProperty(context, userName, propertyName);
        }
        catch (FrameworkException Ex)
        {
            throw Ex;
        }
        return 0;
    }

     /**
     * This utility method loads the Property cache in the RMI VM
     * @param context the eMatrix <code>Context</code> object
     * @param args contains no args, null could be passed
     * @throws Exception if the operation fails
     */
    public static void loadFrameworkProperty(Context context,String [] args) throws Exception
    {
        String sAllSuites = EnoviaResourceBundle.getProperty(context, "eServiceSuites.DisplayedSuites");
    }

     /**
     * This utility method takes field/column values and  autocorrect the mxlink values
     * @param context the eMatrix <code>Context</code> object
     * @param args contains all field values, uiType, and language string
     * @return String contains xml file with error mxlinks and autocorrected data
     * @throws Exception if the operation fails
     */

  public String validateMxLinkData(Context context,String [] args) throws Exception {

      String sMxLinkErrorData = "";
      String uiType= "";
      String languageStr="";
      StringBuffer returnXmlBuf = new StringBuffer(100);
      boolean flag=true;
      int errorMxLinkCount=0;
      HashMap fieldMap = new HashMap();
      Hashtable errorHt = new Hashtable();
      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      if(paramMap.containsKey("uiType")) {
          uiType=(String)paramMap.get("uiType");
          paramMap.remove("uiType");
      }
     if(paramMap.containsKey("language")) {
          languageStr=(String)paramMap.get("language");
          paramMap.remove("language");
      }

      fieldMap = UINavigatorUtil.validateEmbeddedURL(context,paramMap,languageStr);

      if(fieldMap.containsKey("errorMxlink")) {
          errorHt=(Hashtable)fieldMap.get("errorMxlink");
          fieldMap.remove("errorMxlink");
      }

      java.util.Set mxLinkErrorSet = (java.util.Set)errorHt.entrySet();
      Iterator iterator =mxLinkErrorSet.iterator();
      while(iterator.hasNext() && errorMxLinkCount<10) {
           Map.Entry mxlinkME = (Map.Entry)iterator.next();
          sMxLinkErrorData = sMxLinkErrorData+"\n"+(String)mxlinkME.getValue();
          errorMxLinkCount++;
      }
      if(errorMxLinkCount>=10){
		  String errMsg3 = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.DynamicURL.ErrorMsg3");
		  sMxLinkErrorData=sMxLinkErrorData+errMsg3;
      }

      if(!"".equals(sMxLinkErrorData) && uiType.equalsIgnoreCase("structurebrowser")) {
          sMxLinkErrorData = sMxLinkErrorData.replaceAll("(?i)(mxLink\\\\s*:\\\\s*)","");
      }else if( !"".equals(sMxLinkErrorData)) {
    	  Locale locale = new Locale(languageStr);
		  String errMsg1 = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.DynamicURL.ErrorMsg1");
		  String errMsg2 = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.DynamicURL.ErrorMsg2"); 
		  sMxLinkErrorData = sMxLinkErrorData.replaceAll("(?i)(mxLink\\\\s*:\\\\s*)","");
          sMxLinkErrorData = errMsg1 + sMxLinkErrorData;
          sMxLinkErrorData = sMxLinkErrorData + errMsg2;
      }

      returnXmlBuf.append("<mxLinkRoot> <errorMsg> <![CDATA["+sMxLinkErrorData+"]]> </errorMsg>");
      java.util.Set fieldSet = (java.util.Set)fieldMap.entrySet();
      Iterator fieldItr = fieldSet.iterator();
      while(fieldItr.hasNext()) {
        Map.Entry me = (Map.Entry)fieldItr.next();
        returnXmlBuf.append("<mxField name=\"");
        returnXmlBuf.append(me.getKey() +"\">");
        returnXmlBuf.append(" <![CDATA["+me.getValue()+"]]> </mxField>");
      }
      returnXmlBuf.append("</mxLinkRoot>");
      return returnXmlBuf.toString();
      }
  
  
  /**
   * Update cache value for Admin property
   * @param context
   * @param args
   * String array of length 4
   * args[0] -> adminType 
   * args[1] -> adminName 
   * args[2] -> propertyName
   * args[3] -> propertyValue
   * 
   * e.g. {"person", "Test Everything", "IconMailLanguagePreference", "en"}
   * @throws FrameworkException
     * @deprecated V6R2013x, use updateAdminCache instead.
   */
  
   public void updateAdminCahce(Context context, String[] args) throws FrameworkException {
       updateAdminCache(context, args);
   }
   
  /**
   * Update cache value for Admin property
   * @param context
   * @param args
   * String array of length 4
   * args[0] -> adminType 
   * args[1] -> adminName 
   * args[2] -> propertyName
   * args[3] -> propertyValue
   * 
   * e.g. {"person", "Test Everything", "IconMailLanguagePreference", "en"}
   * @throws FrameworkException
   */
  
   public void updateAdminCache(Context context, String[] args) throws FrameworkException {
       if(args == null || args.length != 4) {
           return;
       }
       PropertyUtil.updateAdminProperty(context, args[0], args[1], args[2], args[3]);
   }
   

   
   public MapList getObjectsFromRowIDs(Context context, String[] args) throws Exception {

       MapList mlResult = new MapList();
       HashMap paramMap = (HashMap) JPO.unpackArgs(args);
       String sRowIDs = (String) paramMap.get("emxTableRowId");

       if (sRowIDs != null) {
           if (!"".equals(sRowIDs)) {
               String[] aOIDs = sRowIDs.split(";");
               for (int i = 0; i < aOIDs.length; i++) {
                   String sOID = aOIDs[i];
                   Map mResult = new HashMap();
                   mResult.put("id", sOID);
                   mlResult.add(mResult);
               }
           }
       }

       return mlResult;
   }

   public static String getMCSURL(Context context, String[] args) throws Exception {

       Map programMap  = (Map) JPO.unpackArgs(args);           
       Map imageData   = new HashMap();
       
       if(programMap.containsKey("paramList")) {                
           Map paramList = (Map)programMap.get("paramList");
           imageData= (Map)paramList.get("ImageData");
       } else {
           Map requestMap = (Map)programMap.get("requestMap");
           imageData= (Map)requestMap.get("ImageData");                    
       }
               
       return (String)imageData.get("MCSURL");        

   }  
       
   public static String getPrimaryImageURL(Context context, String[] args, String sOID, String sFormat, String sMCSURL, String sDefaultImage) throws Exception {

       if(null == sDefaultImage || "".equals(sDefaultImage)) { sDefaultImage = "../common/images/icon48x48ImageNotFound.gif"; }
       
       String sResult          = sDefaultImage;
       DomainObject dObject    = new DomainObject(sOID);    
       StringList busSelects   = new StringList();
       
       busSelects.add("to["+ PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_ImageHolder) +"].from.id");
       busSelects.add("to[Image Holder].from.attribute[Primary Image]");
       busSelects.add("from[Primary Image].to.id");

       Map mData               = dObject.getInfo(context, busSelects);                       
       String sOIDImageHolder  = (String)mData.get("to["+ PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_ImageHolder) +"].from.id");
       String sFileName        = (String)mData.get("to[Image Holder].from.attribute[Primary Image]");

       if(null == sOIDImageHolder) {            
           sOIDImageHolder = (String)mData.get("from[Primary Image].to.id");
           if(null != sOIDImageHolder) {    
               DomainObject doImage = new DomainObject(sOIDImageHolder);
               FileList fileList = doImage.getFiles(context);
               if (fileList.size() > 0) {
                   for (int k = 0; k < fileList.size(); k++) {
                       matrix.db.File fTemp = (matrix.db.File) fileList.get(k);
                       String sFormatFile = fTemp.getFormat();  
                       if(sFormatFile.equals(sFormat)) {
                           sFileName = fTemp.getName();
                           break;
                       }
                   }
               }
           }
       }            

       if(null != sOIDImageHolder) {  

           if(!sFormat.equals("generic")) {
               sFileName = sFileName.substring(0, sFileName.lastIndexOf(".")) + ".jpg";
           }
             
           ArrayList bopArrayList  = new ArrayList();
           BusinessObjectProxy bop = new BusinessObjectProxy(sOIDImageHolder, sFormat, sFileName, false, false);
           bopArrayList.add(bop);	        
           String[] tmpImageUrls = ImageRequestData.getImageURLS(context, sMCSURL, bopArrayList);
           sResult = tmpImageUrls[0];
       } 

       return sResult;

   }

               
   
   // Set visibility of web form elements depending on view/edit mode
   public static Boolean checkViewMode(Context context, String[] args) throws Exception {

       Boolean bResult     = true;
       HashMap requestMap  = (HashMap) JPO.unpackArgs(args);
       String sMode        = (String) requestMap.get("mode");

       try {            
           if (sMode.equals("edit")) {
               bResult = false;
           } else if (sMode.equals("")) {
               sMode = (String) requestMap.get("editLink");
               if (!sMode.equals("")) {
                   bResult = true;
               }
           }
       } catch (Exception e) {}

       return bResult;
   }    
   public static Boolean checkEditMode(Context context, String[] args) throws Exception {

       Boolean bResult     = false;
       HashMap requestMap  = (HashMap) JPO.unpackArgs(args);
       String sMode        = (String) requestMap.get("mode");
       
       try {            
           if (sMode.equals("edit")) { bResult = true; }
       } catch (Exception e) {}

       return bResult;
   }   
   
   
   // Update Program to edit relationships in Structure Browser
   // In use by WBS view of projects to manage reference folder of tasks
   public static boolean updateRelationship(Context context, String[] args) throws Exception {

       HashMap programMap      = (HashMap) JPO.unpackArgs( args );
       HashMap paramMap        = (HashMap) programMap.get( "paramMap" );  
       HashMap columnMap       = (HashMap) programMap.get("columnMap");
       String sOID             = (String) paramMap.get( "objectId" );
       String sNewValue        = (String) paramMap.get( "New Value" );
       String sExpression      = (String)columnMap.get("expression_businessobject");
       DomainObject dObject    = new DomainObject(sOID);        
       
       StringList relSelects = new StringList();
       relSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
       
       if(sExpression.contains("[")) {            
           if(sExpression.contains("]")) {
               String sRelationship = sExpression.substring(sExpression.indexOf("[") + 1, sExpression.indexOf("]"));                
               Map mRelationship = dObject.getRelatedObject(context, sRelationship, true, null, relSelects);                
               if(null != mRelationship) {
                   String sRID = (String)mRelationship.get(DomainObject.SELECT_RELATIONSHIP_ID);
                   DomainRelationship.disconnect(context, sRID);
               }                
               dObject.addToObject(context, new RelationshipType(sRelationship), sNewValue);                               
           }
       }        
       
       return true;
       
   }    
   
   
   // Edit Types and Policies in Structure Browser
   // Used by Folder Browser in Projects
   public Map reloadTypes(Context context, String[] args)throws Exception {

       Map mResult     = new HashMap();
       Map programMap  = (HashMap) JPO.unpackArgs(args);        
       Map requestMap  = (HashMap) programMap.get("requestMap");
       String sLang    = (String)requestMap.get("languageStr");
       Map rowValues   = (HashMap) programMap.get("rowValues");
       String sOID     = (String) rowValues.get("objectId"); 

       MapList mlTypes         = new MapList();
       StringList slValues     = new StringList();
       StringList slDisplay    = new StringList();
       DomainObject dObject    = new DomainObject(sOID);
       String sRootType        = dObject.getInfo(context, "type.kindof");

       String sTypes       = MqlUtil.mqlCommand(context, "print type $1 select $2", sRootType, "derivative.abstract");
       String sHidden      = MqlUtil.mqlCommand(context, "print type $1 select $2", sRootType, "derivative.hidden");
       
       String[] aTypes  = sTypes.split("derivative");
       String[] aHidden = sHidden.split("derivative");
       
       for(int i = 1; i < aTypes.length; i++) {
       
           if(aTypes[i].contains("= FALSE")) {
               
               String sName = aTypes[i].substring(1, aTypes[i].indexOf("]"));
               
               if(!aHidden[i].contains(".hidden = TRUE")) {
                   if(!sName.contains(" Versioned ")) {
          
                       String sNameNLS = i18nNow.getTypeI18NString(sName, sLang);
                
                       Map mType = new HashMap();
                       mType.put("value", sName);
                       mType.put("display", sNameNLS);            
          
                       mlTypes.add(mType);
                       
                   }
               }                
           }
                       
       }
       
       mlTypes.sort("display", "ascending", "String");
       
       for(int i = 0; i < mlTypes.size(); i++) {            
           Map mType = (Map)mlTypes.get(i);
           slValues.add((String)mType.get("value"));
           slDisplay.add((String)mType.get("display"));          
       }        

       mResult.put("RangeValues"       , slValues  );
       mResult.put("RangeDisplayValue" , slDisplay );        
   
       return mResult;
       
   }       
   public Map reloadPolicies(Context context, String[] args)throws Exception {

       Map mResult     = new HashMap();
       Map programMap  = (HashMap) JPO.unpackArgs(args);        
       Map requestMap  = (HashMap) programMap.get("requestMap");
       String sLang    = (String)requestMap.get("languageStr");
       Map rowValues   = (HashMap) programMap.get("rowValues");
       String sOID     = (String) rowValues.get("objectId"); 
       
       DomainObject dObject    = new DomainObject(sOID);
       String sType            = dObject.getInfo(context, "type");
       MapList mlPolicies      = mxType.getPolicies(context, sType, false);
       StringList slValues     = new StringList();
       StringList slDisplay    = new StringList();
       
       mlPolicies.sort("name", "ascending", "String");
       
       for(int i = 0; i < mlPolicies.size(); i++) {
           
           Map mPolicy     = (Map)mlPolicies.get(i);
           String sName    = (String)mPolicy.get("name");
           String sNameNLS = i18nNow.getI18nString("emxFramework.Policy." + sName.replace(" ", "_") , "emxFrameworkStringResource", sLang);
           
           mPolicy.put("value", sName);
           mPolicy.put("display", sNameNLS);
           
       }
       
       mlPolicies.sort("display", "ascending", "String");
       
       for(int i = 0; i < mlPolicies.size(); i++) {            
           Map mPolicy = (Map)mlPolicies.get(i);
           slValues.add((String)mPolicy.get("value"));
           slDisplay.add((String)mPolicy.get("display"));          
       }        

       mResult.put("RangeValues"       , slValues  );
       mResult.put("RangeDisplayValue" , slDisplay );        
   
       return mResult;
       
   }    
}
