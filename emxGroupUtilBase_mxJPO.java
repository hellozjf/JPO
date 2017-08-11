import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import matrix.db.Context;
import matrix.db.Group;
import matrix.db.GroupItr;
import matrix.db.GroupList;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
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
public class emxGroupUtilBase_mxJPO extends emxDomainObject_mxJPO {

    public emxGroupUtilBase_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);
        // TODO Auto-generated constructor stub
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    public MapList getGroupListForSummaryTable(List groupNames, List groupsToDisable) {
        MapList groups = new MapList(groupNames.size());
        HashMap groupDetails = new HashMap(2);

        for (int i = 0; i < groupNames.size(); i++) {
            Map clone = (Map) groupDetails.clone();
            
            Object group = groupNames.get(i);
            String selectable = groupsToDisable != null && groupsToDisable.contains(group) ? "true" : "false";
            
            clone.put(SELECT_ID, group);
            clone.put("disableSelection", selectable);
            
            groups.add(clone);
        }
        return groups;
    }
    
    public StringList getI18NGroupName(Context context, String[] args) throws FrameworkException {
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            StringList groupNames = new StringList(objectList.size());
            HashMap paramList  = (HashMap) programMap.get("paramList");
            String strLanguage = (String) paramList.get("languageStr");

            for (int i = 0; i < objectList.size(); i++) {
                Map objectMap = (Map)objectList.get(i);
                groupNames.add((String)objectMap.get(SELECT_ID));
            }
            return i18nNow.getAdminI18NStringList("Group", groupNames, strLanguage);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
    
    public StringList getI18NGroupDescription(Context context, String[] args) throws FrameworkException {
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            StringList groupDesc = new StringList(objectList.size());
            HashMap paramList  = (HashMap) programMap.get("paramList");
            String strLanguage = (String) paramList.get("languageStr");
            String adminGroupDesc = "GroupDescription";
            for (int i = 0; i < objectList.size(); i++) {
                Map objectMap = (Map)objectList.get(i);
                groupDesc.add(i18nNow.getMXI18NString((String) objectMap.get(SELECT_ID), "", strLanguage, adminGroupDesc));
            }
            return groupDesc;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getGroupSearchResults(Context context, String[] args) throws FrameworkException {
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            HashMap requestMap      = (HashMap)programMap.get("RequestValuesMap");
            String[] topLevelArr = requestMap == null ? null : (String [])requestMap.get("APPFilterTopLevelCheckbox");
            String[] subLevelArr = requestMap == null ? null : (String [])requestMap.get("APPFilterSubLevelCheckbox");
            
            String sNamePattern         = (String)programMap.get("APPNameMatchesTextbox");
            String sTopChecked        = topLevelArr != null && topLevelArr.length > 0 ? topLevelArr[0] : null;
            String sSubChecked        = subLevelArr != null && subLevelArr.length > 0 ? subLevelArr[0] : null;
            
            List allGroupList   =  getAllGroupList(context, sNamePattern, sSubChecked, sTopChecked, -1);
            return getGroupListForSummaryTable(allGroupList, null);
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
    
    public List getAllGroupList(Context context, String namePattern, String sSubChecked, String sTopChecked, int queryLimit) throws FrameworkException {
        try {
            List resultsList  = new ArrayList();
            
            Group group  = new Group();
            List groupList = getGroupNameList(context, group.getGroups(context));
            List topGroups = getGroupNameList(context, group.getTopLevelGroups(context));
            
            if(!"true".equalsIgnoreCase(sTopChecked) && "true".equalsIgnoreCase(sSubChecked)) {
                groupList.removeAll(topGroups);
            } else if("true".equalsIgnoreCase(sTopChecked) && !"true".equalsIgnoreCase(sSubChecked)) {
                groupList = topGroups;
            }
            
            Pattern pattern = namePattern != null ? new Pattern(namePattern) : new Pattern("*");
            String caseStatus = MqlUtil.mqlCommand(context, "print system  casesensitive");
            if(caseStatus.equals("CaseSensitive=Off")) {
                  pattern.setCaseSensitive(false);
            }            

            for (int i = 0; i < groupList.size(); i++) {
                String sGroup  = (String) groupList.get(i);
                if(!pattern.match(sGroup)) {
                    continue;
                }
                resultsList.add(sGroup);
            }
            
            return (queryLimit > 0 && resultsList.size() > queryLimit) ?  resultsList.subList(0, queryLimit - 1) : resultsList;
                    
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
    
    protected List getGroupNameList(Context context, GroupList groupsList) throws FrameworkException {
        try {
            List arrayList = new ArrayList(groupsList.size());
            GroupItr groupItr = new GroupItr(groupsList);
            while (groupItr.next()) {
              String sGroup  = groupItr.obj().getName();
              arrayList.add(sGroup);
            }
            return arrayList;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
   
    public String getGroupsTypeHTML(Context context, String[] args) throws FrameworkException {
        try {
			//XSSOK
            StringBuffer sb = new StringBuffer();
            sb.append("<img border=0 src=../common/images/iconSmallGroup.gif></img>&nbsp;");
            sb.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", 
                    new Locale(context.getSession().getLanguage()),"emxComponents.Common.Group"));
            return sb.toString();
                   
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
     }
    
    public String getGroupLevelsHTMLOutput(Context context, String[] args) throws FrameworkException {
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
    
    public DomainObject createBusinessGroupObject(Context context, String []args) throws FrameworkException{
       DomainObject groupObject = null;
       String businessGroupName = args[0];
        try {
            ContextUtil.pushContext(context);
            ContextUtil.startTransaction(context, true);
            groupObject = new DomainObject();
            groupObject.createObject(context, DomainConstants.TYPE_BUSINESS_GROUP, businessGroupName, "-", DomainConstants.POLICY_BUSINESS_GROUP, context.getVault().getName());
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            throw new FrameworkException(e);
        }finally{
            ContextUtil.popContext(context);
        }
       
        return groupObject;
    }
}
