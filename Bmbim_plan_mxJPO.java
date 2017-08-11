import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class Bmbim_plan_mxJPO extends emxDomainObject_mxJPO {


    public Bmbim_plan_mxJPO(Context arg0, String[] arg1) throws Exception {
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

    public Vector getActualTotalAmount(Context context, String[] args) throws Exception {
        // Get an object list which based on args.
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");

        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector actualTotalAmount = new Vector(objectList.size());
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            DomainObject domainObject = DomainObject.newInstance(context, oid);
//            String year = domainObject.getInfo(context, "attribute[Bmbim_plan_year]");
            String starttime = domainObject.getInfo(context, "attribute[Bmbim_plan_starttime]");
            String endtime = domainObject.getInfo(context, "attribute[Bmbim_plan_endtime]");
//            System.out.println("hellozjf: " + " starttime:" + starttime + " endtime:" + endtime);
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            Date dateStartTime = sdf.parse(starttime);
            Date dateEndTime = sdf.parse(endtime);

            // Search All Receipts
            double rec = 0;
            StringList objectSelects = new StringList();
            objectSelects.add(DomainConstants.SELECT_ID);
            MapList contracts = DomainObject.findObjects(context, "Bmbim_contract", "*", "", objectSelects);
            for (Iterator iter2 = contracts.iterator(); iter2.hasNext();) {
                Map map2 = (Map) iter2.next();
                String oid2 = (String) map2.get(DomainConstants.SELECT_ID);
                DomainObject domainObject2 = DomainObject.newInstance(context, oid2);
                String contract_totalamount = domainObject2.getInfo(context, "attribute[Bmbim_contract_totalamount]");
//                String contract_clientsign = domainObject2.getInfo(context, "attribute[Bmbim_contract_clientsign]");
                String contract_signdate = domainObject2.getInfo(context, "attribute[Bmbim_contract_signdate]");
                System.out.println("hellozjf: " + " contract_signdate:" + contract_signdate);
                Date dateContractSigndate = sdf.parse(contract_signdate);
//                if (contract_clientsign.contains(year)) {
//                    rec += Double.parseDouble(contract_totalamount);
//                }
                if (dateStartTime.compareTo(dateContractSigndate) <= 0 &&
                        dateContractSigndate.compareTo(dateEndTime) <= 0) {
                    rec += Double.parseDouble(contract_totalamount);
                }
            }

            actualTotalAmount.addElement(String.valueOf(rec));
        }
        // Return the object array.
        return actualTotalAmount;
    }

    public Vector getActualTotalPayment(Context context, String[] args) throws Exception {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");

        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector actualTotalPayment = new Vector(objectList.size());
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(); iter.hasNext();) {
            Map map = (Map) iter.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            DomainObject domainObject = DomainObject.newInstance(context, oid);
//            String year = domainObject.getInfo(context, "attribute[Bmbim_plan_year]");
            String starttime = domainObject.getInfo(context, "attribute[Bmbim_plan_starttime]");
            String endtime = domainObject.getInfo(context, "attribute[Bmbim_plan_endtime]");
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            Date dateStartTime = sdf.parse(starttime);
            Date dateEndTime = sdf.parse(endtime);
            double rec = 0;

            // Search All Receipts
            StringList objectSelects = new StringList();
            objectSelects.add(DomainConstants.SELECT_ID);
            MapList contracts = DomainObject.findObjects(context, "Bmbim_contract", "*", "", objectSelects);
            for (Iterator iter2 = contracts.iterator(); iter2.hasNext();) {
                Map map2 = (Map) iter2.next();
                String oid2 = (String) map2.get(DomainConstants.SELECT_ID);
                DomainObject domainObject2 = DomainObject.newInstance(context, oid2);
//                String contract_clientsign = domainObject2.getInfo(context, "attribute[Bmbim_contract_clientsign]");
                String contract_signdate = domainObject2.getInfo(context, "attribute[Bmbim_contract_signdate]");
                Date dateContractSigndate = sdf.parse(contract_signdate);
                
//                if (contract_clientsign.contains(year)) {
                if (dateStartTime.compareTo(dateContractSigndate) <= 0 &&
                        dateContractSigndate.compareTo(dateEndTime) <= 0) {
                    StringList objectSelects3 = new StringList();
                    objectSelects3.add(DomainConstants.SELECT_ID);
                    MapList objectList3 = domainObject2.getRelatedObjects(context, "Bmbim_contract_to_receipt", "Bmbim_receipt", objectSelects3, new StringList(), false, true, (short) 0, null, null, 0);
                    for (Iterator iter3 = objectList3.iterator(); iter3.hasNext();) {
                        Map map3 = (Map) iter3.next();
                        String oid3 = (String) map3.get(DomainConstants.SELECT_ID);
                        DomainObject domainObject3 = DomainObject.newInstance(context, oid3);
//                        String actualmoney = domainObject3.getInfo(context, "attribute[Bmbim_receipt_actualmoney]");
                        String watertransport = domainObject3.getInfo(context, "attribute[Bmbim_receipt_watertransport]");
                        String headoffice = domainObject3.getInfo(context, "attribute[Bmbim_receipt_headoffice]");
                        String ground = domainObject3.getInfo(context, "attribute[Bmbim_receipt_ground]");
                        String plan = domainObject3.getInfo(context, "attribute[Bmbim_receipt_plan]");
                        String echo = domainObject3.getInfo(context, "attribute[Bmbim_receipt_echo]");
                        String building = domainObject3.getInfo(context, "attribute[Bmbim_receipt_building]");
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
//                        System.out.println("watertransport:" + watertransport +
//                                " headoffice:" + headoffice +
//                                " ground:" + ground +
//                                " plan:" + plan +
//                                " echo:" + echo +
//                                " building: " + building);
                        double actual = Double.parseDouble(watertransport) + Double.parseDouble(headoffice) +
                                Double.parseDouble(ground) + Double.parseDouble(plan) +
                                Double.parseDouble(echo) + Double.parseDouble(building);
//                        System.out.println("oid2:" + oid2 + " actualmoney:" + actualmoney);
//                        rec += Double.parseDouble(actualmoney);
                        rec += actual;
                    }
//                    StringList objectSelects3 = new StringList();
//                    objectSelects3.add(DomainConstants.SELECT_ID);
//                    MapList receipts = DomainObject.findObjects(context, "Bmbim_receipt", "*", "attribute[Bmbim_poid]==" + oid2, objectSelects3);
//                    for (Iterator iter3 = receipts.iterator(); iter3.hasNext();) {
//                        Map map3 = (Map) iter3.next();
//                        String oid3 = (String) map3.get(DomainConstants.SELECT_ID);
//                        DomainObject domainObject3 = DomainObject.newInstance(context, oid3);
//                        String name3 = domainObject3.getInfo(context, "name");
//                        String poid = domainObject3.getInfo(context, "attribute[Bmbim_poid]");
//                        String actualmoney = domainObject3.getInfo(context, "attribute[Bmbim_receipt_actualmoney]");
//                        System.out.println("hellozjf: " + " name3:" + name3 + " poid:" + poid + " oid2:" + oid2 + " actualmoney:" + actualmoney);
//                        rec += Double.parseDouble(domainObject3.getInfo(context, "attribute[Bmbim_receipt_actualmoney]"));
//                    }
                }
            }

            actualTotalPayment.addElement(String.valueOf(rec));
        }
        // Return the object array.
        return actualTotalPayment;
    }

    public Vector getTotalAmountPercentage(Context context, String[] args) throws Exception {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");

        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector actualTotalAmountPecentage = new Vector(objectList.size());
        Vector actualTotalAmount = getActualTotalAmount(context, args);
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(), iter2 = actualTotalAmount.iterator(); iter.hasNext() && iter2.hasNext();) {
            Map map = (Map) iter.next();
            String actual = (String) iter2.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            DomainObject domainObject = DomainObject.newInstance(context, oid);
            String plan = domainObject.getInfo(context, "attribute[Bmbim_plan_totalamount]");

            // System.out.println("hellozjf: " + "actual:" + actual + " plan:" + plan + "
            // oid:" + oid);
            actualTotalAmountPecentage.addElement(String.valueOf(Double.parseDouble(actual) / Double.parseDouble(plan) * 100) + "%");
        }
        return actualTotalAmountPecentage;
    }

    public Vector getTotalPaymentPercentage(Context context, String[] args) throws Exception {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) paramMap.get("objectList");

        // Create an object array. The value of the object array will be
        // displayed in the column.
        Vector actualTotalPaymentPecentage = new Vector(objectList.size());
        Vector actualTotalPayment = getActualTotalPayment(context, args);
        // Look all the objects in the object list.
        for (Iterator iter = objectList.iterator(), iter2 = actualTotalPayment.iterator(); iter.hasNext() && iter2.hasNext();) {
            Map map = (Map) iter.next();
            String actual = (String) iter2.next();
            // Get object's id, and add it into the object array.
            String oid = (String) map.get(DomainConstants.SELECT_ID);
            DomainObject domainObject = DomainObject.newInstance(context, oid);
            String plan = domainObject.getInfo(context, "attribute[Bmbim_plan_totalpayment]");

            // System.out.println("hellozjf: " + "actual:" + actual + " plan:" + plan + "
            // oid:" + oid);
            actualTotalPaymentPecentage.addElement(String.valueOf(Double.parseDouble(actual) / Double.parseDouble(plan) * 100) + "%");
        }
        return actualTotalPaymentPecentage;
    }
}
