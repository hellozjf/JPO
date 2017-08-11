/*
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  All Rights Reserved.
 */

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;

/**
 * The <code>emxDomainObjectBase</code> class implements the DomainObject bean.
 */
public class emxDomainObjectBase_mxJPO extends DomainObject {

    private static final long serialVersionUID = -8595122082676574726L;

	/**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        args[0] - id of the business object
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.1
     */
    public emxDomainObjectBase_mxJPO(Context context, String[] args) throws Exception {
   		super();
    	if(args != null && args.length > 0) {
    		setId(args[0]);
    	}
  	}

    /**
     * This method is used to check if atleast one object is connected through specific relationship, with the expand Limit to 1.
	 *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        args[0] - id of the business object
     *        args[1] - side of expand (from/to)
     *        args[2] - relationship name
     *        
     * @return String true - if the one object is connected.
     *				  false - if not even a single object is connected.
     *
     * @throws Exception if the operation fails
     * @since AEF BX3-HFx
     */
    public String hasRelationship(Context context, String args[]) throws Exception {
    	 String result = "false";
    	 boolean fromSide = false;
    	 boolean toSide = false;
    	 String objectId = args[0];
    	 if(args[1].equalsIgnoreCase("from")){
    		 fromSide = true;
    	 } else {
    		 toSide = true;
    	 }
    	 StringList objectSelects = new StringList(6);
    	 objectSelects.addElement("type");
    	 objectSelects.addElement("name");
    	 objectSelects.addElement("revision");
    	 StringList relselects = new StringList();

    	 DomainObject domObj = new DomainObject(objectId);
    	 MapList mList = null;

    	 try {
    		 mList = domObj.getRelatedObjects(context,
    				 args[2],
    		         "*",
    		         objectSelects,
    		         relselects,
    		         toSide,
    		         fromSide,
    		         (short)1,
    		         null,
    		         null,
    		         1);

    		 if(mList.size() > 0){
    			 result = "true";
    		 }
    	 }catch (Exception ex){
    		 System.out.println("Exception thrown in hasRelationship::"+ex.toString());
    	 }
    	 return result;
    }

	/**
	 * This method is used to update the objects updatestamp on the configured triggered event.
	 * 
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        args[0] - id of the business object
     * @throws Exception if the operation fails
     * @since 3DEXPERIENCER2015x(R417)
     * @author GUDLAVALLETI Sreenivasa [WGI]
	 */
	public static void setUpdatestamp(Context context, String[] args) throws Exception {
		try
		{
			String objectId = args[0];
			String UUID = matrix.util.UUID.getNewUUIDHEXString();
			DomainObject obj = DomainObject.newInstance(context, objectId);
			obj.setUpdateStamp(context, UUID);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
    }
}
