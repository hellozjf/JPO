/*
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/


import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;

import matrix.db.State;
import matrix.db.StateList;
import matrix.util.*;
import java.util.*;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.domain.*;



/**
 * The <code>emxTriggerReportBase</code> class contains methods for Trigger Tool
 *
 * @version AEF 11-0 - Copyright (c) 2005, MatrixOne, Inc.
 */

public class emxTriggerValidationResultsBase_mxJPO
{


  /**
   * Constructor.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @throws Exception if the operation fails
   * @since AEF 11-0
   */

  public emxTriggerValidationResultsBase_mxJPO(Context context, String[] args)
    throws Exception
  {

  }

  /**
   * This method is executed if a specific method is not specified.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @return int 0, status code
   * @throws Exception if the operation fails
   * @since AEF 11-0
   */

  public int mxMain(Context context, String[] args)
    throws Exception
  {
    if (!context.isConnected())
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        Map paramList = (Map)paramMap.get("paramList");
        String languageStr = (String)paramList.get("languageStr");
        String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.DesktopClient", new Locale(languageStr));        
        throw new Exception(exMsg);
    }
    return 0;
  }

    /**
     * @param context
     * @param args
     * @return MapList
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTriggerResults(Context context, String[] args) throws Exception
    {
    MapList mapReturn = new MapList();
    HashMap paraMap = (HashMap) JPO.unpackArgs(args);
    String strResults = (String) paraMap.get("results");
    StringList slResults = FrameworkUtil.split(strResults,"|");

    for(int i=0;i<slResults.size();i++)
    {
      StringList slValues = FrameworkUtil.split((String)slResults.get(i),"~");
      Map map = new HashMap();
      map.put("id",slValues.get(0));
      map.put("result",slValues.get(1));
      if(UIUtil.isNullOrEmpty(slValues.get(2).toString())){
    	  map.put("description",slValues.get(3));
      }
      else{
    	  map.put("description",slValues.get(2));
      }
      map.put("rev",slValues.get(4));
      mapReturn.add(map);
    }
        return mapReturn;
  }
    /**
     * @param context
     * @param args
     * @return Vector
     * @throws Exception
     */
    public Vector getResults(Context context, String[] args) throws Exception
    {
        HashMap paraMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramListMap = (HashMap)paraMap.get("paramList"); 
        String strExpFormat = (String) paramListMap.get("exportFormat");
        MapList objectList =(MapList) paraMap.get("objectList");
        String languageStr = (String)paramListMap.get("languageStr");
        int objSize = objectList.size();
        Vector v = new Vector(objSize+1);
        HashMap hMap = new HashMap();
        for(int i=0;i<objSize;i++)
        {
            hMap =(HashMap) objectList.get(i);
            String strResult = (String) hMap.get("result");
            if (strExpFormat != null && ("text".equalsIgnoreCase(strExpFormat) || "csv".equalsIgnoreCase(strExpFormat))){
              v.add(XSSUtil.encodeForHTML(context, strResult));
            }
            else{
            	StringBuffer resStr = new StringBuffer();
                if("Pass".equalsIgnoreCase(strResult) && strResult!=null)
                {
                	resStr.append("<table id=error").append(i).append(" width=100% border=0 > <tr> <td align=left> <font forecolor=black >").append(EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.TriggerValidationReport.Result.Pass", new Locale(languageStr))).append("<script language=javascript> javascript:changeColor('Pass',").append(i).append(") </script></font> </td> </tr> </table> ");                	
                }
                if("Fail".equalsIgnoreCase(strResult) && strResult!=null)
                {
                	resStr.append("<table id=error").append(i).append(" width=100% border=0 > <tr> <td align=left> <font forecolor=black >").append(EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.TriggerValidationReport.Result.Fail", new Locale(languageStr))).append("<script language=javascript> javascript:changeColor('Fail',").append(i).append(") </script></font> </td> </tr> </table> ");                	
                }
                if("Warning".equalsIgnoreCase(strResult) && strResult!=null)
                {
                	resStr.append("<table id=error").append(i).append(" width=100% border=0 > <tr> <td align=left> <font forecolor=black >").append(EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.TriggerValidationReport.Result.Warning", new Locale(languageStr))).append("<script language=javascript> javascript:changeColor('Warning',").append(i).append(") </script></font> </td> </tr> </table> ");                	
                }
              //XSSOK
                v.add(resStr.toString());
           }

        }
        return v;
    }
    /**
     * @param context
     * @param args
     * @return Vector
     * @throws Exception
     */
    public Vector getErrorDetails(Context context, String[] args) throws Exception
    {
        HashMap paraMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramListMap = (HashMap)paraMap.get("paramList"); 
        String strExpFormat = (String) paramListMap.get("exportFormat");
        MapList objectList =(MapList) paraMap.get("objectList");
        String languageStr = (String)paramListMap.get("languageStr");
        int objSize = objectList.size();
        Vector v = new Vector(objSize+1);
        HashMap hMap = new HashMap();
        for(int i=0;i<objSize;i++)
        {
            hMap =(HashMap) objectList.get(i);
            String strResult = (String) hMap.get("result");
            String ErrorDetails = (String) hMap.get("rev");
            StringBuffer resStr = new StringBuffer();
            if("fail".equalsIgnoreCase(strResult)&& strResult!=null){
            		resStr.append(ErrorDetails);
           	}
             v.add(resStr.toString());
            
         }
        return v;
    }
    /**
     * @param context
     * @param args
     * @return Vector
     * @throws Exception
     */
    public Vector getDescription(Context context, String[] args) throws Exception
    {
        HashMap paraMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList =(MapList) paraMap.get("objectList");
        int objSize = objectList.size();
        Vector v = new Vector(objSize+1);
        HashMap hMap = new HashMap();
        for(int i=0;i<objSize;i++)
        {
            hMap =(HashMap) objectList.get(i);
            String strDescription = (String)hMap.get("description");
            if(strDescription==null || strDescription.length()==0)
            {
                v.add((String)hMap.get("rev"));
            }
            else
            {
                v.add(strDescription);
            }
        }
        return v;
    }
    /**
     * @param context
     * @param args
     * @return Vector
     * @throws Exception
     */
    public Vector getComments(Context context, String[] args) throws Exception
    {
        HashMap paraMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList =(MapList) paraMap.get("objectList");
        int objSize = objectList.size();
        Vector v = new Vector(objSize+1);
        HashMap hMap = new HashMap();
        for(int i=0;i<objSize;i++)
        {
            hMap =(HashMap) objectList.get(i);
            v.add((String)hMap.get("Comments"));
        }
        return v;
    }

  /**
   * subEnvironmentVars method substitute any ${} macros.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        i - a String Contains environment variable value
   * @throws Exception if the operation fails
   * @since AEF 11-0
   */

  protected void subEnvironmentVars(Context context, String[] args) throws Exception
  {
    String startStr = "${";
    String endStr = "}";
    String[] s = new String[1];
    emxUtil_mxJPO utilObj = new emxUtil_mxJPO(context, args);

    for (int i = 0; i < args.length; i++) {
      String temp = args[i];
      if(temp != null) {
      int startIndex = temp.indexOf(startStr);
      int endIndex = temp.indexOf(endStr);

      // if the start and end delimiters where found, then extract the
      // key and look its value up
      if (startIndex != -1 && endIndex != -1) {
        String key = temp.substring(startIndex + startStr.length(), endIndex);
        s[0] = "get env " + key;
        ArrayList al = utilObj.executeMQLCommands(context, s);
        args[i] = (String)al.get(0);
      }
      }
    }
    }
   /**
   * executeTriggers method executes Trigger selected by User.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *
   * @throws Exception if the operation fails
   * @since AEF 11-0
   */
  public String executeTriggers(Context context, String[] args) throws Exception
  {
    emxUtil_mxJPO utilObj = new emxUtil_mxJPO(context,args);

    String strSelectedId = (String)JPO.unpackArgs(args);
        StringList strSelectedIdList = FrameworkUtil.split(strSelectedId,"|");
        String strObjectId = (String)strSelectedIdList.get(0);
        String strTriggerId = (String)strSelectedIdList.get(1);
    //getting object id that are selected by user for validate
    DomainObject domainObj = DomainObject.newInstance(context);
    StringBuffer strResults = new StringBuffer();
    // Attribute of eService Trigger Program Parameter
    MQLCommand mql = new MQLCommand();
    String[] s = new String[19];
    s[0]  = "attribute_eServiceProgramArgument1";
    s[1]  = "attribute_eServiceProgramArgument2";
    s[2]  = "attribute_eServiceProgramArgument3";
    s[3]  = "attribute_eServiceProgramArgument4";
    s[4]  = "attribute_eServiceProgramArgument5";
    s[5]  = "attribute_eServiceProgramArgument6";
    s[6]  = "attribute_eServiceProgramArgument7";
    s[7] = "attribute_eServiceProgramArgument8";
    s[8] = "attribute_eServiceProgramArgument9";
    s[9] = "attribute_eServiceProgramArgument10";
    s[10] = "attribute_eServiceProgramArgument11";
    s[11] = "attribute_eServiceProgramArgument12";
    s[12] = "attribute_eServiceProgramArgument13";
    s[13] = "attribute_eServiceProgramArgument14";
    s[14] = "attribute_eServiceProgramArgument15";
    s[15] = "vault_eServiceAdministration";
    s[16] = "attribute_eServiceConstructorArguments";
    s[17] = "attribute_eServiceTargetStates";

    ArrayList adminNames = utilObj.getAdminNameFromProperties(context, s);
    String progArg1 = (String)adminNames.get(0);
    String progArg2 = (String)adminNames.get(1);
    String progArg3 = (String)adminNames.get(2);
    String progArg4 = (String)adminNames.get(3);
    String progArg5 = (String)adminNames.get(4);
    String progArg6 = (String)adminNames.get(5);
    String progArg7 = (String)adminNames.get(6);
    String progArg8 = (String)adminNames.get(7);
    String progArg9 = (String)adminNames.get(8);
    String progArg10 = (String)adminNames.get(9);
    String progArg11 = (String)adminNames.get(10);
    String progArg12 = (String)adminNames.get(11);
    String progArg13 = (String)adminNames.get(12);
    String progArg14 = (String)adminNames.get(13);
    String progArg15 = (String)adminNames.get(14);

    String adminVault = (String)adminNames.get(15);
    String constructorParam = (String)adminNames.get(16);
    String targetStates = (String)adminNames.get(17);
    String strResult = "";
    // Get all the information about trigger objects found.
    StringList sl =  new StringList();
    sl.addElement("attribute[" +  progArg1 + "].value");
    sl.addElement("attribute[" +  progArg2 + "].value");
    sl.addElement("attribute[" +  progArg3 + "].value");
    sl.addElement("attribute[" +  progArg4 + "].value");
    sl.addElement("attribute[" +  progArg5 + "].value");
    sl.addElement("attribute[" +  progArg6 + "].value");
    sl.addElement("attribute[" +  progArg7 + "].value");
    sl.addElement("attribute[" +  progArg8 + "].value");
    sl.addElement("attribute[" +  progArg9 + "].value");
    sl.addElement("attribute[" +  progArg10 + "].value");
    sl.addElement("attribute[" +  progArg11 + "].value");
    sl.addElement("attribute[" +  progArg12 + "].value");
    sl.addElement("attribute[" +  progArg13 + "].value");
    sl.addElement("attribute[" +  progArg14 + "].value");
    sl.addElement("attribute[" +  progArg15 + "].value");
    sl.addElement("attribute[" +  constructorParam + "].value");
    sl.addElement("attribute[" +  targetStates + "].value");

    //loop to execute the list of triggers that user have selected

        domainObj.setId(strObjectId);

        //Get the Basic Details about the object
        StringList strInfoList = new StringList();
        strInfoList.add(DomainConstants.SELECT_CURRENT);
        strInfoList.add(DomainConstants.SELECT_POLICY);
        strInfoList.add(DomainConstants.SELECT_TYPE);
        strInfoList.add(DomainConstants.SELECT_NAME);
        strInfoList.add(DomainConstants.SELECT_REVISION);
        strInfoList.add(DomainConstants.SELECT_VAULT);
        strInfoList.add(DomainConstants.SELECT_OWNER);
        strInfoList.add(DomainConstants.SELECT_LOCKED);
        strInfoList.add(DomainConstants.SELECT_DESCRIPTION);
        Map ObjectInfoMap = domainObj.getInfo(context,strInfoList);
        //Get the State Details like Revisionable,Versionable
        State thisState   = null;
        StateList stateList = domainObj.getStates(context);
        Iterator stateItr   = stateList.iterator();
        ArrayList al = new ArrayList();
        //Check for Current state object
        s = new String[29];
        while(stateItr.hasNext())
        {

          // get the next state in the policy
          thisState = (State)stateItr.next();
          if(thisState.isCurrent())
          {
              s[0] = "set env  ISCURRENT "+thisState.isCurrent();
              s[1] = "set env  ISDISABLED "+thisState.isDisabled();
              s[2] = "set env  ISENABLED "+thisState.isEnabled();
              s[3] = "set env  ISOVERRIDDEN "+thisState.isOverridden();
              s[4] = "set env  ISREVISIONABLE "+thisState.isRevisionable();
              s[5] = "set env  ISVERSIONABLE "+thisState.isVersionable();
          //Added:ixe:IR-073686V6R2012:23Oct-2010
          if(stateItr.hasNext()){
              thisState = (State)stateItr.next();
              s[6] = "set env  NEXTSTATE "+thisState.getName();
          }
          //End:ixe:IR-073686V6R2012:23Oct-2010
              break;
          }
        }
        //Get Trigger Object Details
        domainObj.setId(strTriggerId);
        StringList triggerInfoList = new StringList();
        String strProgramAttribute = "attribute[eService Program Name].value";
        String strMethodAttribute = "attribute[eService Method Name].value";
        String strConstuctor = "attribute[" +  constructorParam + "].value";
        triggerInfoList.add(strProgramAttribute);
        triggerInfoList.add(strConstuctor);
        triggerInfoList.add(strMethodAttribute);
        triggerInfoList.add(DomainConstants.SELECT_VAULT);
        Map triggerInfoMap = domainObj.getInfo(context,triggerInfoList);
        Map attMap = domainObj.getInfo(context,sl);
        String methodArgs[] = new String[15];
        for(int k=0;k<15;k++)
        {
            methodArgs[k] = (String) attMap.get(sl.get(k));
        }
        String strMethodName= (String) triggerInfoMap.get(strMethodAttribute);
        if (strMethodName == null || strMethodName.length() == 0)
        {
            strMethodName = "mxMain";
        }
        String strProgramName = (String) triggerInfoMap.get(strProgramAttribute);
        String strConst = (String)triggerInfoMap.get(strConstuctor);
        String constArgs[] = {strConst};
        //Setting Environment Variables
        String strRevision = (String)ObjectInfoMap.get(DomainConstants.SELECT_REVISION);
        String strType = (String)ObjectInfoMap.get(DomainConstants.SELECT_TYPE);
        String strName = (String)ObjectInfoMap.get(DomainConstants.SELECT_NAME);

        s[7] = "set env  0 "+(String) triggerInfoMap.get(strProgramAttribute);
        s[8] = "set env  ACCESSFLAG True ";
        s[9] = "set env  APPLICATION Matrix ";
        s[10] = "set env  AUTOPROMOTE False ";
        s[11] = "set env  CHECKACCESSFLAG True ";
        s[12] = "set env  CURRENTSTATE \""+(String)ObjectInfoMap.get(DomainConstants.SELECT_CURRENT)+"\"" ;
        s[13] = "set env  ENFORCEDLOCKINGFLAG False ";
        s[14] = "set env  EVENT Promote ";
        s[15] = "set env  LATTICE \""+(String)ObjectInfoMap.get(DomainConstants.SELECT_VAULT)+"\"" ;
        s[16] = "set env  LOCKER \""+(String)ObjectInfoMap.get(DomainConstants.SELECT_LOCKER)+ "\"";

        s[17] = "set env  NAME \""+strName+"\"" ;
        s[18] = "set env  OBJECT \" '" + strType +"' '" + strName + "' '" + strRevision + "'\" ";
        s[19] = "set env  OBJECTID "+strObjectId ;
        s[20] = "set env  OWNER \""+(String)ObjectInfoMap.get(DomainConstants.SELECT_OWNER)+"\"" ;
        s[21] = "set env  POLICY \""+(String)ObjectInfoMap.get(DomainConstants.SELECT_POLICY)+"\"" ;
        s[22] = "set env  REVISION \""+strRevision+ "\"";
        s[23] = "set env  STATENAME \""+(String)ObjectInfoMap.get(DomainConstants.SELECT_CURRENT)+"\"" ;
        s[24] = "set env  TRANSACTION update";
        s[25] = "set env  TRIGGER_VAULT \""+(String)triggerInfoMap.get(DomainConstants.SELECT_VAULT) +"\"";
        s[26] = "set env  TYPE \""+strType +"\"";
        s[27] = "set env  USER \""+context.getUser()+"\"" ;
        s[28] = "set env  VAULT \""+(String)ObjectInfoMap.get(DomainConstants.SELECT_VAULT)+"\"" ;

        al = utilObj.executeMQLCommands(context,s);
       try
        {
            s = new String[1];
            s[0] = "print program "+ (String) triggerInfoMap.get(strProgramAttribute)+" select isjavaprogram dump";
            al = utilObj.executeMQLCommands(context,s);
            strResult = (String)al.get(0);
            if("TRUE".equalsIgnoreCase(strResult))
            {
                subEnvironmentVars(context,constArgs);
                subEnvironmentVars(context,methodArgs);
                int iStatus = JPO.invoke(context,strProgramName,constArgs,strMethodName,methodArgs);
                if(iStatus==1)
                {
                    //map.put("result","Fail");
                    //map.put("Comments","");
                    strResults.append("Fail");
                    strResults.append("~");
                }
                if(iStatus==0)
                {
                    //map.put("result","Pass");
                    //map.put("Comments","");
                    strResults.append("Pass");
                    strResults.append("~");
                }
            }
            else
            {
                StringBuffer sbCommand = new StringBuffer();
                sbCommand.append("execute program emxTriggerWrapper.tcl ");
                for (int k = 0; k < 15; k++)
                {
                    sbCommand.append("'");
                    sbCommand.append((String)attMap.get(sl.get(k)));
                    sbCommand.append("' ");
                }
                sbCommand.append(("'"));
                sbCommand.append((String) triggerInfoMap.get(strProgramAttribute));
                sbCommand.append(("' "));
                s[0] = sbCommand.toString();
                ArrayList retArr = utilObj.executeMQLCommands(context, s);
                if("1".equalsIgnoreCase((String)retArr.get(0)) || "False".equalsIgnoreCase((String)retArr.get(0)) )
                {
                    String strErrorType = domainObj.getAttributeValue(context,"eService Error Type");
                    if("Warning".equalsIgnoreCase(strErrorType))
                    {
                        //map.put("result","Warning");
                        //map.put("Comments","");
                        strResults.append("Warning");
                        strResults.append("~");
                    }
                    if("Error".equalsIgnoreCase(strErrorType))
                    {
                        //map.put("result","Fail");
                        //map.put("Comments","");
                        strResults.append("Fail");
                        strResults.append("~");
                    }
                }
                if("0".equalsIgnoreCase((String)retArr.get(0)) || "True".equalsIgnoreCase((String)retArr.get(0)))
                {
                    //map.put("result","Pass");
                    //map.put("Comments","");
                    strResults.append("Pass");
                    strResults.append("~");
                }
            }
        }
        catch(Exception e)
        {
            //map.put("result","Fail");
            //map.put("Comments",e.getMessage());
            strResults.append("Fail");
            strResults.append("~");
        }
        s = new String[30];
        s[0] = "unset env  ISCURRENT ";
        s[1] = "unset env  ISDISABLED ";
        s[2] = "unset env  ISENABLED ";
        s[3] = "unset env  ISOVERRIDDEN ";
        s[4] = "unset env  ISREVISIONABLE ";
        s[5] = "unset env  ISVERSIONABLE ";
        s[6] = "unset env  NEXTSTATE ";
        s[7] = "unset env  0 ";
        s[8] = "unset env  ACCESSFLAG ";
        s[9] = "unset env  APPLICATION ";
        //s[10] = "unset env  ";
        s[10] = "unset env  AUTOPROMOTE ";
        s[11] = "unset env  CHECKACCESSFLAG ";
        s[12] = "unset env  CURRENTSTATE ";
        //s[13] = "unset env  DESCRIPTION " ;
        s[13] = "unset env  ENFORCEDLOCKINGFLAG";
        s[14] = "unset env  EVENT " ;
        //s[15] = "unset env  HASACTUALDATE " ;
        //s[16] = "unset env  HASSCHEDULEDATE ";
        //s[17] = "unset env  INVOCATION ";

        s[15] = "unset env  LATTICE ";
        //s[19] = "unset env  LOCKFLAG ";
        s[16] = "unset env  LOCKER ";
        s[17] = "unset env  NAME ";

        s[18] = "unset env  OBJECT ";
        s[19] = "unset env  OBJECTID ";
        s[20] = "unset env  OWNER ";
        s[21] = "unset env  POLICY ";
        s[22] = "unset env  REVISION ";
        s[23] = "unset env  STATENAME ";
        s[24] = "unset env  TRANSACTION ";
        s[25] = "unset env  TRIGGER_VAULT ";
        s[26] = "unset env  TYPE ";
        s[27] = "unset env  USER ";
        s[28] = "unset env  VAULT ";
        al = utilObj.executeMQLCommands(context,s);
        strResults.append(domainObj.getDescription(context));
        strResults.append("~");
        strResults.append(domainObj.getRevision());
        //mapReturn.add(i,map);`
    return strResults.toString();
    }
}
