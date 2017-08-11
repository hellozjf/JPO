import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Person;

public class Bmbim_contract_states_mxJPO extends emxDomainObject_mxJPO {
    
    public Bmbim_contract_states_mxJPO(Context arg0, String[] arg1) throws Exception {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    // Role
    public static final String ROLE_BRANCHLEADER = "branchleader";
    public static final String ROLE_GENERALLEADER = "generalleader";
    public static final String ROLE_CLIENT = "client";
    public static final String ROLE_ADMIN = "admin";
    
    // State
    public static final String STATE_INPUT = "contract_info_input";
    public static final String STATE_BRANCHLEADER_REVIEW = "contract_branchleader_review";
    public static final String STATE_GENERALLEADER_REVIEW = "contract_generalleader_review";
    public static final String STATE_CLIENT_REVIEW = "contract_client_review";
    public static final String STATE_SIGNED = "contract_official_signed";
    public static final String STATE_PAYMENT = "contract_wait_payment";
    public static final String STATE_END = "contract_final_end";

    public Vector getOIDs(Context context, String[] args) throws Exception {
        // Get an object list which based on args.
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");

        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector oids = new Vector(objectList.size());
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            oids.addElement(oid);
        }
        // Return the object array.
        return oids;
    }
    
    public static boolean isLeader(Context context, String[] args) throws Exception {
        String user = context.getUser();
        Person person = new Person(user);
        if (person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_ADMIN) ||
                person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_BRANCHLEADER) ||
                person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_GENERALLEADER) ||
                person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_CLIENT)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static List getDyanmicColumns(Context context, String[] args) throws Exception {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");
        
        MapList columnMapList = new MapList();
//        System.out.println("context.getRole() " + context.getRole());
        String user = context.getUser();
        Person person = new Person(user);
//        boolean isAssigned = person.isAssigned(context, "branchleader");
//        System.out.println("user:" + user + " person:" + person + " isAssigned:" + isAssigned);
        
        Iterator iter = objectList.iterator();
        Map map = (Map) iter.next();
        String oid = (String) map.get(DomainConstants.SELECT_ID);
        DomainObject domainObject = DomainObject.newInstance(context, oid);
        String state = domainObject.getInfo(context, "current");
        System.out.println("user:" + user + " state:" + state);
        
        if (person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_BRANCHLEADER)) {
            if (state.equals(Bmbim_contract_states_mxJPO.STATE_BRANCHLEADER_REVIEW)) {
                Map approveColMap = new HashMap();
                HashMap approveSettingMap = new HashMap();
                approveSettingMap.put("Registered Suite", "Framework");
                approveSettingMap.put("Editable", "false");
                approveSettingMap.put("Column Type", "program");
                approveSettingMap.put("program", "Bmbim_contract_states");
                approveSettingMap.put("function", "getBranchleaderApprove");
                approveSettingMap.put("Target Location", "listHidden");
                approveColMap.put("name", "Bmbim_contract_states_branchleader_approve");
                approveColMap.put("label", "emxFramework.Attribute.Bmbim_contract_states_branchleader_approve");
                approveColMap.put("href", "${COMMON_DIR}/emxPromoteWithApprove.jsp");
                approveColMap.put("settings", approveSettingMap);
                columnMapList.add(approveColMap);
                
                Map rejectColMap = new HashMap();
                HashMap rejectSettingMap = new HashMap();
                rejectSettingMap.put("Registered Suite", "Framework");
                rejectSettingMap.put("Editable", "false");
                rejectSettingMap.put("Column Type", "program");
                rejectSettingMap.put("program", "Bmbim_contract_states");
                rejectSettingMap.put("function", "getBranchleaderReject");
                rejectSettingMap.put("Target Location", "listHidden");
                rejectColMap.put("name", "Bmbim_contract_states_branchleader_reject");
                rejectColMap.put("label", "emxFramework.Attribute.Bmbim_contract_states_branchleader_reject");
                rejectColMap.put("href", "${COMMON_DIR}/emxPromoteWithReject.jsp");
                rejectColMap.put("settings", rejectSettingMap);
                columnMapList.add(rejectColMap);
            }
        } else if (person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_GENERALLEADER)) {
            if (state.equals(Bmbim_contract_states_mxJPO.STATE_GENERALLEADER_REVIEW)) {
                Map approveColMap = new HashMap();
                HashMap approveSettingMap = new HashMap();
                approveSettingMap.put("Registered Suite", "Framework");
                approveSettingMap.put("Editable", "false");
                approveSettingMap.put("Column Type", "program");
                approveSettingMap.put("program", "Bmbim_contract_states");
                approveSettingMap.put("function", "getGeneralleaderApprove");
                approveSettingMap.put("Target Location", "listHidden");
                approveColMap.put("name", "Bmbim_contract_states_generalleader_approve");
                approveColMap.put("label", "emxFramework.Attribute.Bmbim_contract_states_generalleader_approve");
                approveColMap.put("href", "${COMMON_DIR}/emxPromoteWithApprove.jsp");
                approveColMap.put("settings", approveSettingMap);
                columnMapList.add(approveColMap);
                
                Map rejectColMap = new HashMap();
                HashMap rejectSettingMap = new HashMap();
                rejectSettingMap.put("Registered Suite", "Framework");
                rejectSettingMap.put("Editable", "false");
                rejectSettingMap.put("Column Type", "program");
                rejectSettingMap.put("program", "Bmbim_contract_states");
                rejectSettingMap.put("function", "getGeneralleaderReject");
                rejectSettingMap.put("Target Location", "listHidden");
                rejectColMap.put("name", "Bmbim_contract_states_generalleader_reject");
                rejectColMap.put("label", "emxFramework.Attribute.Bmbim_contract_states_generalleader_reject");
                rejectColMap.put("href", "${COMMON_DIR}/emxPromoteWithReject.jsp");
                rejectColMap.put("settings", rejectSettingMap);
                columnMapList.add(rejectColMap);
            }
        } else if (person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_CLIENT)) {
            if (state.equals(Bmbim_contract_states_mxJPO.STATE_CLIENT_REVIEW)) {
                Map approveColMap = new HashMap();
                HashMap approveSettingMap = new HashMap();
                approveSettingMap.put("Registered Suite", "Framework");
                approveSettingMap.put("Editable", "false");
                approveSettingMap.put("Column Type", "program");
                approveSettingMap.put("program", "Bmbim_contract_states");
                approveSettingMap.put("function", "getClientApprove");
                approveSettingMap.put("Target Location", "listHidden");
                approveColMap.put("name", "Bmbim_contract_states_client_approve");
                approveColMap.put("label", "emxFramework.Attribute.Bmbim_contract_states_client_approve");
                approveColMap.put("href", "${COMMON_DIR}/emxPromoteWithApprove.jsp");
                approveColMap.put("settings", approveSettingMap);
                columnMapList.add(approveColMap);
                
                Map rejectColMap = new HashMap();
                HashMap rejectSettingMap = new HashMap();
                rejectSettingMap.put("Registered Suite", "Framework");
                rejectSettingMap.put("Editable", "false");
                rejectSettingMap.put("Column Type", "program");
                rejectSettingMap.put("program", "Bmbim_contract_states");
                rejectSettingMap.put("function", "getClientReject");
                rejectSettingMap.put("Target Location", "listHidden");
                rejectColMap.put("name", "Bmbim_contract_states_client_reject");
                rejectColMap.put("label", "emxFramework.Attribute.Bmbim_contract_states_client_reject");
                rejectColMap.put("href", "${COMMON_DIR}/emxPromoteWithReject.jsp");
                rejectColMap.put("settings", rejectSettingMap);
                columnMapList.add(rejectColMap);
            }
        } else {
            if (state.equals(Bmbim_contract_states_mxJPO.STATE_INPUT)) {
                Map promoteColMap = new HashMap();
                HashMap promoteSettingMap = new HashMap();
                promoteSettingMap.put("Registered Suite", "Framework");
                promoteSettingMap.put("Editable", "false");
                promoteSettingMap.put("Column Type", "program");
                promoteSettingMap.put("program", "Bmbim_contract_states");
                promoteSettingMap.put("function", "getSubmit");
                promoteSettingMap.put("Target Location", "listHidden");
                promoteColMap.put("name", "Bmbim_contract_states_submit");
                promoteColMap.put("label", "emxFramework.Attribute.Bmbim_contract_states_submit");
                promoteColMap.put("href", "${COMMON_DIR}/emxPromote.jsp");
                promoteColMap.put("settings", promoteSettingMap);
                columnMapList.add(promoteColMap);
            } else if (state.equals(Bmbim_contract_states_mxJPO.STATE_SIGNED)) {
                Map promoteColMap = new HashMap();
                HashMap promoteSettingMap = new HashMap();
                promoteSettingMap.put("Registered Suite", "Framework");
                promoteSettingMap.put("Editable", "false");
                promoteSettingMap.put("Column Type", "program");
                promoteSettingMap.put("program", "Bmbim_contract_states");
                promoteSettingMap.put("function", "startGathering");
                promoteSettingMap.put("Target Location", "listHidden");
                promoteColMap.put("name", "Bmbim_contract_states_startgathering");
                promoteColMap.put("label", "emxFramework.Attribute.Bmbim_contract_states_startgathering");
                promoteColMap.put("href", "${COMMON_DIR}/emxPromote.jsp");
                promoteColMap.put("settings", promoteSettingMap);
                columnMapList.add(promoteColMap);
            } else if (state.equals(Bmbim_contract_states_mxJPO.STATE_PAYMENT)) {
                Map promoteColMap = new HashMap();
                HashMap promoteSettingMap = new HashMap();
                promoteSettingMap.put("Registered Suite", "Framework");
                promoteSettingMap.put("Editable", "false");
                promoteSettingMap.put("Column Type", "program");
                promoteSettingMap.put("program", "Bmbim_contract_states");
                promoteSettingMap.put("function", "stopGathering");
                promoteSettingMap.put("Target Location", "listHidden");
                promoteColMap.put("name", "Bmbim_contract_states_stopgathering");
                promoteColMap.put("label", "emxFramework.Attribute.Bmbim_contract_states_stopgathering");
                promoteColMap.put("href", "${COMMON_DIR}/emxPromote.jsp");
                promoteColMap.put("settings", promoteSettingMap);
                columnMapList.add(promoteColMap);
            }
        }
        return columnMapList;
    }
    
    public Vector getSubmit(Context context, String[] args) throws Exception {
        // Get an object list which based on args.
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");
        
        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector oids = new Vector(objectList.size());
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            String str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Attribute.Bmbim_contract_states_submit", context.getLocale());
            oids.addElement(str);
        }
        // Return the object array.
        return oids;
    }
    
    public Vector getBranchleaderApprove(Context context, String[] args) throws Exception {
        // Get an object list which based on args.
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");
        
        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector oids = new Vector(objectList.size());
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            String str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Attribute.Bmbim_contract_states_branchleader_approve", context.getLocale());
            oids.addElement(str);
        }
        // Return the object array.
        return oids;
    }
    
    public Vector getBranchleaderReject(Context context, String[] args) throws Exception {
        // Get an object list which based on args.
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");
        
        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector oids = new Vector(objectList.size());
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            String str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Attribute.Bmbim_contract_states_branchleader_reject", context.getLocale());
            oids.addElement(str);
        }
        // Return the object array.
        return oids;
    }
    
    public Vector getGeneralleaderApprove(Context context, String[] args) throws Exception {
        // Get an object list which based on args.
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");
        
        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector oids = new Vector(objectList.size());
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            String str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Attribute.Bmbim_contract_states_generalleader_approve", context.getLocale());
            oids.addElement(str);
        }
        // Return the object array.
        return oids;
    }
    
    public Vector getGeneralleaderReject(Context context, String[] args) throws Exception {
        // Get an object list which based on args.
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");
        
        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector oids = new Vector(objectList.size());
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            String str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Attribute.Bmbim_contract_states_generalleader_reject", context.getLocale());
            oids.addElement(str);
        }
        // Return the object array.
        return oids;
    }
    
    public Vector getClientApprove(Context context, String[] args) throws Exception {
        // Get an object list which based on args.
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");
        
        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector oids = new Vector(objectList.size());
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            String str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Attribute.Bmbim_contract_states_client_approve", context.getLocale());
            oids.addElement(str);
        }
        // Return the object array.
        return oids;
    }
    
    public Vector getClientReject(Context context, String[] args) throws Exception {
        // Get an object list which based on args.
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");
        
        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector oids = new Vector(objectList.size());
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            String str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Attribute.Bmbim_contract_states_client_reject", context.getLocale());
            oids.addElement(str);
        }
        // Return the object array.
        return oids;
    }
    
    public Vector startGathering(Context context, String[] args) throws Exception {
        // Get an object list which based on args.
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");
        
        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector oids = new Vector(objectList.size());
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            String str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Attribute.Bmbim_contract_states_startgathering", context.getLocale());
            oids.addElement(str);
        }
        // Return the object array.
        return oids;
    }
    
    public Vector stopGathering(Context context, String[] args) throws Exception {
        // Get an object list which based on args.
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");
        
        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector oids = new Vector(objectList.size());
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            String str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Attribute.Bmbim_contract_states_stopgathering", context.getLocale());
            oids.addElement(str);
        }
        // Return the object array.
        return oids;
    }
}
