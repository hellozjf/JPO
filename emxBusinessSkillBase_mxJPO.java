/*   emxBusinessSkillBase
**
**   Copyright (c) 2003-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the implementation of emxBusinessSkill
**
*/


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.PolicyList;
import matrix.db.RelationshipType;
import matrix.db.SelectConstants;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>emxBusinessSkillBase</code> class represents the BusinessSkill JPO
 * functionality for the BusinessSkill
 *
 * @version Common 10.5.1.2 - Copyright (c) 2004, MatrixOne, Inc.
 */

public class emxBusinessSkillBase_mxJPO extends emxDomainObject_mxJPO
{

    public static final String RELATIONSHIP_SUBSKILL =
            PropertyUtil.getSchemaProperty("relationship_SubSkill");

    public static final String RELATIONSHIP_ORGANIZATIONSKILL =
            PropertyUtil.getSchemaProperty("relationship_OrganizationSkill");


    public static final String RELATIONSHIP_HASBUSINESSSKILL =
            PropertyUtil.getSchemaProperty("relationship_hasBusinessSkill");

    public static final String ATTRIBUTE_COMPETENCY =
         PropertyUtil.getSchemaProperty("attribute_Competency");

    public static final String ATTRIBUTE_EXPERIENCE =
         PropertyUtil.getSchemaProperty("attribute_Experience");

    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @version Common 10.5.1.2
     * @grade 0
     */
    public emxBusinessSkillBase_mxJPO (Context context, String[] args)
        throws Exception
    {

        super(context, args);

    }

    /**
     * Executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer: 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */
    public int mxMain(Context context, String[] args) throws Exception {
      if(!context.isConnected()) {

       throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
      }

      return 0;
     }

    /**
     * Gets the Company information of the BusinessSkill
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    0 - objectId of the BusinessSkill
     * @return MapList containing information of the company
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */
    public static MapList GetCompanyInfo(Context context, String[] args) throws Exception
    {
        //get the id of the BusinessSkill
        String sObjectId = args[0];
        DomainObject objDomain = new DomainObject(sObjectId);

        //get the expand criteria
        StringList busSelects = new StringList(2);
        busSelects.add(SELECT_ID);
        busSelects.add(SELECT_NAME);
        busSelects.add(SELECT_TYPE);

        String sRelPattern = RELATIONSHIP_SUBSKILL + "," + RELATIONSHIP_ORGANIZATIONSKILL;

        String sTypePattern = DomainConstants.TYPE_COMPANY;

        Pattern patternType = new Pattern(sTypePattern);

        MapList mapCompany = objDomain.getRelatedObjects(context,sRelPattern, "*", busSelects, null, true, false, (short)0, "", "", patternType, null, null);

        return mapCompany;
    }

    /**
     * Check to see if the Person is a Company Representative
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sCompanyId contains the Company Id
     * @param sPersonId contains the Person Id
     * @return String: Yes if the person is a company representative and No otherwise
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */
    protected static String IsPersonCompanyEmployee(Context context, String sCompanyId, String sPersonId) throws Exception
    {
        String sEmployee = "No";

        DomainObject person = new DomainObject(sPersonId);
        String SELECT_USER_COMPANY_ID = "to[" + PropertyUtil.getSchemaProperty(context, "relationship_Employee") + "].from.id";

        String sPersonCompanyId = person.getInfo(context, SELECT_USER_COMPANY_ID);

        if (sPersonCompanyId != null &&  sPersonCompanyId.equals(sCompanyId))
        {
            sEmployee = "Yes";
        }

        return sEmployee;
    }

    /**
     * Check to see if the BusinessSkill name is unique
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    0 - objectId of the BusinessSkill
     *    1 - name of the BusinessSkill
     * @return boolean: true if skill name is not unique and false otherwise
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */
    public static boolean CheckForUniqueSkill(Context context, String[] args ) throws Exception
    {
        boolean bFound = false;
        String sParentId = args[0];
        String sName = args[1];

        DomainObject objParent = new DomainObject(sParentId);

        //get the expand criteria
        StringList busSelects = new StringList(2);
        busSelects.add(SELECT_NAME);

        String sRelPattern = RELATIONSHIP_SUBSKILL + "," + RELATIONSHIP_ORGANIZATIONSKILL;

        String sBusWhere = "name==\"" + sName + "\"";


        MapList mapChild = objParent.getRelatedObjects(context,sRelPattern, "*", busSelects, null, false, true, (short)1, sBusWhere, "", null, null, null);

        if (mapChild != null && (mapChild.size() > 0))
        {
            bFound = true;
        }
        return bFound;
    }

    /**
     * Return the persons associated with the BusinessSkill
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectId - the context BusinessSkill object
     * @return MapList containing persons associated with the BusinessSkill
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList ListBusinessSkillPersons(Context context, String[] args)
        throws Exception
    {
        MapList objectList = new MapList();


        //get the id of the BusinessSkill

        Map map = (Map) JPO.unpackArgs(args);
        String sObjectId = (String) map.get("objectId");

        StringList busSelects = new StringList(1);
        busSelects.add(DomainObject.SELECT_ID);

        StringList relSelects = new StringList(1);
        relSelects.add(DomainRelationship.SELECT_ID);
        
        DomainObject objSkill = new DomainObject(sObjectId);
        MapList mapPersons = objSkill.getRelatedObjects(context,
                                        RELATIONSHIP_HASBUSINESSSKILL, "*", 
                                        busSelects, relSelects, 
                                        true, false, 
                                        (short)0, 
                                        "", "",
                                        0,
                                        null, null, null);

        //get the company information
        String sArgs[] = {sObjectId};
        MapList mapCompanyInfo = GetCompanyInfo(context,sArgs );
        Iterator mapCompanyItr = mapCompanyInfo.iterator();

        String sCompanyId = "";
        while (mapCompanyItr.hasNext() )
        {

           Map Company = (Map)mapCompanyItr.next();
           sCompanyId = (String)Company.get(DomainObject.SELECT_ID);
           break;
        }

        String roleResourceManager = PropertyUtil.getSchemaProperty(context, "role_ResourceManager");
        boolean hasEditAccess = PersonUtil.getAssignments(context).contains(roleResourceManager);
        String rowEditable = hasEditAccess ? "show" :"readonly";

        Iterator mapPersonsItr = mapPersons.iterator();
        
        while (mapPersonsItr.hasNext() )
        {

           Map item = (Map)mapPersonsItr.next();

           String sPersonId = (String)item.get(DomainObject.SELECT_ID);
           String sPersonRelId = (String)item.get(DomainRelationship.SELECT_ID);

           HashMap mapObject = new HashMap();
           mapObject.put("id",sPersonId);
           mapObject.put("id[connection]",sPersonRelId);
           mapObject.put("Employee", IsPersonCompanyEmployee(context,sCompanyId, sPersonId));
           mapObject.put("RowEditable", rowEditable);
           objectList.add(mapObject);

        }

        return  objectList;
    }
    //Added:14-July-10:ms9:R210:PRG: IR-057124V6R2011x
    /**
     * Return the Business Skills associated with the person
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectId - the context BusinessSkill object
     * @return String containing BusinessSkill associated with the persons 
     * @throws Exception if the operation fails
     * @since since R210
     */
    public String getBusinessSkills(Context context, String[] args)
    throws Exception
    {
    	 List featList = new StringList(args[0]);
         boolean isMatrixSearch = false;
         if(args.length > 1) 
         {
             isMatrixSearch = "MATRIXSEARCH".equalsIgnoreCase(args[1]);
         }

         DomainObject domObj = new DomainObject(args[0]);
         StringList objSelects = new StringList();
         objSelects.addElement(SELECT_ID);
         objSelects.addElement(SELECT_TYPE);
         objSelects.addElement(SELECT_NAME);
         objSelects.addElement(SELECT_REVISION);
         
         String relpattern=PropertyUtil.getSchemaProperty(context,"relationship_hasBusinessSkill");
         StringList selectRelStmts=new StringList();
         selectRelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
         String typepattern=PropertyUtil.getSchemaProperty(context,"type_BusinessSkill");

         MapList businessSkillList = domObj.getRelatedObjects(context, relpattern,typepattern,objSelects,selectRelStmts,false,true,(short)1,null,null);
         
         String delimiter = null;
         if(isMatrixSearch)
         {
        	 delimiter = "|";
         }
         else
         {
        	 delimiter = SelectConstants.cSelectDelimiter;
         }
         
         StringList idList = new StringList();
         for(int jj=0;jj<businessSkillList.size();jj++)
         {	
        	 Map objMap = (Map) businessSkillList.get(jj);
        	 idList.add((String)objMap.get("name"));
         }
         StringBuffer BSBuff = new StringBuffer();
         for (Iterator BS = idList.iterator(); BS.hasNext(); )
         {
            String name = (String) BS.next();
            BSBuff.append(BSBuff.length() == 0? name:delimiter + name);
         }
         return(BSBuff.toString());
 	}
 
    
    /**
     * Return the range value of Business Skills having that many number of persons associated with it.
     *
     * @param context the eMatrix Context object
     * @param args[]
     * @return Map containing range for BusinessSkill
     * @throws Exception if the operation fails
     * @since since R210
     */
    public static Map getRangeValuesForBusinessSkills(Context context, String args[])
    throws Exception
    {
		Map argMap = (Map) JPO.unpackArgs(args);
		String currentField = (String) argMap.get("currentField");
		Map fieldValues = (Map) argMap.get("fieldValues");
		Map requestMap = (Map) argMap.get("requestMap");
		String strObjectId = (String) requestMap.get("objectId");

		//////
		DomainObject domObj = new DomainObject(strObjectId);
		strObjectId= domObj.getInfo(context,"to[" + "Company Project" + "].from.id");
		DomainObject CompDomObj = new DomainObject(strObjectId);
		//////
		
        StringList expandSelects = new StringList();
	    expandSelects.addElement(SELECT_ID);
	    expandSelects.addElement(SELECT_TYPE);
	    String expandTypes = TYPE_PERSON;
	       
	    Map returnMap = new HashMap();
	    String vaultString = QUERY_WILDCARD;
	    String whereString = "";
	       
	    MapList resultsList = null;
	    MapList forRemainingBusinessSkills = null;
	   	String selectable = null;
	   	
	    if ("PRG_BUSINESS_SKILL".equals(currentField)) {
	    	selectable = "program[emxBusinessSkill -method getBusinessSkills ${OBJECTID}]";
	    }
	    else {
            String[] formatArgs = {currentField};
            String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.BusinessSkill.UnSupportedField",formatArgs);
	    	throw new RuntimeException(message);
	    }
	    
	    SelectList resultSelects = new SelectList();
	    resultSelects.add(CompDomObj.SELECT_ID);
	    resultSelects.add(selectable);
	    resultsList = CompDomObj.findObjects(context, TYPE_PERSON, vaultString, whereString, resultSelects);
	    
	    SelectList newResultSelects = new SelectList();
	    newResultSelects.add(CompDomObj.SELECT_ID);
	    newResultSelects.add(CompDomObj.SELECT_NAME);
	    forRemainingBusinessSkills = CompDomObj.findObjects(context, TYPE_BUSINESS_SKILL, vaultString, whereString, newResultSelects);
		
	    StringList BSList=new StringList();
	    for(int i=0;i<forRemainingBusinessSkills.size();i++)
	    {
	    	Map map = (Map) forRemainingBusinessSkills.get(i);
	    	BSList.addElement((String)map.get("name"));
	    }
	    
      	Iterator itr = resultsList.iterator();
		while(itr.hasNext())
		{
	       HashMap objectMap = (HashMap)itr.next();
	       String retKey = (String)objectMap.get(selectable);
	       if (retKey == null || "".equals(retKey)) 
	       {
	            continue;
	       }
           StringList businessSkillList = FrameworkUtil.split(retKey, SelectConstants.cSelectDelimiter);

           if(retKey.indexOf(SelectConstants.cSelectDelimiter) == -1)
	       {
	        	   businessSkillList = FrameworkUtil.split(retKey, "|");
           }

           for (Iterator itrProductName = businessSkillList.iterator(); itrProductName.hasNext();) 
           {
               String businessSkillName = (String) itrProductName.next();
	           if (!returnMap.containsKey(businessSkillName)) 
	           {
	                   returnMap.put(businessSkillName, new Integer(1));
	           }
	           else 
	           {
	                   Integer count = (Integer)returnMap.get(businessSkillName);
	                   count = new Integer(count.intValue() + 1);
	                   returnMap.put(businessSkillName, count);
	               }
	           }
	       }

	       for (Iterator itrBusinessSkillName = returnMap.keySet().iterator(); itrBusinessSkillName.hasNext();) 
	       {
	           String BSName = (String) itrBusinessSkillName.next();
	           Integer count = (Integer)returnMap.get(BSName);

	           returnMap.put(BSName, BSName + " (" + count + ")");
	           for (int indexx=0;indexx<BSList.size();indexx++)
	           {
	        	   if(BSName.equals(BSList.get(indexx)))
	        	   {
	        		   BSList.remove(indexx);
	        	   }
	           }
	       }
	       
	       for(int index1=0;index1<BSList.size();index1++)
	       {
	    	   returnMap.put(BSList.get(index1), BSList.get(index1) + " (0)");
	       }

	       return returnMap;
	}
    ////End:ms9:R210:PRG: IR-057124V6R2011x

    /**
     * Returns the Employee id
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    0 - HashMap containing Map entries for the key "paramList" and "objectList"
     * @return Object of type Vector
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */
    public Object retrieveEmployeeDate(Context context, String[] args)
        throws Exception
    {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get("paramList");

        MapList mlObject = (MapList)programMap.get("objectList");

        Vector vEmployee = new Vector();

        String sEmployee = "";

        if ( mlObject != null)
        {

            int iObject  = mlObject.size();

            for (int i = 0; i < iObject; i++)
            {

                Map valueMap = (Map) mlObject.get(i);
                sEmployee      = (String) valueMap.get("Employee");
                vEmployee.add(sEmployee);

            }
        }

        return vEmployee;
    }

    /**
     * Deletes the BusinessSkill object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    0 - objectId of the BusinessSkill
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */
    public static void DeleteBusinessSkills(Context context, String[] args) throws Exception
    {
        StringList sDeleteIdList = new StringList();
        if (args != null && (args.length > 0 ))
        {
            for (int iCount = 0 ; iCount < args.length ; ++iCount)
            {
                String sBusId = args[iCount];
                DomainObject objDomain = new DomainObject(sBusId);
                //get the expand criteria
                StringList busSelects = new StringList(1);
                busSelects.add(DomainObject.SELECT_ID);

                MapList mapSkill = objDomain.getRelatedObjects(context,RELATIONSHIP_SUBSKILL, "*", busSelects, null, false, true, (short)0, "", "", null, null, null);

                mapSkill.sort(DomainObject.SELECT_LEVEL, "descending",  "integer");
                if (mapSkill.size() > 0) {
                    Iterator itr = mapSkill.iterator();
                    while (itr.hasNext()) {
                        Map map = (Map) itr.next();
                        String sSkillId = (String) map.get(DomainObject.SELECT_ID);

                        // ensure there are no duplicate ids

                        if (!sDeleteIdList.contains(sSkillId)) {
                            sDeleteIdList.add(sSkillId);
                        }
                    }
                }
                if (!sDeleteIdList.contains(sBusId)) {
                    sDeleteIdList.add(sBusId);
                }
            }
        }

          // start transaction
          ContextUtil.startTransaction(context, true);
          for (int i = 0; i < sDeleteIdList.size(); i++)
          {
              MqlUtil.mqlCommand(context,"delete bus $1", (String) sDeleteIdList.get(i));
          }
          ContextUtil.commitTransaction(context);
    }

    /**
     * Gets the parent skill of the business skill
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectId - the context BusinessSkill object
     * @return Object of type StringList
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */
    public static Object getParentSkill(Context context, String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map requestMap  = (Map) programMap.get("requestMap");
        i18nNow i18nnow = new i18nNow();
        String strLanguage = (String)requestMap.get("languageStr");
        String parentSkill = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(), "emxComponents.BusinessSkill.ParentSkillLevel");

        //Added:16-Mar-09:oef:R207:PRG Advanced Resource Planning
        String strMode = (String)requestMap.get("mode");
        final String SELECT_KINDOF_BUSINESS_SKILL = "type.kindof[" + TYPE_BUSINESS_SKILL + "]";
        //End:R207:PRG Advanced Resource Planning
        
        String PARENT_SKILL_NAME = "to["+RELATIONSHIP_SUBSKILL+"].from.name";
        
        String  objectId = (String) requestMap.get("objectId");

        //String  objectId = (String) paramMap.get("objectId");
        DomainObject objDomain = new DomainObject(objectId);

        StringList busSelects = new StringList();
        busSelects.add(objDomain.SELECT_ID);
        //Added:16-Mar-09:oef:R207:PRG Advanced Resource Planning
        busSelects.add(objDomain.SELECT_NAME);
        busSelects.add(SELECT_KINDOF_BUSINESS_SKILL);
        //End:R207:PRG Advanced Resource Planning
        busSelects.add(PARENT_SKILL_NAME);
        Map parentInfo = objDomain.getInfo(context, busSelects);
        
        //Modified:16-Mar-09:oef:R207:PRG Advanced Resource Planning
        if ("create".equals(strMode)) {
            if ("TRUE".equalsIgnoreCase((String)parentInfo.get(SELECT_KINDOF_BUSINESS_SKILL))) {
                parentSkill = (String)parentInfo.get(objDomain.SELECT_NAME);
            }
        }
        else 
        {
            if(parentInfo.get(PARENT_SKILL_NAME) != null)
            {
                parentSkill = parentInfo.get(PARENT_SKILL_NAME).toString();
            }
        }
        //End:R207:PRG Advanced Resource Planning

        StringList fieldValues = new StringList();
        
        if(objectId != null) 
        {
            fieldValues.addElement(parentSkill);
        }

        return fieldValues;
    }

    /**
     * Return the business skills associated with the person
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectId - the context BusinessSkill object
     * @return MapList containing business skills
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList ListPersonBusinessSkills(Context context, String[] args)
        throws Exception
    {
        //get the id of the BusinessSkill

        Map map = (Map) JPO.unpackArgs(args);
        String sObjectId = (String) map.get("objectId");

        DomainObject objSkill = new DomainObject(sObjectId);

        StringList busSelects = new StringList(1);
        busSelects.add(DomainObject.SELECT_ID);
        
        StringList relSelects = new StringList(3);
        relSelects.add(DomainRelationship.SELECT_ID);

        MapList mapSkills = objSkill.getRelatedObjects(context, 
                                     RELATIONSHIP_HASBUSINESSSKILL, 
                                     "*", 
                                     busSelects, relSelects, 
                                     false, true, 
                                     (short)1, 
                                     "", "",
                                     0,
                                     null, null, null);
        String roleResourceManager = PropertyUtil.getSchemaProperty(context, "role_ResourceManager");
        boolean hasEditAccess = PersonUtil.getAssignments(context).contains(roleResourceManager);
        for (int i = 0; !hasEditAccess && i < mapSkills.size(); i++) {
            Map skill = (Map) mapSkills.get(i);
            skill.put("RowEditable", "readonly");
        }

        return  mapSkills;
    }

    /**
     * Checks if a BusinessSkill is a Parent skill
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectId - the context BusinessSkill object
     * @return boolean true if it is a parent skill
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */
    public static boolean CheckForParentSkill(Context context, String[] args) throws Exception
    {
        boolean bDontHaveChild = true;

        Map map = (Map) JPO.unpackArgs(args);
        String sObjectId = (String) map.get("objectId");
        DomainObject objParent = new DomainObject(sObjectId);

        //get the expand criteria
        StringList busSelects = new StringList(2);
        busSelects.add(SELECT_NAME);

        String sRelPattern = RELATIONSHIP_SUBSKILL ;


        MapList mapChild = objParent.getRelatedObjects(context,sRelPattern, "*", busSelects, null, false, true, (short)1, "", "", null, null, null);

        if (mapChild != null && (mapChild.size() > 0))
        {
            bDontHaveChild = false;
        }
        return bDontHaveChild;
    }

    /**
     * Check to see if a subskill can be added to a Parent skill
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    0 - objectId of the BusinessSkill
     * @return boolean true if a subskill can be added to a parent
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */
    public static boolean CanAddSubSkill(Context context, String[] args) throws Exception
    {
        boolean bCanAddSkill = true;

        String sObjectId = args[0];
        DomainObject objParent = new DomainObject(sObjectId);

        //get the expand criteria
        StringList busSelects = new StringList(1);
        busSelects.add(SELECT_NAME);

        String sRelPattern = RELATIONSHIP_HASBUSINESSSKILL ;


        MapList mapPersons = objParent.getRelatedObjects(context,sRelPattern, "*", busSelects, null, true, false, (short)1, "", "", null, null, null);

        if (mapPersons != null && (mapPersons.size() > 0))
        {
            bCanAddSkill = false;
        }
        return bCanAddSkill;
    }

    /**
     * Connects a SubSkill to the Parent skill
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    0 - objectId of Parent skill
     *    1 - objectId of Sub skill
     * @throws Exception if the operation fails
     * @since Common 10.5.1.2
     */
    public static void ConnectSubSkill(Context context, String[] args) throws Exception
    {

        String sParentId = args[0];
        String sSubSkillId = args[1];

        DomainObject objParent = new DomainObject(sParentId);

        //get the expand criteria
        StringList busSelects = new StringList(2);
        busSelects.add(SELECT_NAME);
        busSelects.add(SELECT_ID);

        String sRelPattern = RELATIONSHIP_HASBUSINESSSKILL ;

        StringList relSelects = new StringList(3);
        relSelects.add(DomainRelationship.SELECT_ID);
        relSelects.add("attribute[Competency]" );
        relSelects.add("attribute[Experience]" );


        MapList mapPersons = objParent.getRelatedObjects(context,sRelPattern, "*", busSelects, relSelects, true, false, (short)1, "", "", null, null, null);

        if (mapPersons != null && (mapPersons.size() > 0))
        {
            Iterator itr = mapPersons.iterator();
            while (itr.hasNext()) {
                Map map = (Map) itr.next();
                String sPersonId = (String) map.get(DomainObject.SELECT_ID);
                DomainObject objPerson = new DomainObject(sPersonId);

                RelationshipType reltype =new RelationshipType(RELATIONSHIP_HASBUSINESSSKILL);
                DomainRelationship newRel =  objPerson.addToObject(context,reltype, sSubSkillId);

                 Map attribMap = new HashMap();
                 attribMap.put(ATTRIBUTE_COMPETENCY, (String) map.get("attribute[Competency]"));
                 attribMap.put(ATTRIBUTE_EXPERIENCE, (String) map.get("attribute[Experience]"));

                 newRel.setAttributeValues(context, attribMap);

                 //disconnect the relationship between parent and the person
                 DomainRelationship.disconnect(context, (String) map.get(DomainRelationship.SELECT_ID));
            }



        }

    }
    
 // Added:16-Mar-09:oef:R208:PRG Advanced Resource Planning
 /**
  * Used to connect the newly created business skill to Organization type
  * 
  * @param context Object
  * @param args String array
  * @throws Exception
  */
    
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void createPostProcess(Context context,String[] args) throws Exception
    {
        Map map = (Map) JPO.unpackArgs(args);
        Map mapParamMap = (Map) map.get("paramMap");
        Map mapRequestMap = (Map) map.get("requestMap");
        String strObjectId = (String) mapRequestMap.get("objectId");
        String strNewObjectId = (String) mapParamMap.get("newObjectId");
        
        DomainObject dmoObject = DomainObject.newInstance(context,strObjectId);
        DomainObject dmoNewObject = DomainObject.newInstance(context,strNewObjectId);

        //This code is written for updating revision of business skill.
        //This will enable each company must have unique business skill
        //Update Revision:Start
        String strName = (String) mapRequestMap.get("Name");
        String strParentOID = (String)mapRequestMap.get("parentOID");
		String sCommandStatement = "print bus $1 select $2 dump $3";
		String strRevision =  MqlUtil.mqlCommand(context, sCommandStatement,strParentOID, "physicalid", "|"); 
        sCommandStatement = "modify bus $1 name $2 revision $3";
        MqlUtil.mqlCommand(context, sCommandStatement,strNewObjectId,strName,strRevision);
        //Update Revision:End
        
        if(dmoObject.isKindOf(context,DomainConstants.TYPE_ORGANIZATION))
        {
            DomainRelationship.connect(context,dmoObject,RELATIONSHIP_ORGANIZATION_SKILL,dmoNewObject);
        }
        else if(dmoObject.isKindOf(context,TYPE_BUSINESS_SKILL))
        {
            DomainRelationship.connect(context,dmoObject,RELATIONSHIP_SUBSKILL,dmoNewObject);
        }
    }
    /**
     * Expands Child Business Skills in table "APPBusinessSkillSummary" with relationship SubSkill
     * 
     * @param context Matrix Context object
     * @param args Packed arguments sent by UITable Component
     * @return MapList containg Maps of each children Business Skill.Each map contains ID of each Business Skill
     * @throws Exception
     */ 
    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableExpandChildBusinessSkillData(Context context,String[] args) throws Exception
    {
        final String RELATIONSHIP_SUBSKILLS = PropertyUtil.getSchemaProperty(context, "relationship_SubSkill");
        final String SELECT_SUBSKILL_IDS = "from[" + RELATIONSHIP_SUBSKILLS + "].to.id";
        Map programMap = (Map)JPO.unpackArgs(args);
        String strObjectId = (String) programMap.get("objectId");
        String strExpandFilter = (String) programMap.get("emxExpandFilter");
        
        if (strExpandFilter == null || "".equals(strExpandFilter) )
        {
            strExpandFilter = "1";
        }
        if ("All".equals(strExpandFilter) )
        {
            strExpandFilter = "0";
        }
     
        MapList mlRelatedObjects = getSubSkill(context, 
				strObjectId, strExpandFilter);

        StringList slValue = null;
        Object objValue = null;
        for (Iterator itrObjects = mlRelatedObjects.iterator(); itrObjects.hasNext();)
        {
            Map mapObject = (Map) itrObjects.next();
            
            objValue = mapObject.get(SELECT_SUBSKILL_IDS);
            if (objValue == null)
            {
                mapObject.put("SubSkillCount", "0");
            }
            else if (objValue instanceof String)
            {
                mapObject.put("SubSkillCount", "1");
            }
            else if (objValue instanceof StringList)
            {
                slValue = (StringList)objValue;
                mapObject.put("SubSkillCount", String.valueOf(slValue.size()));
            }
        }
         
        return mlRelatedObjects;
    }

	/**
	 * @param context
	 * @param SELECT_SUBSKILL_IDS
	 * @param strObjectId
	 * @param strExpandFilter
	 * @return
	 * @throws FrameworkException
	 */
	private MapList getSubSkill(Context context,
			String strObjectId,
			String strExpandFilter) throws FrameworkException 
	{
		final String RELATIONSHIP_SUBSKILLS = PropertyUtil.getSchemaProperty(context, "relationship_SubSkill");
		final String SELECT_SUBSKILL_IDS = "from[" + RELATIONSHIP_SUBSKILLS + "].to.id";
        short recurseToLevel = Short.parseShort(strExpandFilter);
        DomainObject dmoObject = DomainObject.newInstance(context, strObjectId);
        String strRelationshipPattern = null;
        String strTypePattern = TYPE_BUSINESS_SKILL;
        
        if(dmoObject.isKindOf(context,DomainConstants.TYPE_ORGANIZATION))
        {
            strRelationshipPattern =  RELATIONSHIP_ORGANIZATION_SKILL;
        }
        else if(dmoObject.isKindOf(context,TYPE_BUSINESS_SKILL))
        {
             strRelationshipPattern = RELATIONSHIP_SUBSKILL;
        }
        
        StringList slBusSelect = new StringList();
        slBusSelect.add(DomainObject.SELECT_ID);
        slBusSelect.add(SELECT_SUBSKILL_IDS);
        
        StringList slRelSelect = new StringList();
        slRelSelect.add(DomainRelationship.SELECT_ID);
        
        boolean getTo = false; 
        boolean getFrom = true; 
        
        String strBusWhere = "";
        String strRelWhere = "";
        
        MapList mlRelatedObjects = dmoObject.getRelatedObjects(context,
                                                                                        strRelationshipPattern, //pattern to match relationships
                                                                                        strTypePattern, //pattern to match types
                                                                                        slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                                                                                        slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                                                                                        getTo, //get To relationships
                                                                                        getFrom, //get From relationships
                                                                                        recurseToLevel, //the number of levels to expand, 0 equals expand all.
                                                                                        strBusWhere, //where clause to apply to objects, can be empty ""
                                                                                        strRelWhere,0); //where clause to apply to relationship, can be empty ""
        return mlRelatedObjects;
    }
    
    /**
     * Returns list containing the count of subskills for each skill in a row, used for table "APPBusinessSkillSummary" and  column "Count"
     * 
     * @param context Matrix Context object
     * @param args packed arguments sent by UI Table components
     * @return Vector containing the count of subskills for each skill in a row
     * @throws Exception if operation fails
     */
    public Vector getColumnCountData(Context context, String[] args)throws Exception
    { 
        try
        {
            // Create result vector
            Vector vecResult = new Vector();
            
            // Get object list information from packed arguments
            Map programMap = (Map) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");

            Map mapObjectInfo = null;
            String strCount = null;
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) 
            {
                mapObjectInfo = (Map) itrObjects.next();
                String strObjectId = (String) mapObjectInfo.get("id");
                String strRootNode = (String) mapObjectInfo.get("Root Node");
                boolean isRootNode = (! UIUtil.isNullOrEmpty(strRootNode)) && "True".equalsIgnoreCase(strRootNode)?true:false;
                DomainObject dmoObject = DomainObject.newInstance(context, strObjectId);
                if(isRootNode)
                {
                	MapList mlRelatedObjects = getSubSkill(context, 
              				strObjectId, "1");
                	strCount = ""+mlRelatedObjects.size();  
                }
                else
                {
                strCount = (String)mapObjectInfo.get("SubSkillCount");
                }
                vecResult.add(strCount);
            }
            
            return vecResult;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }
    }
    
    /**
     * getSkillTypePolicy - This Method populates the policy for Business Skill
     * Used in emxBusinessSkill WebForm
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectList - Contains a MapList of Maps which contains object names
     * @return Vector of "Policy" values for the combobox
     * @throws Exception if the operation fails
     */
    public HashMap getSkillTypePolicy(Context context, String[] args)
    throws Exception
    {
        try {
                       
            Map programMap = (Map)JPO.unpackArgs(args);
            Map requestMap = (Map)programMap.get("requestMap");
            String strSelectedType = (String)requestMap.get("type");
            
            
            if (strSelectedType != null) {
                if (strSelectedType.indexOf(":") != -1) {
                    // type=_selectedType:TestChildVault,type_ProjectVault,type_TectChildVault
                    StringList slSplitType = FrameworkUtil.split(strSelectedType, ":");
                    if (slSplitType.size() > 1) {
                        strSelectedType = (String)slSplitType.get(1);
                        slSplitType = FrameworkUtil.split(strSelectedType, ",");
                        if (slSplitType.size() > 0) {
                            strSelectedType = (String)slSplitType.get(0);
                        }
                        else {
                            strSelectedType = null;
                        }
                    }
                    else {
                        strSelectedType = null;
                    }
                }else{
                	 strSelectedType = PropertyUtil.getSchemaProperty(context,strSelectedType) ;
                     
                }
            }
           
            Policy policy = null;
            String strPolicy = null;
            StringList slParentPolicies = new StringList();
            String sLanguage = context.getSession().getLanguage();
            
            BusinessType btSkillVault = new BusinessType(strSelectedType, context.getVault());
            btSkillVault.open(context);
            PolicyList strList = btSkillVault.getPolicies(context);
            btSkillVault.close(context);
            
            StringList slSkillVault = new StringList();
            StringList slSkillVaultTranslated = new StringList();
           HashMap map = new HashMap();
            for(int i=0; i<strList.size();i++){
                policy = (Policy)strList.elementAt(i);
                strPolicy = policy.getName();
                slSkillVault.add(strPolicy);
                slSkillVaultTranslated.add(i18nNow.getAdminI18NString("Policy", strPolicy, sLanguage));
            }       
            
            map.put("field_choices", slSkillVault);
            map.put("field_display_choices", slSkillVaultTranslated);
            
            return  map;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;           
        }
    }
    
  
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList getAssociatedBusinessSkills(Context context, String[] args) throws Exception {
		Map programMap = (Map)JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
		com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person)DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
		person.setId(objectId);
		StringList busSelects = new StringList();
		StringList nonSelectList = new StringList();
		busSelects.add(person.SELECT_ID);
		MapList relatedSkills = person.getRelatedObjects(context, "hasBusinessSkill", "*", busSelects, DomainConstants.EMPTY_STRINGLIST, false, true, (short)1, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
		for(int i = 0 ; i <relatedSkills.size(); i++){
			nonSelectList.add( (String)((Map)relatedSkills.get(i)).get(person.SELECT_ID));
			getParentSkills(context , nonSelectList,(String)((Map)relatedSkills.get(i)).get(person.SELECT_ID));     
		}
		if(nonSelectList == null){
			nonSelectList = new StringList();
		}
		return nonSelectList;
	}
	
	private void getParentSkills(Context context,StringList nonSelectList, String objectId){
        try
        {
          boolean flag=true;
          DomainObject domChildObject = new DomainObject(objectId);
          String parentObjectId= domChildObject.getInfo(context,"to["+DomainConstants.RELATIONSHIP_SUBSKILL+"].from."+DomainConstants.SELECT_ID);
    
          DomainObject domParentObject = new DomainObject(parentObjectId);
         
          StringList busSelects = new StringList();
          busSelects.add(DomainConstants.SELECT_ID);
  
          MapList childFromParentList = domParentObject.getRelatedObjects(context, DomainConstants.RELATIONSHIP_SUBSKILL, DomainConstants.TYPE_BUSINESS_SKILL, busSelects, DomainConstants.EMPTY_STRINGLIST, false, true, (short)1, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING );
                 
          Iterator childFromParentItr = childFromParentList.iterator();
          while(childFromParentItr.hasNext())
          {
            Map childFromParent =(Map)childFromParentItr.next();
            String childObjectId = (String) childFromParent.get(DomainConstants.SELECT_ID);
            if(!nonSelectList.contains(childObjectId))
            {
              flag=false;
              break;
            }
            
          }
          
          if(flag)
          {
            nonSelectList.add(parentObjectId);
            getParentSkills(context,nonSelectList,parentObjectId);
          }
          
        }
        catch(Exception e)
        {
          e.toString();
        }
    }
    
  
// End:R208:PRG Advanced Resource Planning
}
