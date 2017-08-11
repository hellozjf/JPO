/*
**  emxTriggerReportBase
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.*;
import matrix.util.*;
import java.util.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.domain.DomainObject;

/**
 * The <code>emxTriggerReportBase</code> class contains methods for Trigger Tool
 *
 * @version AEF 11-0 - Copyright (c) 2005, MatrixOne, Inc.
 */

public class emxTriggerReportBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 11-0
     */

    public emxTriggerReportBase_mxJPO(Context context, String[] args)
      throws Exception
    {
        super(context, args);
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
    * This method returns the list of triggers defined for Admin Object
    *
    *  By iterating each trigger of a selected admin object, it find
    *  all the revision objects and finally stores state(if admin type is policy),
    *  trigger name and trigger type in MapList.
    *
    * @param context the eMatrix <code>Context</code> object
    *
    * @return <code>Vector</code> object containing states
    * @throws Exception if the operation fails
    * @since AEF 11-0
    */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getListOfTriggers(Context context, String args[]) throws Exception
    {
        MapList mapList = new MapList();

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String adminObjectType = (String)paramMap.get("adminObjectType");
        String adminObjectName = (String)paramMap.get("adminObjectName");

        ArrayList absTriggers = null;

        if("Policy".equals(adminObjectType)) {
            absTriggers = TriggerUtil.getPolicyTriggersList(context, adminObjectName);
        }else {
            absTriggers = TriggerUtil.getTriggersList(context,adminObjectType,adminObjectName);
        }

        MapList mapListRev = null;
        Map map = null;
        for(Iterator trigItr = absTriggers.iterator();trigItr.hasNext();)
        {
            String currentTrigStr = (String)trigItr.next();

            StringList trigList = FrameworkUtil.split(currentTrigStr,";");

            int size = trigList.size();

            if(!"".equals((String)(trigList.get(size-1))))
            {
                mapListRev = TriggerUtil.getRevisionInfo(context,(String)(trigList.get(size-1)));

                int mapListSize = 0;

                if(mapListRev != null && (mapListSize = mapListRev.size()) > 0)
                {

                    for(int i = 0 ; i < mapListSize ; i++)
                    {
                        map = (Map)mapListRev.get(i);

                        int k = 0;

                        if("Policy".equals(adminObjectType))
                        {
                            map.put("STATE",(String)trigList.get(k));
                            k++;
                        }
                        map.put("TRIGGERNAME",(String)trigList.get(k));
                        k++;
                        map.put("TRIGGERTYPE",(String)trigList.get(k));
                        k++;
                        if("Type".equalsIgnoreCase(adminObjectType)) {
                            map.put("INHERITED",(String)trigList.get(k));
                        }
                        mapList.add(map);
                    }
                }
            }

        }
        return mapList;
    }



    /**
    * This method returns the states for Policy Admin Object - for trigger tool
    *
    * @param context the eMatrix <code>Context</code> object
    *
    * @return <code>Vector</code> object containing states
    * @throws Exception if the operation fails
    * @since AEF 11-0
    */

    public Vector getStateOfPolicies(Context context,String[] args) throws Exception
    {
        Vector vectorOfStates = new Vector();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        Map paramList = (Map)paramMap.get("paramList");

        String strPolicyName = (String)paramList.get("adminObjectName");
        String languageStr = (String)paramList.get("languageStr");

        MapList objectList = (MapList)paramMap.get("objectList");

        int listSize = 0;
        if(objectList != null && (listSize = objectList.size()) > 0)
        {
            Map map = null;
            for(int i = 0 ; i < listSize ; i++)
            {
                map = (Map)objectList.get(i);
                vectorOfStates.add(i18nNow.getStateI18NString(strPolicyName,(String)map.get("STATE"),languageStr));
            }
        }

        return vectorOfStates;

    }

    /**
    * This method will decide whether to show States column or Not.
    * Only for Policy admin object this column should be displayed.
    *
    * @param context the eMatrix <code>Context</code> object
    *
    * @return <code>boolean</code> value for showing state column
    * @throws Exception if the operation fails
    * @since AEF 11-0
    */
    public boolean showStatesColumn(Context context , String args[]) throws Exception
    {
        boolean returnValue = false;

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String adminObjectType = (String)paramMap.get("adminObjectType");

        if("Policy".equals(adminObjectType))
        {
            returnValue = true;
        }

        return returnValue;
    }


    /**
    * This method will decide whether to show the inherited column or not
    * Inherited column should be displayed only when admin type is "TYPE"
    *
    * @param context the eMatrix <code>Context</code> object
    *
    * @return <code>boolean</code> value for showing inherited column
    * @throws Exception if the operation fails
    * @since AEF 11-0
    */
    public boolean showInheritedColumn(Context context , String args[]) throws Exception
    {
        boolean returnValue = false;

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String adminObjectType = (String)paramMap.get("adminObjectType");

        if("Type".equals(adminObjectType))
        {
            returnValue = true;
        }

        return returnValue;
    }


    /**
    * If the trigger is inherited, value added is "Yes" and if it is not,
    * then value added is "No".
    *
    * @param context the eMatrix <code>Context</code> object
    *
    * @return <code>Vector</code> value for showing inherited column
    * @throws Exception if the operation fails
    * @since AEF 11-0
    */
   public Vector isInheritedTrigger(Context context, String args[]) throws Exception
    {
        Vector vectorInherited = new Vector();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        Map paramList = (Map)paramMap.get("paramList");
        String languageStr = (String)paramList.get("languageStr");
        MapList objectList = (MapList)paramMap.get("objectList");

        int listSize = 0;
        if(objectList != null && (listSize = objectList.size()) > 0)
        {
            Map map = null;
            for(int i = 0 ; i < listSize ; i++)
            {
                map = (Map)objectList.get(i);
                vectorInherited.add(EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.TriggerReport."+(String)map.get("INHERITED"), new Locale(languageStr)));                
            }
        }


        return vectorInherited;
    }

    /**
    * This method returns the Trigger Names for Admin Objects
    *
    * @param context the eMatrix <code>Context</code> object
    *
    * @return <code>Vector</code> object containing Trigger Names
    * @throws Exception if the operation fails
    * @since AEF 11-0
    */
    public Vector getTriggerName(Context context, String args[]) throws Exception
    {
        Vector vectorTrigNames = new Vector();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);

        MapList objectList = (MapList)paramMap.get("objectList");

        int listSize = 0;
        if(objectList != null && (listSize = objectList.size()) > 0)
        {
            Map map = null;
            for(int i = 0 ; i < listSize ; i++)
            {
                map = (Map)objectList.get(i);
                vectorTrigNames.add((String)map.get("TRIGGERNAME"));
            }
        }

        return vectorTrigNames;
    }


    /**
    * This method returns the Trigger Types for Admin Objects
    *
    * @param context the eMatrix <code>Context</code> object
    *
    * @return <code>Vector</code> object containing Trigger Types
    * @throws Exception if the operation fails
    * @since AEF 11-0
    */
    public Vector getTriggerType(Context context, String args[]) throws Exception
    {
        Vector vectorTrigTypes = new Vector();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        Map paramList = (Map)paramMap.get("paramList");

        String languageStr = (String)paramList.get("languageStr");

        MapList objectList = (MapList)paramMap.get("objectList");

        int listSize = 0;
        if(objectList != null && (listSize = objectList.size()) > 0)
        {
            Map map = null;
            for(int i = 0 ; i < listSize ; i++)
            {
                map = (Map)objectList.get(i);
                vectorTrigTypes.add(EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.TriggerReport."+(String)map.get("TRIGGERTYPE"), new Locale(languageStr)));
                
            }
        }

        return vectorTrigTypes;
    }

  /**
    * This method returns the Trigger Description(purpose) for Admin Objects
    *
    * @param context the eMatrix <code>Context</code> object
    *
    * @return <code>Vector</code> object containing Trigger purpose
    * @throws Exception if the operation fails
    * @since AEF 11-0
    */
    public Vector getTriggerPurpose(Context context, String args[]) throws Exception
    {
        Vector vectorTrigPurpose = new Vector();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        Map paramList = (Map)paramMap.get("paramList");

        String languageStr = (String)paramList.get("languageStr");

        MapList objectList = (MapList)paramMap.get("objectList");
        int listSize = 0;
        String []objectIdArray = null;
        
        //Collect object id's into string []
        if(objectList != null && (listSize = objectList.size()) > 0)
        {
            Map map = null;
            objectIdArray = new String[listSize];
            for(int i = 0 ; i < listSize ; i++)
            {
                map = (Map)objectList.get(i);
                objectIdArray[i] = (String)map.get(SELECT_ID);
            }
        }
        
        //get description of all the triggers
        if(objectIdArray != null && objectIdArray.length > 0) {
            StringList selectList = new StringList();
            selectList.add(SELECT_DESCRIPTION);

            MapList descriptionList = DomainObject.getInfo(context, objectIdArray, selectList);

            if(descriptionList != null && (listSize = descriptionList.size()) > 0) {
                Map map = null;
                for(int i = 0 ; i < listSize; i++) {
                    map = (Map)descriptionList.get(i);
                    vectorTrigPurpose.add(EnoviaResourceBundle.getFrameworkStringResourceProperty(context, (String)map.get(SELECT_DESCRIPTION), new Locale(languageStr)));
                    
                }
            }
        }

        return vectorTrigPurpose;
    }
}
