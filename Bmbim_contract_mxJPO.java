import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.aspose.slides.p7cce53cf.pe;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Person;
import matrix.util.StringList;

public class Bmbim_contract_mxJPO extends emxDomainObject_mxJPO {
// hellozjf
    public Bmbim_contract_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);
    }

    // ???Excel????
    public String index;
    public String id;
    public String name;
    public String state;
    public String signDate;
    public String totalMoney;
    public String scale;
    public String clientName;
    public String measure;
    public String geology;
    public String other;
    public String hasMeetingFee;

    public void setParameters(String index, String id, String name, String state, String signDate, String totalMoney, String scale, String clientName, String measure, String geology, String other, String hasMeetingFee) {
        this.index = index;
        this.id = id;
        this.name = name;
        this.state = state;
        this.signDate = signDate;
        this.totalMoney = totalMoney;
        this.scale = scale;
        this.clientName = clientName;
        this.measure = measure;
        this.geology = geology;
        this.other = other;
        this.hasMeetingFee = hasMeetingFee;
    }

    public static Vector getBmbim_contract_totalamount(Context context, String[] args) throws Exception {
        return getBmbim_contract_attribute(context, args, "Bmbim_contract_totalamount");
    }

    public static boolean setBmbim_contract_totalamount(Context context, String[] args) throws Exception {
        return setBmbim_contract_attribute(context, args, "Bmbim_contract_totalamount");
    }

    public static Vector getBmbim_contract_measure(Context context, String[] args) throws Exception {
        return getBmbim_contract_attribute(context, args, "Bmbim_contract_measure");
    }

    public static boolean setBmbim_contract_measure(Context context, String[] args) throws Exception {
        return setBmbim_contract_attribute(context, args, "Bmbim_contract_measure");
    }

    public static Vector getBmbim_contract_geology(Context context, String[] args) throws Exception {
        return getBmbim_contract_attribute(context, args, "Bmbim_contract_geology");
    }

    public static boolean setBmbim_contract_geology(Context context, String[] args) throws Exception {
        return setBmbim_contract_attribute(context, args, "Bmbim_contract_geology");
    }

    public static Vector getBmbim_contract_other(Context context, String[] args) throws Exception {
        return getBmbim_contract_attribute(context, args, "Bmbim_contract_other");
    }

    public static boolean setBmbim_contract_other(Context context, String[] args) throws Exception {
        return setBmbim_contract_attribute(context, args, "Bmbim_contract_other");
    }

    public static Vector getBmbim_contract_meetingcharge(Context context, String[] args) throws Exception {
        return getBmbim_contract_attribute(context, args, "Bmbim_contract_meetingcharge");
    }

    public static boolean setBmbim_contract_meetingcharge(Context context, String[] args) throws Exception {
        return setBmbim_contract_attribute(context, args, "Bmbim_contract_meetingcharge");
    }
    
    // ????????????????
    public static boolean isAllowAccess(Context context, String[] args, DomainObject domainObject) throws Exception {
        String current = domainObject.getInfo(context, "current");
        if (!Bmbim_contract_states_mxJPO.isLeader(context, args) && !current.equals(Bmbim_contract_states_mxJPO.STATE_INPUT)) {
            return false;
        } else {
            return true;
        }
    }

    public static Vector getBmbim_contract_attribute(Context context, String[] args, String attribute) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");

        Vector retVal = new Vector();
        for (int i = 0; i < objectList.size(); i++) {
            Map map = (Map) objectList.get(i);
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            DomainObject domainObject = DomainObject.newInstance(context, oid);
            String attr = domainObject.getInfo(context, "attribute[" + attribute + "]");
            
            if (! isAllowAccess(context, args, domainObject)) {
                attr = "-";
            }
            retVal.add(attr);
        }
        return retVal;
    }

    public static boolean setBmbim_contract_attribute(Context context, String[] args, String attribute) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");

        String objectId = (String) paramMap.get("objectId");
        String relId = (String) paramMap.get("relId");
        String newValue = (String) paramMap.get("New Value");
        String oldValue = (String) paramMap.get("Old Value");
        String languageStr = (String) requestMap.get("languageStr");

        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        String current = domainObject.getInfo(context, "current");
        if (!isAllowAccess(context, args, domainObject)) {
            return false;
        }
        Map map = new HashMap();
        map.put(attribute, newValue);
        domainObject.setAttributeValues(context, map);
        return true;
    }

    public MapList getAllContracts(Context context, String[] args) throws Exception {
        // Create a select which equals to MQL's select id.
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        // Find all the BOs whose Type is TestPart.
        MapList mapList = DomainObject.findObjects(context, "Bmbim_contract", "*", "", objectSelects);
        // Create an objectList, which add the map object from mapList.
        MapList objectList = new MapList();
        for (Iterator iter = mapList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            String id = (String) map.get(DomainConstants.SELECT_ID);
            // After getting object's id, you can do more things.
            DomainObject domainObject = DomainObject.newInstance(context, id);
            String state = domainObject.getInfo(context, "current");
            String user = context.getUser();
            Person person = new Person(user);

            boolean bAdd = false;
            if (state.equals(Bmbim_contract_states_mxJPO.STATE_INPUT)) {
                // ??????????????
                bAdd = true;
            } else if (state.equals(Bmbim_contract_states_mxJPO.STATE_BRANCHLEADER_REVIEW)) {
                // ?????????????branchleader?admin????
                if (person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_BRANCHLEADER) || person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_ADMIN)) {
                    bAdd = true;
                } else {
                    bAdd = false;
                }
            } else if (state.equals(Bmbim_contract_states_mxJPO.STATE_GENERALLEADER_REVIEW)) {
                // ???????????generalleader?admin????
                if (person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_GENERALLEADER) || person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_ADMIN)) {
                    bAdd = true;
                } else {
                    bAdd = false;
                }
            } else if (state.equals(Bmbim_contract_states_mxJPO.STATE_CLIENT_REVIEW)) {
                // ???????????client?admin????
                if (person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_CLIENT) || person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_ADMIN)) {
                    bAdd = true;
                } else {
                    bAdd = false;
                }
            } else {
                // ???????????????admin????
                if (person.isAssigned(context, Bmbim_contract_states_mxJPO.ROLE_ADMIN)) {
                    bAdd = true;
                } else {
                    bAdd = false;
                }
            }

            if (bAdd) {
                objectList.add(map);
            }
        }
        return objectList;
    }

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

    public Vector getSubPackage(Context context, String[] args) throws Exception {
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
            DomainObject domainObject = DomainObject.newInstance(context, oid);

            // Search All Projects
            double rec = 0;
            StringList objectSelects2 = new StringList();
            objectSelects2.add(DomainConstants.SELECT_ID);
            MapList objectList2 = domainObject.getRelatedObjects(context, "Bmbim_contract_to_project", "Bmbim_project", objectSelects2, new StringList(), false, true, (short) 0, null, null, 0);
            for (Iterator iter2 = objectList2.iterator(); iter2.hasNext();) {
                Map map2 = (Map) iter2.next();
                String oid2 = (String) map2.get(DomainConstants.SELECT_ID);
                DomainObject domainObject2 = DomainObject.newInstance(context, oid2);
                String subpackage = domainObject2.getInfo(context, "attribute[Bmbim_project_subpackage]");
                if (subpackage == null) {
                    subpackage = "0";
                }
                rec += Double.parseDouble(subpackage);
            }

            String attr = String.valueOf(rec);
            // ???????????????????
            if (!isAllowAccess(context, args, domainObject)) {
                attr = "-";
            }
            oids.addElement(attr);
        }
        // Return the object array.
        return oids;
    }

    public Vector getSubPackagePaid(Context context, String[] args) throws Exception {
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
            DomainObject domainObject = DomainObject.newInstance(context, oid);

            // Search All Projects
            double rec = 0;
            StringList objectSelects2 = new StringList();
            objectSelects2.add(DomainConstants.SELECT_ID);
            MapList objectList2 = domainObject.getRelatedObjects(context, "Bmbim_contract_to_project", "Bmbim_project", objectSelects2, new StringList(), false, true, (short) 0, null, null, 0);
            for (Iterator iter2 = objectList2.iterator(); iter2.hasNext();) {
                Map map2 = (Map) iter2.next();
                String oid2 = (String) map2.get(DomainConstants.SELECT_ID);
                DomainObject domainObject2 = DomainObject.newInstance(context, oid2);
                String subpackagepaid = domainObject2.getInfo(context, "attribute[Bmbim_project_subpackagepaid]");
                if (subpackagepaid == null) {
                    subpackagepaid = "0";
                }
                rec += Double.parseDouble(subpackagepaid);
            }

            String attr = String.valueOf(rec);
            // ???????????????????
            if (!isAllowAccess(context, args, domainObject)) {
                attr = "-";
            }
            oids.addElement(attr);
        }
        // Return the object array.
        return oids;
    }

    public Vector getSubPackageUnpaid(Context context, String[] args) throws Exception {
        // Get an object list which based on args.
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");

        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector oids = new Vector(objectList.size());
        // Look all the objects in the object list.
        Vector subPackage = getSubPackage(context, args);
        Vector subPackagePaid = getSubPackagePaid(context, args);
        for (int i = 0; i < objectList.size(); i++) {
            if (subPackage.get(i).equals("-")) {
                oids.addElement("-");
            } else {
                double subPackageUnpaid = Double.parseDouble((String) subPackage.get(i)) - Double.parseDouble((String) subPackagePaid.get(i));
                oids.addElement(String.valueOf(subPackageUnpaid));
            }
        }
        // Return the object array.
        return oids;
    }

    // public Vector getStatesI18N(Context context, String[] args) throws Exception
    // {
    // // Get an object list which based on args.
    // HashMap paramMap = (HashMap) JPO.unpackArgs(args);
    // MapList objectList = (MapList) paramMap.get("objectList");
    //
    // // Create an object array. The value of the object array will be
    // // displayed in the column.
    // Vector oids = new Vector(objectList.size());
    // // Look all the objects in the object list.
    // for (Iterator iter = objectList.iterator(); iter.hasNext();) {
    // Map map = (Map) iter.next();
    // // Get object's id, and add it into the object array.
    // String oid = (String) map.get(DomainConstants.SELECT_ID);
    // DomainObject domainObject = DomainObject.newInstance(context, oid);
    // String state = domainObject.getInfo(context, "current");
    // String str = null;
    // if (state.equals("contract_info_input")) {
    // str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context,
    // "emxFramework.State.Bmbim_contract.contract_info_input",
    // context.getLocale());
    // } else if (state.equals("contract_branchleader_review")) {
    // str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context,
    // "emxFramework.State.Bmbim_contract.contract_branchleader_review",
    // context.getLocale());
    // } else if (state.equals("contract_generalleader_review")) {
    // str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context,
    // "emxFramework.State.Bmbim_contract.contract_generalleader_review",
    // context.getLocale());
    // } else if (state.equals("contract_client_review")) {
    // str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context,
    // "emxFramework.State.Bmbim_contract.contract_client_review",
    // context.getLocale());
    // } else if (state.equals("contract_official_signed")) {
    // str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context,
    // "emxFramework.State.Bmbim_contract.contract_official_signed",
    // context.getLocale());
    // } else if (state.equals("contract_wait_payment")) {
    // str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context,
    // "emxFramework.State.Bmbim_contract.contract_wait_payment",
    // context.getLocale());
    // } else {
    // str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context,
    // "emxFramework.State.Bmbim_contract.contract_final_end", context.getLocale());
    // }
    //
    // oids.addElement(str);
    // }
    // // Return the object array.
    // return oids;
    // }

    public Vector getReceipts(Context context, String[] args) throws Exception {
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
            String str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.String.manage", context.getLocale());
            oids.addElement(str);
        }
        // Return the object array.
        return oids;
    }

    public Vector getProjects(Context context, String[] args) throws Exception {
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
            String str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.String.manage", context.getLocale());
            oids.addElement(str);
        }
        // Return the object array.
        return oids;
    }

    public Vector getStates(Context context, String[] args) throws Exception {
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
            String str = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.String.manage", context.getLocale());
            oids.addElement(str);
        }
        // Return the object array.
        return oids;
    }

    public Vector getReceived(Context context, String[] args) throws Exception {
        // Get an object list which based on args.
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");

        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector received = new Vector(objectList.size());
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            // System.out.println("oid:" + oid);
            DomainObject domainObject = DomainObject.newInstance(context, oid);

            // Search All Receipts
            double rec = 0;
            StringList objectSelects2 = new StringList();
            objectSelects2.add(DomainConstants.SELECT_ID);
            MapList objectList2 = domainObject.getRelatedObjects(context, "Bmbim_contract_to_receipt", "Bmbim_receipt", objectSelects2, new StringList(), false, true, (short) 0, null, null, 0);
            for (Iterator iter2 = objectList2.iterator(); iter2.hasNext();) {
                Map map2 = (Map) iter2.next();
                String oid2 = (String) map2.get(DomainConstants.SELECT_ID);
                DomainObject domainObject2 = DomainObject.newInstance(context, oid2);

                // Get Actual Money
                String watertransport = domainObject2.getInfo(context, "attribute[Bmbim_receipt_watertransport]");
                String headoffice = domainObject2.getInfo(context, "attribute[Bmbim_receipt_headoffice]");
                String ground = domainObject2.getInfo(context, "attribute[Bmbim_receipt_ground]");
                String plan = domainObject2.getInfo(context, "attribute[Bmbim_receipt_plan]");
                String echo = domainObject2.getInfo(context, "attribute[Bmbim_receipt_echo]");
                String building = domainObject2.getInfo(context, "attribute[Bmbim_receipt_building]");
                if (watertransport == null) {
                    watertransport = "0";
                }
                if (headoffice == null) {
                    headoffice = "0";
                }
                if (ground == null) {
                    ground = "0";
                }
                if (plan == null) {
                    plan = "0";
                }
                if (echo == null) {
                    echo = "0";
                }
                if (building == null) {
                    building = "0";
                }
                // System.out.println("watertransport:" + watertransport +
                // " headoffice:" + headoffice +
                // " ground:" + ground +
                // " plan:" + plan +
                // " echo:" + echo +
                // " building: " + building);
                double actual = Double.parseDouble(watertransport) + Double.parseDouble(headoffice) + Double.parseDouble(ground) + Double.parseDouble(plan) + Double.parseDouble(echo) + Double.parseDouble(building);
                // String actualmoney = domainObject2.getInfo(context,
                // "attribute[Bmbim_receipt_actualmoney]");
                // System.out.println("oid2:" + oid2 + " actualmoney:" + actualmoney);
                // rec += Double.parseDouble(actualmoney);
                rec += actual;
            }

            String name = String.valueOf(rec);
            if (!isAllowAccess(context, args, domainObject)) {
                name = "-";
            }
            received.addElement(name);
//            if (isAllowAccess(context, args, domainObject)) {
//                String url = "<a href=\"emxIndentedTable.jsp?program=Bmbim_contract:getAllRelatedReceipts&amp;table=Bmbim_receipt&amp;editLink=true&amp;selection=multiple&amp;toolbar=Bmbim_receipt_toolbar&amp;freezePane=Bmbim_receipt_id&amp;header=emxFramework.String.Bmbim_receipt_header&amp;objectId=" + oid + "\" target=\"_blank\">" + name + "</a>";
//                received.addElement(url);
//            } else {
//                String url = name;
//                received.addElement(url);
//            }
        }
        // Return the object array.
        return received;
    }

    public MapList getAllRelatedReceipts(Context context, String[] args) throws Exception {
        // Find domainObject by objectId
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        Iterator iter = paramMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
        }
        String objectId = (String) paramMap.get("objectId");
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        // Get domainObject relatedObjects
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        if (isAllowAccess(context, args, domainObject)) {
            MapList mapList = domainObject.getRelatedObjects(context, "Bmbim_contract_to_receipt", "Bmbim_receipt", objectSelects, new StringList(), false, true, (short) 0, null, null, 0);
            return mapList;
        } else {
            return new MapList();
        }
    }

    public MapList getAllRelatedProjects(Context context, String[] args) throws Exception {
        // Find domainObject by objectId
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        Iterator iter = paramMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
        }
        String objectId = (String) paramMap.get("objectId");
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        // Get domainObject relatedObjects
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        if (isAllowAccess(context, args, domainObject)) {
            MapList mapList = domainObject.getRelatedObjects(context, "Bmbim_contract_to_project", "Bmbim_project", objectSelects, new StringList(), false, true, (short) 0, null, null, 0);
            return mapList;
        } else {
            return new MapList();
        }
    }

    public MapList getCurrentContract(Context context, String[] args) throws Exception {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) paramMap.get("objectId");
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        String type = domainObject.getInfo(context, "type");
        String name = domainObject.getInfo(context, "name");
        String revision = domainObject.getInfo(context, "revision");

        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        MapList mapList = DomainObject.findObjects(context, type, name, revision, "*", "*", "", false, objectSelects);
        return mapList;
    }
}