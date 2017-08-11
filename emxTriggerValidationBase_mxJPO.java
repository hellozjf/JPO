/*
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;

/**
 * The <code>emxTriggerReportBase</code> class contains methods for Trigger Tool
 *
 * @version AEF 11-0 - Copyright (c) 2005, MatrixOne, Inc.
 */

public class emxTriggerValidationBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 11-0
     */

    public emxTriggerValidationBase_mxJPO(Context context, String[] args)
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
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 11-0
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getCheckTriggers(Context context, String[] args) throws Exception
    {
        MapList mapTriggerObjects = new MapList();
        MapList mapFinal = new MapList();
        String strTrigger = "";
        String strNamePattern = "";       
        String strTypePattern = PropertyUtil.getSchemaProperty(context, "type_eServiceTriggerProgramParameters");
        String strVaultPattern = PropertyUtil.getSchemaProperty(context, "vault_eServiceAdministration");
        StringList strTriggerList = new StringList();
        HashMap paraMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String)paraMap.get("objectId");
        HashMap requestMap = (HashMap) paraMap.get("RequestValuesMap");
        String[] emxTableRowId = (String[])requestMap.get("emxTableRowId");
        //String strUIType = (String) paraMap.get("uiType");
        StringList objectSelect = new StringList();
       //Getting the the selected Object Ids from request map.
        String selectedObjectIds = (String)paraMap.get("selectedObjIds");
        String strObjectId,strCurrent,strPolicy,strCommand = "";
        HashSet hsObject = new HashSet();
        DomainObject domainObj = DomainObject.newInstance(context);
        HashMap htemp = new HashMap();

        //Check for SelectedObject Ids if it is null then
        // user has come from life cycle page.
        if(selectedObjectIds!=null)
        {
            StringTokenizer strTokens = new StringTokenizer(selectedObjectIds,",");
            while(strTokens.hasMoreElements())
            {
                hsObject.add((String)strTokens.nextElement());
            }
        }
        else if(emxTableRowId!=null)
        {
            for(int i=0;i<emxTableRowId.length;i++)
            {
                String strRowId = emxTableRowId[i];
                if(strRowId.indexOf("|")>=0)
                {
                    StringList strIdList = FrameworkUtil.split(strRowId,"|");
                    hsObject.add((String)strIdList.get(1));
                }
                else
                {
                    hsObject.add(emxTableRowId[i]);
                }
            }
        }
        else
        {
            if(objectId!=null)
            {   
                hsObject.add(objectId);
            }
        }
        MapList mpFinalObjectList = new MapList();
        Iterator itr = hsObject.iterator();
        while(itr.hasNext())
        {
            //Getting the Related Objects
            StringList infoSelect = new StringList();
            infoSelect.add(DomainConstants.SELECT_CURRENT);
            infoSelect.add(DomainConstants.SELECT_POLICY);
            strObjectId = (String) itr.next();
            domainObj.setId(strObjectId);
            domainObj.open(context);
            Map objInfoMap = domainObj.getInfo(context,infoSelect);
            strCurrent = (String) objInfoMap.get("current");
            strPolicy = (String) objInfoMap.get("policy");
            strCurrent = FrameworkUtil.reverseLookupStateName(context,strPolicy,strCurrent);
            strPolicy = FrameworkUtil.getAliasForAdmin(context,"policy",strPolicy,true);
            String strToDirectionKey = "emxFramework.PromoteTrigger.Validation.To.Rels."+strPolicy+"."+strCurrent;
            String strFromDirectionKey = "emxFramework.PromoteTrigger.Validation.From.Rels."+strPolicy+"."+strCurrent;
            String strToRelationship = EnoviaResourceBundle.getProperty(context, "emxSystem", context.getSession().getLocale(), strToDirectionKey);
            String strFromRelationship = EnoviaResourceBundle.getProperty(context, "emxSystem", context.getSession().getLocale(), strFromDirectionKey);
            StringList relSelect = new StringList();
            strNamePattern = "";
            objectSelect.add(DomainConstants.SELECT_ID);
            objectSelect.add(DomainConstants.SELECT_TYPE);
            StringList strRelationshipList = new StringList();
            String sObjWhere = "current.access[read] == TRUE";
            if(!(strToDirectionKey.equalsIgnoreCase(strToRelationship)) && strToRelationship!=null && !"".equalsIgnoreCase(strToRelationship))
            {
                strRelationshipList = FrameworkUtil.split(strToRelationship,",");
                strToRelationship="";
                for(int i=0;i<strRelationshipList.size();i++)
                {
                    if(i!=0)
                    {
                        strToRelationship +=",";
                    }
                    strToRelationship += PropertyUtil.getSchemaProperty(context, (String)strRelationshipList.get(i));
                }
                MapList mplToRelatedObject = domainObj.getRelatedObjects(context,strToRelationship,"*",objectSelect,relSelect,false,true,(short)1,sObjWhere,null,0);
                mpFinalObjectList.addAll(mplToRelatedObject);
            }
            if(!(strFromRelationship.equalsIgnoreCase(strFromDirectionKey)) && strFromRelationship!=null && !"".equalsIgnoreCase(strFromRelationship))
            {
                strRelationshipList = FrameworkUtil.split(strFromRelationship,",");
                strFromRelationship="";
                for(int i=0;i<strRelationshipList.size();i++)
                {
                    if(i!=0)
                    {
                        strFromRelationship +=",";
                    }
                    strFromRelationship += PropertyUtil.getSchemaProperty(context, (String)strRelationshipList.get(i));
                }
                MapList mplFromRelatedObject = domainObj.getRelatedObjects(context,strFromRelationship,"*",objectSelect,relSelect,true,false,(short)1,sObjWhere,null,0);
                mpFinalObjectList.addAll(mplFromRelatedObject);
            }
            Hashtable hTable = new Hashtable();
            hTable.put("id",strObjectId);
            domainObj.close(context);
            mpFinalObjectList.add(hTable);
        }
        //mplToRelatedObject.add(hTable);


        Iterator objectItr  = mpFinalObjectList.iterator();
        HashSet hsAffectedType = new HashSet();
        while(objectItr.hasNext())
        {
            Map objectMap = (Map) objectItr.next();
            strObjectId =(String) objectMap.get(DomainConstants.SELECT_ID);


            domainObj.setId(strObjectId);
            domainObj.open(context);
            strCurrent = domainObj.getInfo(context,DomainConstants.SELECT_CURRENT);
            strPolicy = domainObj.getInfo(context,DomainConstants.SELECT_POLICY);
            hsAffectedType.add(domainObj.getInfo(context,DomainConstants.SELECT_TYPE));
            String result = MqlUtil.mqlCommand(context, "print policy $1 select $2",strPolicy,"state[" + strCurrent + "].trigger");
            if(!result.equals(""))
            {
                StringList triggerList = FrameworkUtil.split(result.trim(),"\n");

                strTriggerList.removeAllElements();
                for(int j=0;j<triggerList.size();j++)
                {
                    strTrigger =(String) triggerList.get(j);

                    int index = strTrigger.indexOf("PromoteCheck:");
                    if(index>0)
                    {
                        strTriggerList.add(strTrigger.substring(strTrigger.indexOf("(",index)+1,strTrigger.indexOf(")")));
                    }
                }

                MapList mapTemp = new MapList();
                for(int j=0;j<strTriggerList.size();j++)
                {
                    strNamePattern = (String)strTriggerList.get(j);
                    if(strNamePattern.indexOf(" ")>0)
                    {
                        strNamePattern = strNamePattern.replaceAll(" ",",");
                    }
                    
                    mapTemp.addAll(DomainObject.findObjects(context,strTypePattern,strNamePattern,"*","*",strVaultPattern,DomainConstants.SELECT_CURRENT+"==Active",false,objectSelect));
                }
                Iterator itor = mapTemp.iterator();
                // sId is the id of Objects that user have selected.
                // id is the id of Trigger Objects
                while(itor.hasNext())
                {
                    Map map = (Map)itor.next();
                    map.put("sId",domainObj.getId(context));
                    String strTemp = (String) htemp.get((String)map.get("id"));
                    String strTempPolicy,strTempCurrent,strTempName,strTempDesc = "";
                    if(strTemp!=null)
                    {
                        strTemp +="~"+domainObj.getId(context);
                    }
                    else
                    {
                        strTemp = (String) domainObj.getId(context);
                    }
                    htemp.put((String)map.get("id"),strTemp);
                 
                }
            }
         }
        Iterator iterator = (htemp.keySet()).iterator();
        MapList mapList = new MapList();
        while(iterator.hasNext())
        {
            String key = iterator.next().toString();
            String value = htemp.get(key).toString();
            HashMap map = new HashMap();
            key = value + "|" + key;
            map.put("id",key);
            map.put("affectedType", hsAffectedType);
            mapList.add(map);
        }
        mapTriggerObjects.addAll(mapList);
        return mapTriggerObjects;
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


    public Vector getPolicy(Context context, String[] args) throws Exception
    {
        Vector v = new Vector();
        String languageStr = context.getSession().getLanguage();
        HashMap paraMap = (HashMap)JPO.unpackArgs(args);
        MapList objectList =(MapList) paraMap.get("objectList");
        HashMap hMap = new HashMap();
        for(int i=0;i<objectList.size();i++)
        {
            hMap =(HashMap) objectList.get(i);
            StringList strObjectList = FrameworkUtil.split((String)hMap.get("id"),"|");
            strObjectList = FrameworkUtil.split((String)strObjectList.get(0),"~");
            DomainObject doMain = DomainObject.newInstance(context,(String)strObjectList.get(0));
           
           
           String strPolicy = (String)UINavigatorUtil.getAdminI18NString("Policy", (String)doMain.getInfo(context,DomainConstants.SELECT_POLICY), languageStr); 
            v.add(strPolicy);
        }
        return v;
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

    public Vector getState(Context context, String[] args) throws Exception
    {
        Vector v = new Vector();
        HashMap paraMap = (HashMap)JPO.unpackArgs(args);
        String languageStr = context.getSession().getLanguage();
        MapList objectList =(MapList) paraMap.get("objectList");
        HashMap hMap = new HashMap();
        for(int i=0;i<objectList.size();i++)
        {
            hMap =(HashMap) objectList.get(i);
            StringList strObjectList = FrameworkUtil.split((String)hMap.get("id"),"|");
            strObjectList = FrameworkUtil.split((String)strObjectList.get(0),"~");
            DomainObject doMain = DomainObject.newInstance(context,(String)strObjectList.get(0));
            String policyName=(String)doMain.getInfo(context,DomainConstants.SELECT_POLICY);
            String stateName=(String)doMain.getInfo(context,DomainConstants.SELECT_CURRENT);
            String policy_Name= policyName.replace(' ', '_');
            String state_Name= stateName.replace(' ', '_');
            String i18Nstring="emxFramework.State."+policy_Name+"."+state_Name;
            String returnString = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), i18Nstring);
            v.add(returnString);
        }
        return v;
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

    public Vector getName(Context context, String[] args) throws Exception
    {
        Vector v = new Vector();
        HashMap paraMap = (HashMap)JPO.unpackArgs(args);
        MapList objectList =(MapList) paraMap.get("objectList");
        HashMap hMap = new HashMap();
        for(int i=0;i<objectList.size();i++)
        {
            hMap =(HashMap) objectList.get(i);
            StringList strObjectList = FrameworkUtil.split((String)hMap.get("id"),"|");
            strObjectList = FrameworkUtil.split((String)strObjectList.get(0),"~");
            DomainObject doMain = DomainObject.newInstance(context,(String)strObjectList.get(0));
            v.add(doMain.getInfo(context,DomainConstants.SELECT_NAME));
        }
        return v;
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

    public Vector getCheckBoxName(Context context ,String[] args) throws Exception
    {
        Vector v = new Vector();
        HashMap paraMap = (HashMap)JPO.unpackArgs(args);
        MapList objectList =(MapList) paraMap.get("objectList");
        HashMap hMap = new HashMap();
        String prefixCbx = "<input type=\"checkbox\" name=\"emxTableRowId\" value=\"";
        String suffixCbx = "\" onclick=\"doCheckboxClick(this); doSelectAllCheck(this)\">";
        for(int i=0;i<objectList.size();i++)
        {
            hMap =(HashMap) objectList.get(i);
            String strCheckBoxName = (String)hMap.get("sId")+"|"+(String)hMap.get("id");
            v.add(prefixCbx+strCheckBoxName+suffixCbx);
        }
        return v;
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

    public Vector getAffectedType(Context context ,String[] args) throws Exception
    {
        Vector v = new Vector();
        HashMap paraMap = (HashMap)JPO.unpackArgs(args);
        String languageStr = context.getSession().getLanguage();
        MapList objectList =(MapList) paraMap.get("objectList");
        HashMap hMap = new HashMap();
        for(int i=0;i<objectList.size();i++)
        {
            hMap =(HashMap) objectList.get(i);
            HashSet hsAffectType = (HashSet)hMap.get("affectedType");
            StringBuffer sbAffectedType = new StringBuffer();
            Iterator itr = hsAffectType.iterator();
            int j=0;
            while(itr.hasNext())
            {
                if(j!=0)
                {
                    sbAffectedType.append(",");
                }
                //Stringset = (String) itr.next();
                sbAffectedType.append((String) itr.next());
                j++;
            }
            //v.add(sbAffectedType.toString());
            String strPolicy = (String)UINavigatorUtil.getAdminI18NString("Type", sbAffectedType.toString(), languageStr); 
            v.add(strPolicy); 
        }
        return v;
    }
    public Vector getDescription(Context context,String[] args) throws Exception
    {

        Vector v = new Vector();
        HashMap paraMap = (HashMap)JPO.unpackArgs(args);
        MapList objectList =(MapList) paraMap.get("objectList");
        HashMap hMap = new HashMap();
        for(int i=0;i<objectList.size();i++)
        {
            hMap =(HashMap) objectList.get(i);
            String strTemp = (String)hMap.get("id");
            StringList strList = FrameworkUtil.split(strTemp,"|");
            DomainObject doMain = DomainObject.newInstance(context,(String)strList.get(1));
            String strDescription = doMain.getInfo(context,DomainConstants.SELECT_DESCRIPTION);
            if(strDescription==null || strDescription.length()==0)
            {
                strDescription = doMain.getInfo(context,DomainConstants.SELECT_REVISION);
            }
            v.add(strDescription);
        }
        return v;
    }
	public Vector getErrorType(Context context, String[] args) throws Exception
	{
		Vector v = new Vector();
        String languageStr = context.getSession().getLanguage();
		HashMap paraMap = (HashMap)JPO.unpackArgs(args);
		MapList objectList =(MapList) paraMap.get("objectList");
		for(int i=0;i<objectList.size();i++)
		{
			String returnString = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(languageStr), "emxFramework.TriggerValidationReport.Error");
            v.add(returnString);
		}
		return v;
    }
}
