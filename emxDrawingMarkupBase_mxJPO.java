/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.engineering.*;

import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;

/**
 * The <code>emxDrawingMarkupBase</code> class contains implementation code for emxECR.
 *
 * @version EC Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */

public class emxDrawingMarkupBase_mxJPO extends emxDomainObject_mxJPO
{
    /** state "Plan ECO" for the "ECR Standard" policy. */
  /*  public static final String STATE_ECRSTANDARD_PLANECO =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_ECR_STANDARD,
                                           "state_PlanECO");*/

    /**
    * Constructor.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @throws Exception if the operation fails.
    * @since EC Rossini.
    */
    public emxDrawingMarkupBase_mxJPO (Context context, String[] args)
      throws Exception
    {
        super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return an int.
     * @throws Exception if the operation fails.
     * @since EC Rossini.
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception("must specify method on emxECO invocation");
        }
        return 0;
    }


    /**
	 * checks the edit mode of the web form display.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
     *        0 - HashMap containing one Map entry for the key "paramMap"
     * 			This Map contains the arguments passed to the jsp which called this method.
	 * @return Object - boolean true if the omode is view
	 * @throws Exception if operation fails
	 * @since EngineeringCentral 10-5-Next - Copyright (c) 2003, MatrixOne, Inc.
	 */

    public Object isViewMode(Context context, String[] args)
		throws Exception
	{

        //return true if it is not view mode.
        Boolean isEditMode = (Boolean)isEditMode(context, args);
        return Boolean.valueOf(!isEditMode.booleanValue());
	}


    /**
	 * checks the view mode of the web form display.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
     *        0 - HashMap containing one Map entry for the key "paramMap"
     * 			This Map contains the arguments passed to the jsp which called this method.
	 * @return Object - boolean true if the omode is view
	 * @throws Exception if operation fails
	 * @since EngineeringCentral 10-5-Next - Copyright (c) 2003, MatrixOne, Inc.
	 */

    public Object isEditMode(Context context, String[] args)
		throws Exception
	{

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strMode = (String) programMap.get("mode");
        Boolean isEditMode = Boolean.FALSE;

        // check the mode of the web form, if it is view assign true to return value
        if( strMode != null && "edit".equalsIgnoreCase(strMode) ) {
            isEditMode = Boolean.TRUE;
        }

		return isEditMode;
	}


    /**
     * Connects the ECR to Drawing Markup
     * @param context the eMatrix <code>Context</code> object
     * @param args
     *      0 - HashMap containing one Map entry for the key "paramMap"
     * 	This Map contains the arguments passed to the jsp which called this method.
     * @return Object - boolean true if the operation is successful
     * @throws Exception if operation fails
     * @since EngineeringCentral 10-5-Next - Copyright (c) 2003, MatrixOne, Inc.
     */

      public Object updateECR(Context context, String[] args)
      throws Exception
      {
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
          HashMap paramMap = (HashMap) programMap.get("paramMap");

          String objectId = (String) paramMap.get("objectId");
          String oldECRName = (String) paramMap.get("Old value");
          String newECRId = (String) paramMap.get("New OID");
          String strECRSupportingDocumentRelationship = DomainConstants.RELATIONSHIP_ECR_SUPPORTING_DOCUMENT;

          setId(objectId);
		  java.util.List ecrList = new MapList();
		  if (oldECRName == null || "null".equals(oldECRName)) {
              oldECRName = "";
          }

		  if (!"".equals(oldECRName))
		  {
			StringList ObjectSelectsList = new StringList(DomainConstants.SELECT_ID);
			StringList RelSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
			String strECRType = DomainConstants.TYPE_ECR;

            StringBuffer sbWhereCondition = new StringBuffer(25);
			sbWhereCondition = sbWhereCondition.append("name==\"");
			sbWhereCondition = sbWhereCondition.append(oldECRName);
			sbWhereCondition = sbWhereCondition.append("\"");
			String strWhereCondition = sbWhereCondition.toString();

			//Get the existing ECRs connected to the Drawing Markup
            ecrList =
				getRelatedObjects(
					context,
					strECRSupportingDocumentRelationship,
					strECRType+","+ChangeConstants.TYPE_CHANGE_REQUEST,
					ObjectSelectsList,
					RelSelectsList,
					true,
					true,
					(short) 1,
					strWhereCondition,
					DomainConstants.EMPTY_STRING);

            if (ecrList != null)
			{
				String strRelId = (String) ((Hashtable) ecrList.get(0)).get(
						DomainConstants.SELECT_RELATIONSHIP_ID);
				//Disconnecting the existing relationship
				DomainRelationship.disconnect(context, strRelId);
			}
		  }

 		  if (newECRId == null || "null".equals(newECRId)) {
              newECRId = "";
          }

		  //connect the newly selected ECR to Drawing Markup
          if (!"".equals(newECRId))
		  {
             setId(newECRId);
			 DomainObject domainObjectToType = newInstance(context, objectId);
			 DomainRelationship.connect(context,this,strECRSupportingDocumentRelationship,domainObjectToType);
		  }

          // On successful update return the Boolean object
          return Boolean.TRUE;
      }

  	/**
  	 * Update program to connect the Drawing Markup with the ECR
  	 *
  	 * @param context
  	 * @param args
  	 * @throws Exception
  	 * @Since R212
  	 */
  	public void connectECRSupportingDocumentRel(Context context, String[] args) throws Exception {
  		HashMap progMap = (HashMap) JPO.unpackArgs(args);
  		HashMap paramMap = (HashMap) progMap.get("paramMap");

  		String fromObjectId = (String) paramMap.get("New OID");
  		String toObjectId = (String) paramMap.get("objectId");

  		if ((fromObjectId != null) && !"".equals(fromObjectId)) {
  			try {
  				DomainObject fromBusObj = new DomainObject(fromObjectId);
  				DomainObject toBusObj = new DomainObject(toObjectId);

  				DomainRelationship.connect(context, fromBusObj,
  						RELATIONSHIP_ECR_SUPPORTING_DOCUMENT, toBusObj);
  			} catch (Exception e) {
  				e.printStackTrace();
  				throw e;
  			}
  		}
  	}
	/**
	 * Does setting the title attribute of Drawing Markup after creation under Drawing Print context
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds ParamMap
	 * @throws Exception if any operation fails.
  	 * @Since R212
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void performPostProcessConnectDrawingMarkup(Context context, String[] args) throws Exception {
        HashMap map = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) map.get("paramMap");
        String markupId =(String) paramMap.get("objectId");
        DomainObject doMarkup= new DomainObject(markupId);
        doMarkup.setAttributeValue(context, ATTRIBUTE_TITLE, (String)doMarkup.getName(context));
    }
	/**
	 * To create the Drawing Markup object from create component
	 *
	 * @param context
	 * @param args
	 * @return Map
	 * @throws Exception
	 * @Since R212
	 */
  @com.matrixone.apps.framework.ui.CreateProcessCallable
  public Map createDrawingMarkupJPO(Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String[] app = {"ENO_PRT_TP"};
		ComponentsUtil.checkLicenseReserved(context, app);
		String sType =(String)	programMap.get("TypeActual");
		String sPolicy =(String) programMap.get("Policy");
		String sDescription = (String) programMap.get("Description");
		Markup markup = new Markup();
		markup.create(context,
                sType,
                null,
                sPolicy,
                sDescription);
		String markupId = markup.getId(context);
		HashMap mapReturn = new HashMap(1);
		mapReturn.put("id", markupId);

		return mapReturn;
	}

}

