import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;

import matrix.db.Context;
import matrix.db.JPO;

public class Bmbim_project_mxJPO extends emxDomainObject_mxJPO {
    
    public Bmbim_project_mxJPO(Context arg0, String[] arg1) throws Exception {
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
    
    public Vector getSubPackageUnpaid(Context context, String[] args) throws Exception {
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
            String totalmoney = domainObject.getInfo(context, "attribute[Bmbim_project_totalmoney]");
            String subpackage = domainObject.getInfo(context, "attribute[Bmbim_project_subpackage]");
            String paid = domainObject.getInfo(context, "attribute[Bmbim_project_subpackagepaid]");
            if (totalmoney == null) {
                totalmoney = "0";
            }
            if (subpackage == null) {
                subpackage = "0";
            }
            if (paid == null) {
                paid = "0";
            }
            double unpaid = Double.parseDouble(subpackage) - Double.parseDouble(paid);
            oids.addElement(String.valueOf(unpaid));
        }
        // Return the object array.
        return oids;
    }
}
