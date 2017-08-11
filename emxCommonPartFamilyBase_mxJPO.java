/**
 * emxCommonPartFamilyBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import matrix.db.*;
import matrix.util.*;
import java.util.*;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>emxCommonPartFamilyBase</code> class contains utility methods for
 * getting Part related data to feed configurable tables for PartFamily tree categories
 *
 * @since Common 10.6
 * @grade 0
 */
public class emxCommonPartFamilyBase_mxJPO extends emxDomainObject_mxJPO
{
	protected static final String RELATIONSHIP_SUBCLASS = PropertyUtil.getSchemaProperty("relationship_Subclass");
	protected static final String TYPE_CLASSIFICATION = PropertyUtil.getSchemaProperty("type_Classification");
	protected static final String TYPE_LIBRARIES = PropertyUtil.getSchemaProperty("type_Libraries");

	/**
	 * Constructor
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since Common 10.6
	 * @grade 0
	 */
	public emxCommonPartFamilyBase_mxJPO (Context context, String[] args)
		throws Exception
	{
        super(context, args);
	}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @returns nothing
	 * @throws Exception if the operation fails
	 * @since Common 10.6
	 * @grade 0
	 */
	public int mxMain(Context context, String[] args) throws Exception
	{
		if (!context.isConnected())
			throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
		return 0;
	}

	/**
	 * returns the MapList of Parts directly connected to a Part Family
	 * It is called in the Href of Parts command of the Part Family menu.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds all arguments that are being passed in the to emxTable.jsp in the above specified Command Href.
	 * @returns MapList of Object IDs.
	 * @throws FrameworkException if the operation fails
	 * @since Common 10.6
	 * @grade 0
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public static MapList getParts(Context context, String[] args) throws FrameworkException
	{
		MapList partList = new MapList();
		try
		{
			SelectList selectStmts = new SelectList(1);
			selectStmts.addElement(DomainObject.SELECT_ID);

			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			String partFamilyId    = (String)paramMap.get("objectId");

			String RELATIONSHIP_CLASSIFIED_ITEM = PropertyUtil.getSchemaProperty(context,"relationship_ClassifiedItem");

			DomainObject partFamilyObj = new DomainObject(partFamilyId);
			partList = (MapList)partFamilyObj.getRelatedObjects(context,
			                                                    RELATIONSHIP_CLASSIFIED_ITEM,
			                                                    "*",
			                                                    selectStmts,
			                                                    new StringList(),
			                                                    false,
			                                                    true,
			                                                    (short)1,
			                                                    null,
			                                                    null);
		}
		catch(Exception exp)
		{
			throw new FrameworkException(exp.toString());
		}
		return partList;
	}

	public static HashMap getRangeValuesForNameGenerator(Context context, String[] args) throws FrameworkException
	{
		HashMap rangeMap = new HashMap();

		try
		{
			StringList fieldChoices = new StringList();
			StringList fieldDisplayChoices = new StringList();
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			String language = (String)paramMap.get("languageStr");
			String on = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(language), "emxComponents.PartFamily.NameGenerator.On");
			String off = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(language), "emxComponents.PartFamily.NameGenerator.Off");
			//Modifed for IR-057820
			fieldChoices.add("TRUE");
			fieldChoices.add("FALSE");
			fieldDisplayChoices.add(on);
			fieldDisplayChoices.add(off);
			rangeMap.put("field_choices", fieldChoices);
			rangeMap.put("field_display_choices", fieldDisplayChoices);
		}catch(Exception ex)
		{
			throw new FrameworkException(ex.toString());
		}


		return rangeMap;
	}

	public static StringList getValueForNameGenerator(Context context, String[] args) throws FrameworkException
	{
		StringList fieldValues = new StringList();
		try
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			HashMap requestMap = (HashMap)programMap.get("requestMap");
			String objectId = (String)paramMap.get("objectId");
			String language = (String)paramMap.get("languageStr");
			DomainObject obj = new DomainObject(objectId);
			String mode = (String)requestMap.get("mode");
			String value = obj.getAttributeValue(context,"Part Family Name Generator On");
			
			if(!UIUtil.isNullOrEmpty(mode) && "edit".equalsIgnoreCase(mode)) //Modified for IR-170028			
			//if(mode != null && !"".equals(mode))
			{
				//if(mode.equalsIgnoreCase("edit"))
				//{
					fieldValues.add(value);
				//}
			}
			else
			{
				if(value != null && !"".equals(value))
				{
					if("True".equalsIgnoreCase(value))
					{
						fieldValues.add(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(language), "emxComponents.PartFamily.NameGenerator.On"));
					}
					else
					{
						fieldValues.add(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", new Locale(language), "emxComponents.PartFamily.NameGenerator.Off"));
					}
				}
			}

		}
		catch(Exception ex)
		{
			throw new FrameworkException(ex.toString());
		}
		return fieldValues;
	}

	public static void updateValueForNameGenerator(Context context, String[] args) throws FrameworkException
	{
		try
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			HashMap fieldMap = (HashMap)programMap.get("fieldMap");
			String objectId = (String)paramMap.get("objectId");
			String newValue = (String)paramMap.get("New Value");
			String[] newValues = (String[])paramMap.get("New Values");
			DomainObject obj = new DomainObject(objectId);
			obj.setAttributeValue(context, "Part Family Name Generator On", newValue);

		}catch(Exception ex)
		{
			throw new FrameworkException(ex.toString());
		}
	}

    /**
     * Update the 'Classification Class' and its parent objects' count as a result
     * of a classified item being revised or cloned.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments:
     *    [0]:  ${FROMOBJECTID}
     *    [1]:  ${TOOBJECTID}
     *    [2]:  ${PARENTEVENT}
     *    [3]:  ${NEWRELID}
     * @return int
     * @throws Exception if the operation fails
     * @since EngineeringCentral X3
     * @grade 0
     */
    public int updateCount(Context context, String[] args) throws Exception
    {
        if(args == null || (args !=null && args.length < 3))
        {
            throw new Exception ("ERROR - Invalid number of arguments");
        }

        int ret = 0;

        //arg[0]: classification object id
        DomainObject dmObj = (DomainObject) DomainObject.newInstance(context, args[0]);
        String parentEvent = args[2];   //revise,clone,modify
        String newRelId = args[3];      //new relationship id

        //update the count as a result of a classified item being revised
        if(("revise".equals(parentEvent) || "clone".equals(parentEvent)) && newRelId != null && newRelId.length() > 0)
        {
            try
            {
               ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING); //366577
               ContextUtil.startTransaction(context, true);
                String attrCountSel = "attribute[" + dmObj.ATTRIBUTE_COUNT + "]";

                String countStr = dmObj.getInfo(context, attrCountSel);
                int count = (new Integer(countStr)).intValue();
                count++;

                dmObj.setAttributeValue(context, dmObj.ATTRIBUTE_COUNT, Integer.toString(count));

                //Update other parent's count as well
                StringList busSels = new StringList();
                busSels.add(dmObj.SELECT_ID);
                busSels.add(attrCountSel);

                String rels = RELATIONSHIP_SUBCLASS;
                StringBuffer types = new StringBuffer(TYPE_CLASSIFICATION);
                types.append(",");
                types.append(TYPE_LIBRARIES);

                MapList result = dmObj.getRelatedObjects(context,
                                          rels,
                                          types.toString(),
                                          busSels,
                                          null,
                                          true,
                                          false,
                                          (short)0,
                                          null,
                                          null);

                if(result != null && result.size() > 0)
                {
                    for(int i=0; i < result.size(); i++)
                    {
                        Map map = (Map) result.get(i);
                        String sObjId = (String) map.get(dmObj.SELECT_ID);
                        String sCount = (String) map.get(attrCountSel);
                        int objCounts = (new Integer(sCount)).intValue();
                        objCounts++;

                        DomainObject parentObj =
                          (DomainObject) DomainObject.newInstance(context,sObjId);

                        parentObj.setAttributeValue(context, dmObj.ATTRIBUTE_COUNT, Integer.toString(objCounts));
                    }
                }

                ContextUtil.commitTransaction(context);
            }catch(Exception ex)
            {
                //ret = 1;
                ContextUtil.abortTransaction(context);
                throw ex;
            }
            finally { ContextUtil.popContext(context); } //366577
         }

        return ret;
    }

}
