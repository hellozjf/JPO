import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Role;
import matrix.db.RoleItr;
import matrix.db.RoleList;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.RoleUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.XSSUtil;

// ${CLASSNAME}.java
//
// Created on Aug 7, 2010
//
// Copyright (c) 2005 MatrixOne Inc.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

/**
 * @author sg2
 *
 * The <code>${CLASSNAME}</code> class/interface contains ...
 *
 * @version AEF 11.0.0.0 - Copyright (c) 2005, MatrixOne, Inc.
 */
public class emxRoleUtilBase_mxJPO extends emxDomainObject_mxJPO {

    public emxRoleUtilBase_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    public MapList getRoleListForSummaryTable(List roleNames, List rolesToDisable) {
        MapList roles = new MapList(roleNames.size());
        HashMap roleDetails = new HashMap(2);
        
        for (int i = 0; i < roleNames.size(); i++) {
            Map clone = (Map) roleDetails.clone();
            
            Object role = roleNames.get(i);
            String selectable = rolesToDisable != null && rolesToDisable.contains(role) ? "true" : "false";
            
            clone.put(SELECT_ID, role);
            clone.put("disableSelection", selectable);
            
            roles.add(clone);
        }
        return roles;
    }
    
    public StringList getI18NRoleName(Context context, String[] args) throws FrameworkException {
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            HashMap paramList  = (HashMap) programMap.get("paramList");
            String strLanguage = (String) paramList.get("languageStr");
            StringList roleNames = new StringList(objectList.size());
            for (int i = 0; i < objectList.size(); i++) {
                Map objectMap = (Map)objectList.get(i);
                roleNames.add((String)objectMap.get(SELECT_ID));
            }
            return i18nNow.getAdminI18NStringList("Role", roleNames, strLanguage);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
    
    public StringList getI18NRoleDescription(Context context, String[] args) throws FrameworkException {
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            StringList roleDesc = new StringList(objectList.size());
            HashMap paramList  = (HashMap) programMap.get("paramList");
            String strLanguage = (String) paramList.get("languageStr");

            for (int i = 0; i < objectList.size(); i++) {
                Map objectMap = (Map)objectList.get(i);
                roleDesc.add(i18nNow.getRoleDescriptionI18NString((String) objectMap.get(SELECT_ID), strLanguage));
            }
            return roleDesc;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
    
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getRolesSearchResults(Context context, String[] args) throws FrameworkException {
       try {
           Map programMap = (Map) JPO.unpackArgs(args);
           HashMap requestMap      = (HashMap)programMap.get("RequestValuesMap");
           String[] topLevelArr = requestMap == null ? null : (String [])requestMap.get("APPFilterTopLevelCheckbox");
           String[] subLevelArr = requestMap == null ? null : (String [])requestMap.get("APPFilterSubLevelCheckbox");
           
           String sNamePattern         = (String)programMap.get("APPNameMatchesTextbox");
           String sTopChecked        = topLevelArr != null && topLevelArr.length > 0 ? topLevelArr[0] : null;
           String sSubChecked        = subLevelArr != null && subLevelArr.length > 0 ? subLevelArr[0] : null;
           
           String objectId             = (String)programMap.get("objectId");
           List allRolesList           = getAllRolesList(context, objectId, sNamePattern, sSubChecked, sTopChecked, -1);
           return getRoleListForSummaryTable(allRolesList, null);
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }
  
   protected List getAllRolesList(Context context, String objectId, String namePattern, String sSubChecked, String sTopChecked, int queryLimit) throws Exception  {
       try {

           String strOrgId = null;
           if(!"".equals(objectId) && objectId != null) {
               DomainObject doType   = DomainObject.newInstance(context, objectId);
               strOrgId       = ((String)doType.getInfo(context, SELECT_TYPE)).equals(DomainConstants.TYPE_PERSON) ? new Person(objectId).getCompanyId(context) :  
                       			((String)doType.getInfo(context, SELECT_TYPE)).equals(DomainConstants.TYPE_COMPANY)? objectId : null;
           }
           
           List resultsList  = new ArrayList();
           String languageStr = context.getSession().getLanguage();
           Role role  = new Role();
           List roleList = getRoleNameList(context, role.getRoles(context));
           List topRoles = getRoleNameList(context, role.getTopLevelRoles(context));
           if(!"true".equalsIgnoreCase(sTopChecked) && "true".equalsIgnoreCase(sSubChecked)) {
               roleList.removeAll(topRoles);
           } else if("true".equalsIgnoreCase(sTopChecked) && !"true".equalsIgnoreCase(sSubChecked)) {
               roleList = topRoles;
           }
           
           // this vector will eliminate the roles to be displayed.
           Set orgSpecificRoles = null;
           if(strOrgId != null && strOrgId.length() > 0) {
               orgSpecificRoles = new HashSet();
               String isSetupAsPrivateExchange = "true";
               try {isSetupAsPrivateExchange = EnoviaResourceBundle.getProperty(context,"emxComponents.isSetupAsPrivateExchange"); }
               catch(Exception e){}

               boolean isPrivateExchange = !(isSetupAsPrivateExchange != null && "false".equalsIgnoreCase(isSetupAsPrivateExchange.trim()));

               if(!(new emxOrganization_mxJPO(context, null).isParentCompanyAHostCompany(context, strOrgId)))   {
                   orgSpecificRoles.add(PropertyUtil.getSchemaProperty(context, "role_BusinessManager"));
                   orgSpecificRoles.add(PropertyUtil.getSchemaProperty(context, "role_ProgramLead"));
                   orgSpecificRoles.add(PropertyUtil.getSchemaProperty(context, "role_ProjectAdministrator"));
                   orgSpecificRoles.add(PropertyUtil.getSchemaProperty(context, "role_ProjectLead"));
                   orgSpecificRoles.add(PropertyUtil.getSchemaProperty(context, "role_ProjectUser"));

                   // for hiding emplyee and sub roles.
                   String strEmpRole = PropertyUtil.getSchemaProperty(context, "role_Employee");
                   orgSpecificRoles.add(strEmpRole);
                   Role roleEmployee = new Role(strEmpRole);
                   orgSpecificRoles.addAll(getRoleNameList(context, getChildRoles (context,roleEmployee)));
                   // this is defined to eliminate some roles from the "above rolelist for hiding"
                   if(!isPrivateExchange) {
                       orgSpecificRoles.remove(PropertyUtil.getSchemaProperty(context, "role_Buyer"));
                       orgSpecificRoles.remove(PropertyUtil.getSchemaProperty(context, "role_BuyerAdministrator"));
                   }
               }
           }
           
           Pattern pattern = namePattern != null ? new Pattern(namePattern) : new Pattern("*");
           String caseStatus = MqlUtil.mqlCommand(context, "print system  casesensitive");
           if(caseStatus.equals("CaseSensitive=Off")) {
                 pattern.setCaseSensitive(false);
           }
           for (int i = 0; i < roleList.size(); i++) {
               String sRole  = (String) roleList.get(i);
               String i18Role = i18nNow.getRoleI18NString(sRole, languageStr);
               if(!pattern.match(i18Role) || (orgSpecificRoles != null && orgSpecificRoles.contains(sRole))) {
                   continue;
               }
               resultsList.add(sRole);
           }
           
           if(orgSpecificRoles!=null && !orgSpecificRoles.isEmpty()){
               Iterator itr = orgSpecificRoles.iterator();
               while(itr.hasNext())
               {
            	   String sRole = (String)itr.next();
            	   String i18Role = i18nNow.getRoleI18NString(sRole, languageStr);
            	   if(pattern.match(i18Role)){
            		   resultsList.add(sRole);
            	   }
                }
             }
          
           resultsList.removeAll(getProjectAndOrganizationClassificationRoles(context));
           return (queryLimit > 0 && resultsList.size() > queryLimit) ?  resultsList.subList(0, queryLimit - 1) : resultsList;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }
   
   protected RoleList getChildRoles(Context context, Role matrixRole) throws FrameworkException {
       try {
           RoleList roleList = new RoleList();
		   //After assigning a collection to an iterator, if you try to modify the collection in the same iteration
		   //we will get Concurrent modification exception and hence we are using tempRoleList
		   RoleList tempRoleList = new RoleList();
           matrixRole.open(context);

           if(matrixRole.hasChildren()) {
               roleList.addAll(matrixRole.getChildren());
			   tempRoleList.addAll(matrixRole.getChildren());
               RoleItr roleItr = new RoleItr(tempRoleList);
               while (roleItr.next())
               {
                  Role childRole = (Role)roleItr.obj();
                  roleList.addAll(getChildRoles(context, childRole));
               }
           }
           matrixRole.close(context);
           return roleList;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }


   
   protected StringList getProjectAndOrganizationClassificationRoles(Context context) throws Exception
   {
       String strMQL = "list role * where 'isaproject || isanorg'";
       String strResult = MqlUtil.mqlCommand(context, strMQL, true);
       return FrameworkUtil.split(strResult, System.getProperty("line.separator"));
   }
   
   
   public String getRolesTypeHTML(Context context, String[] args) throws FrameworkException {
       try {

           StringBuffer sb = new StringBuffer();
           sb.append("<img border=0 src=../common/images/iconRole.gif></img>&nbsp;");
           sb.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", 
                   context.getLocale(),"emxComponents.Common.Role"));
           return sb.toString();
                  
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
    }

   public String getRoleLevelsHTMLOutput(Context context, String[] args) throws FrameworkException {
       try {
           HashMap programMap = (HashMap) JPO.unpackArgs(args);
           HashMap paramMap = (HashMap) programMap.get("paramMap");
           String languageStr = (String) paramMap.get("languageStr");
           StringBuffer sb = new StringBuffer();
           i18nNow loc = new i18nNow();
           String topLevel = loc.GetString ("emxComponentsStringResource",languageStr,"emxComponents.SearchGroup.TopLevel");
           String subLevel = loc.GetString ("emxComponentsStringResource",languageStr,"emxComponents.SearchGroup.SubLevel");
           sb.append("<input type=checkbox name=chkTopLevel id=chkTopLevel/>"+XSSUtil.encodeForHTML(context,topLevel)+"<br>");
           sb.append("<input type=checkbox name=chkSubLevel id=chkSubLevel />"+XSSUtil.encodeForHTML(context,subLevel));
           
           return sb.toString();
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
    }
   
   @SuppressWarnings({ "rawtypes", "unchecked" })
    protected List getRoleNameList(Context context, RoleList rolesList) throws FrameworkException {
       try {
           List arrayList = new ArrayList(rolesList.size());
           RoleItr roleItr = new RoleItr(rolesList);
           while (roleItr.next()) {
               String sRole  = roleItr.obj().getName();
               if( RoleUtil.isOnlyRole(context, sRole))
               {
                   arrayList.add(sRole);
               }
           }
           return arrayList;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }


   @SuppressWarnings({ "rawtypes", "unchecked" })
   protected List getRoleNameList(RoleList rolesList) throws FrameworkException {
       try {
           List arrayList = new ArrayList(rolesList.size());
           RoleItr roleItr = new RoleItr(rolesList);
           while (roleItr.next()) {
             String sRole  = roleItr.obj().getName();
             arrayList.add(sRole);
           }
           return arrayList;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }
}
