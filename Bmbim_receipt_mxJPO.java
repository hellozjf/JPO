import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.MapList;

import matrix.db.Context;
import matrix.db.JPO;

public class Bmbim_receipt_mxJPO extends emxDomainObject_mxJPO {

    public Bmbim_receipt_mxJPO(Context arg0, String[] arg1) throws Exception {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
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

    public Vector getActualmoney(Context context, String[] args) throws Exception {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");

        Vector actualmoney = new Vector(objectList.size());
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            DomainObject domainObject = DomainObject.newInstance(context, oid);
            String watertransport = domainObject.getInfo(context, "attribute[Bmbim_receipt_watertransport]");
            String headoffice = domainObject.getInfo(context, "attribute[Bmbim_receipt_headoffice]");
            String ground = domainObject.getInfo(context, "attribute[Bmbim_receipt_ground]");
            String plan = domainObject.getInfo(context, "attribute[Bmbim_receipt_plan]");
            String echo = domainObject.getInfo(context, "attribute[Bmbim_receipt_echo]");
            String building = domainObject.getInfo(context, "attribute[Bmbim_receipt_building]");
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
            actualmoney.addElement(String.valueOf(actual));
        }
        return actualmoney;
    }

    public Object createAndConnect(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String objectId = (String) paramMap.get("objectId"); // Part ID
        String newRDOId = (String) paramMap.get("New OID"); // RDO Id
        String strContractToReceiptRelationship = "Bmbim_contract_to_receipt";
        // Connect the part to RDO
        if (!newRDOId.equals("")) {
            setId(newRDOId);
            DomainObject domainObjectToType = newInstance(context, objectId);
            // Part Object
            DomainObject domainObjectFromType = newInstance(context, newRDOId); // RDO Object
            DomainRelationship.connect(context, domainObjectFromType, strContractToReceiptRelationship, domainObjectToType);
        }
        return new Boolean(true);
    }

    public static Boolean createCommandAccess(Context context, String[] args) throws Exception {
        // Define a boolean to return
        Boolean bFieldAccess = new Boolean(true);
        HashMap requestMap = (HashMap) JPO.unpackArgs(args);
        // Get the parameter values from "requestMap" - if required
        String objectId = (String) requestMap.get("objectId");
        String relId = (String) requestMap.get("relId ");
        String languageStr = (String) requestMap.get("languageStr");
        
//        System.out.println("hellozjf: objectId=" + objectId + " relId=" + relId + " languageStr=" + languageStr);
        DomainObject domainObject = DomainObject.newInstance(context, objectId);
        if (!Bmbim_contract_mxJPO.isAllowAccess(context, args, domainObject)) {
            bFieldAccess = new Boolean(false);
        }
        return bFieldAccess;
    }
}
