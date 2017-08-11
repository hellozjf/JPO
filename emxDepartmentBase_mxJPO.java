import java.util.HashMap;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jsystem.util.StringUtils;

// ${CLASSNAME}.java
//
// Created on Aug 22, 2010
//
// Copyright (c) 2005 MatrixOne Inc.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

/**
 * @author SG2
 *
 * The <code>${CLASSNAME}</code> class/interface contains ...
 *
 * @version AEF 11.0.0.0 - Copyright (c) 2005, MatrixOne, Inc.
 */
public class emxDepartmentBase_mxJPO extends emxOrganization_mxJPO {
    private static final String typeCompany          = PropertyUtil.getSchemaProperty("type_Department");
    private static final String attrOrganizationName = PropertyUtil.getSchemaProperty("attribute_OrganizationName");
    private static final String policyOrganization   = PropertyUtil.getSchemaProperty("policy_Organization");
    private static final String relDepartment        = PropertyUtil.getSchemaProperty("relationship_CompanyDepartment");
    private static final String sAttrOrgId = PropertyUtil.getSchemaProperty("attribute_OrganizationID");
    private static final String sAttrCageCode = PropertyUtil.getSchemaProperty("attribute_CageCode");  
    private static final String sStandardCost            = PropertyUtil.getSchemaProperty("attribute_StandardCost");
    
    private static final String nameSelect = getAttributeSelect(attrOrganizationName);
    private static final String departmentIdSelect = getAttributeSelect(sAttrOrgId);
    private static final String cageCodeSelect = getAttributeSelect(sAttrCageCode);
    
    private static final String PARANT_OBJECT_SELECT = "to["+ relDepartment+"].from.id";
    
    
    private static final long serialVersionUID = 1L;
    public emxDepartmentBase_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);
        // TODO Auto-generated constructor stub
    }

    /**
     * This method is overloaded to return blank value in case of crate department use case.  
     */
    public String getFieldResourceManagersData(Context context, String[] args) throws FrameworkException {
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            Map requestMap = (Map) programMap.get("requestMap");
            if("createDepartment".equalsIgnoreCase((String) requestMap.get("actionPerformed"))) {
                return "";
            } else {
                return super.getFieldResourceManagersData(context, args);    
            }
            
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
    
    /**
     * Post process JPO for create department.
     * Here it do some basic checks like Dept Name, Dept ID, and Cage Code are unique for the org where this dept is created
     * Also connects the dept to the organization
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map createDepartmentPostProcess(Context context, String[] args) throws FrameworkException {
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            
            Map actionMap = new HashMap();
            Map requestMap = (Map) programMap.get("requestMap");
            Map paramMap = (Map)programMap.get("paramMap");
            
            String newObjectId = (String) paramMap.get("newObjectId");
            String parentOID = (String) requestMap.get("objectId");
            
            boolean cageCodeRequired = "true".equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxComponents.cageCode.Uniqueness"));
            
            String deptName = (String) requestMap.get("Name");
            String deptId = (String) requestMap.get("Department ID");
            String cageCode = cageCodeRequired ? (String) requestMap.get("Cage Code1") : null;

            //Expand to get the Company id from Person Object , in case the Department is created from People Node.
            DomainObject parentObject = DomainObject.newInstance(context);
            parentObject.setId(parentOID);
            
            StringList selectables = new StringList();
            selectables.add(SELECT_TYPE);
            selectables.add(SELECT_NAME);
            selectables.add(SELECT_REVISION);
            
            Map parentObjectInfo = parentObject.getInfo(context, selectables);  

            if(TYPE_PERSON.equals(parentObjectInfo.get(SELECT_TYPE))) {
                parentOID = PersonUtil.getUserCompanyId(context);
                parentObject.setId(parentOID);
            }
            
            String messageKey = validateUniqueness(context, parentOID, deptName, deptId, cageCode, true, true, cageCodeRequired);
            if(messageKey != null) {
                actionMap.put("Message", messageKey);
                actionMap.put("Action", "stop");
                return actionMap;
            }
            
            DomainObject department = DomainObject.newInstance(context);
            department.setId(newObjectId);
            
            DomainRelationship.connect(context, parentObject, new RelationshipType(relDepartment), department);
            department.promote(context);
            department.setAttributeValue(context, attrOrganizationName, deptName);
            
            return actionMap;
            
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * Post process JPO for edit department.
     * Here it do some basic checks like Dept Name, Dept ID, and Cage Code are unique for the org where this dept is created
     */
    
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public Map editDepartmentPostProcess(Context context, String[] args) throws FrameworkException {

        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            
            Map actionMap = new HashMap();
            Map requestMap = (Map) programMap.get("requestMap");
            Map paramMap = (Map)programMap.get("paramMap");
            
            boolean cageCodeRequired = "true".equalsIgnoreCase(EnoviaResourceBundle.getProperty(context,"emxComponents.cageCode.Uniqueness"));
            
            String departmentObjId = (String) paramMap.get("objectId");
            String deptName = (String) paramMap.get("Name");
            String deptId = (String) paramMap.get("Department ID");
            String cageCode = cageCodeRequired ? (String) paramMap.get("Cage Code1") : null;

            StringList selectables = new StringList(4);
            selectables.add(PARANT_OBJECT_SELECT);
            selectables.add(nameSelect);
            selectables.add(departmentIdSelect);
            selectables.add(cageCodeSelect);
            
            DomainObject dept = DomainObject.newInstance(context);
            dept.setId(departmentObjId);
            
            Map deptDetails = dept.getInfo(context, selectables);
            
            String oldName = (String) deptDetails.get(nameSelect);
            String oldDeptId = (String) deptDetails.get(departmentIdSelect);
            String oldCageCode = (String) deptDetails.get(cageCodeSelect);
            
            //No need to check the name uniqueness as we are validating the org name uniqueness before proceed to edit the department process.
            String messageKey = validateUniqueness(context, 
                    (String) deptDetails.get(PARANT_OBJECT_SELECT),
                    oldName, oldDeptId, oldCageCode,
                    false, !deptId.equals(oldDeptId), cageCodeRequired && !cageCode.equals(oldCageCode));

            if(messageKey != null) {
                actionMap.put("Message", messageKey);
                actionMap.put("Action", "stop");
                return actionMap;
            }
            
            dept.setAttributeValue(context, attrOrganizationName, deptName);
            
            return actionMap;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
            
    }
    
    /**
     * Helper method for checking whether dept name, id and cage code are unique with in the organization or not.
     * If any of them are not unique retrun the error string resoruce key to displayed back to the user. 
     * @param context
     * @param parentOID
     * @param deptName
     * @param departmentId
     * @param cageCode
     * @param checkName
     * @param checkDeptId
     * @param checkCageCode
     * @return
     * @throws FrameworkException
     */

    protected String validateUniqueness(Context context, String parentOID, 
                                            String deptName, 
                                            String departmentId, 
                                            String cageCode,
                                            boolean checkName,
                                            boolean checkDeptId,
                                            boolean checkCageCode) throws FrameworkException {
        
        try {
            String messageKey = null;
            
            if(!checkName && !checkDeptId && !checkCageCode)
                return messageKey;
            
            String existingIDs = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3", parentOID, "from["+relDepartment+"].to.id", ",");
            if(existingIDs == null || existingIDs.equals("") || existingIDs.equals("null"))
                return messageKey;
                
            String[] existingDeptIds = StringUtils.split(existingIDs, ",");
            if(existingDeptIds.length != 0) {
                StringList selectables = new StringList(3);
                selectables.add(nameSelect);
                selectables.add(departmentIdSelect);
                selectables.add(cageCodeSelect);
                
                MapList existingDeptInfo = DomainObject.getInfo(context, existingDeptIds, selectables);
                
                boolean nameExists = false; 
                boolean deptIdExists = false; 
                boolean cageCodeExists = false;
                for (int i = 0; (i < existingDeptInfo.size()) && !(nameExists || deptIdExists || cageCodeExists); i++) {
                    Map dept = (Map) existingDeptInfo.get(i);
                    nameExists = checkName && deptName.equals(dept.get(nameSelect));
                    deptIdExists = checkDeptId && !nameExists && departmentId.equals(dept.get(departmentIdSelect));
                    cageCodeExists = checkCageCode && !(nameExists || deptIdExists) && cageCode.equals(dept.get(cageCodeSelect));
                }
                
                messageKey = nameExists ? "emxComponents.Department.DepartmentAlreadyExists" :
                             deptIdExists ? "emxComponents.CreateOrEditOrganization.OrganizationIdAlreadyExists" :
                             cageCodeExists ? "emxComponents.CreateOrEditCompany.CageCodeAlreadyExists" : null;   
                
            }
            return messageKey;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }
    
	 /**
   	  * Expand program for select Department
	  * Displays All the Business Units and Departments in the selected organization.
	  * It will disable Business Units from selection
	  * Also If isFrom and relationshipName names are passed and if the context object is already connected
	  * to a Department it will be disabled for selection.
	  */ 	
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList selectionDepartmentExpandProgram(Context context, String[] args) throws FrameworkException {
        try {
            Map programMap = (Map) JPO.unpackArgs(args);
            
            boolean isAddExisting = "true".equalsIgnoreCase((String)programMap.get("isAddExisting"));
            String isFrom= (String) programMap.get("isFrom");
            String relationshipName = (String) programMap.get("relationshipName");
            String SELECT_CURRENT_OBJECT_ID = null;
            if(!UIUtil.isNullOrEmpty(isFrom) && !UIUtil.isNullOrEmpty(relationshipName)) {
                relationshipName = PropertyUtil.getSchemaProperty(context,relationshipName);
                SELECT_CURRENT_OBJECT_ID = "true".equalsIgnoreCase(isFrom) ?
                                           "from["+relationshipName+"].to.id" : "to["+relationshipName+"].from.id";
                isAddExisting = true;
            }
            
            String orgId = (String) programMap.get("objectId");
            DomainObject object = DomainObject.newInstance(context, orgId);
            
            Pattern relPattern = new Pattern(RELATIONSHIP_COMPANY_DEPARTMENT);
            relPattern.addPattern(RELATIONSHIP_DIVISION);
            
            Pattern typePattern = new Pattern(TYPE_BUSINESS_UNIT);
            typePattern.addPattern(TYPE_DEPARTMENT);
            
            StringList objSel = new StringList(2);
            objSel.add(SELECT_ID);
            objSel.add(SELECT_TYPE);
            if(SELECT_CURRENT_OBJECT_ID != null)
                objSel.add(SELECT_CURRENT_OBJECT_ID);
            
            MapList  mapList = object.getRelatedObjects(context, relPattern.getPattern(), typePattern.getPattern(),
                                                        false, true,(short)1,
                                                        objSel, null,
                                                        "current=='Active'" ,null,
                                                        0,
                                                        null,null,null);
            
            String parentObjId = isAddExisting ? ((String[])((Map) programMap.get("RequestValuesMap")).get("objectId"))[0] : null;

            for (int i = 0; i < mapList.size(); i++) {
                Map org = (Map) mapList.get(i);
                if(!TYPE_DEPARTMENT.equals(org.get(SELECT_TYPE))) {
                    org.put("disableSelection", "true");
                    continue;
                }
                
                if(isAddExisting) {
                    Object connectedObject = org.get(SELECT_CURRENT_OBJECT_ID);
                    if(connectedObject == null) {
                        continue;
                    } else if (connectedObject instanceof StringList) {
                        if(((StringList)connectedObject).contains(parentObjId)) {
                            org.put("disableSelection", "true");
                        }                            
                    } else if(connectedObject.equals(parentObjId)) {
                        org.put("disableSelection", "true");
                    }
                }                
            }            

            return mapList;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }        
    }
}

