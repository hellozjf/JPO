/*
 ** emxPartFamilyConversionBase
 **
 ** Copyright (c) 1992-2015 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 **
 */
import  java.util.*;
import  java.io.BufferedWriter;
import com.matrixone.apps.domain.util.ContextUtil;
import  matrix.db.*;
import  matrix.util.*;

/**
 * The <code>emxPartFamilyConversionBase.java</code>
 * class contains script to migrate 'Part Family Member' relationship to
 * 'Classified Item', 'Part Family' policy to 'Classification' policy
 * Changes "Part Issuer" type objects to "Part Family"
 *
 * @since AEF 10-6
 */
public class emxPartFamilyConversionBase_mxJPO
{
    private String SELECT_PART_FAMILY_MEMBER_RELATIONSHIP_NAME = null;
    private String RELATIONSHIP_CLASSIFIED_ITEM     = null;
    private String POLICY_CLASSIFICATION            = null;
    private String TYPE_PART_FAMILY                 = null;
    private String POLICY_PART_FAMILY               = null;
    private String STATE_EXISTS                     = null;
    private String STATE_ACTIVE                     = null;
    private String TYPE_PART_ISSUER                 = null;
    private String POLICY_PART_ISSUER_STATE_EXISTS  = null;
    private String POLICY_PART_ISSUER               = null;
    private String ATTRIBUTE_COUNT                  = null;
    private boolean isTypePartIssuerExists          = true;
    private boolean duplicatePartIssuerExists       = false;

   /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     *
     * @since AEF 10-6
     */

    public emxPartFamilyConversionBase_mxJPO (Context context, String[] args) throws Exception
    {
        SELECT_PART_FAMILY_MEMBER_RELATIONSHIP_NAME = "from[" + emxAdminCache_mxJPO.getName(context, "relationship_PartFamilyMember") + "].id";
        RELATIONSHIP_CLASSIFIED_ITEM     = emxAdminCache_mxJPO.getName(context, "relationship_ClassifiedItem");
        POLICY_CLASSIFICATION            = emxAdminCache_mxJPO.getName(context, "policy_Classification");
        TYPE_PART_FAMILY                 = emxAdminCache_mxJPO.getName(context, "type_PartFamily");
        POLICY_PART_FAMILY               = emxAdminCache_mxJPO.getName(context, "policy_PartFamily");
        STATE_EXISTS                     = emxAdminCache_mxJPO.getStateName(context, "policy_PartFamily", "state_Exists");
        STATE_ACTIVE                     = emxAdminCache_mxJPO.getStateName(context, "policy_Classification", "state_Active");
        ATTRIBUTE_COUNT                  = emxAdminCache_mxJPO.getName(context, "attribute_Count");

        // it is put in try/catch intentionally to ensure migration to proceed without any errors
        // if the Part Issuer type is not found in the database -- on clean database
        try
        {
            TYPE_PART_ISSUER                 = emxAdminCache_mxJPO.getName(context, "type_PartIssuer");
            POLICY_PART_ISSUER               = emxAdminCache_mxJPO.getName(context, "policy_PartIssuer");
            POLICY_PART_ISSUER_STATE_EXISTS  = emxAdminCache_mxJPO.getStateName(context, "policy_PartIssuer", "state_Exists");
        }
        catch(Exception e)
        {
            isTypePartIssuerExists = false;
        }

        if(TYPE_PART_ISSUER == null || POLICY_PART_ISSUER == null )
        {
			isTypePartIssuerExists = false;
		}
    }

  /**
    * Main entry point.
    * This method is executed if a specific method is not specified.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return an integer status code (0 = success)
    * @throws Exception if the operation fails
    * @since AEF 10-6
    */
    public int mxMain (Context context, String[] args) throws Exception
    {
        if(!context.isConnected())
        {
            throw new Exception("not supported on desktop client");
        }

        return 0;
    }

  /**
    * Migrate 'Part Family Member' relationship to 'Classified Item'
    * For Part Family objects modify policy from 'Part Family' to 'Classification'
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return an integer status code (0 = success)
    * @throws Exception if the operation fails
    * @since AEF 10-6
    */
    public int migrate(Context context,String[] args)
             throws Exception
    {
        boolean status  = false;
        String  sCmd    = null;
        String  sResult = null;

		// if the coversion was already run sucessfully, then return
		sCmd = "print program eServiceSystemInformation.tcl select property[ConversionPartFamilyMemberRelPartFamilyPolicy].value dump |";
		sResult = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
		if(sResult.equals("Executed"))
		{
            emxInstallUtil_mxJPO.println(context, "\n >ERROR: The coversion was previously run sucessfully : \n");
			return 0;
		}

        try
        {
            sCmd = "trigger off";
            sResult = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
            status = migratePartFamilyData(context);

            if(status)
            {
                sCmd = "modify program eServiceSystemInformation.tcl property ConversionPartFamilyMemberRelPartFamilyPolicy value Executed";
                sResult = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);
                emxInstallUtil_mxJPO.println(context, "\n Part Family,Part Issuer migration completed successfully : \n");
            }
            else
            {
                emxInstallUtil_mxJPO.println(context, "\n >ERROR: Part Family,Part Issuer migration failed: \n");
            }
        }
        catch (Exception ex)
        {
            emxInstallUtil_mxJPO.println(context, "\n >ERROR: In migrate method Exception :" + ex.getMessage() + " \n");
            throw ex;
        }
        finally
        {
            sCmd = "trigger on";
            sResult = emxInstallUtil_mxJPO.executeMQLCommand(context, sCmd);

        }

        return 0;
    }

  /**
    * Helper method for Part Family data migration
    *
    * @param context - The eMatrix <code>Context</code> object
    * @return void
    * @throws Exception if the operation fails
    * @since AEF 10-6
    * @grade 0
    */
    private boolean migratePartFamilyData(Context context)
             throws Exception
    {
        boolean migrationStatus = false;
        try
        {
            matrix.db.Policy cPolicy =  new matrix.db.Policy(POLICY_CLASSIFICATION);

            // Get a MapList of Part Family, Part Issuer objects in the database
            // that need to be migrated

            StringList objectSelects = new StringList (8);
            objectSelects.addElement ("id");
            objectSelects.addElement ("type");
            objectSelects.addElement ("name");
            objectSelects.addElement ("revision");
            objectSelects.addElement ("vault");
            objectSelects.addElement ("current");
            objectSelects.addElement ("policy");
            objectSelects.addElement (SELECT_PART_FAMILY_MEMBER_RELATIONSHIP_NAME);

            // query for all Part Family objects, this will include Part Issuer objects
            // since it is sub-type of Part Family
            matrix.db.Query query = new matrix.db.Query("");

            query.open(context);
            query.setBusinessObjectType(TYPE_PART_FAMILY);
            query.setBusinessObjectName("*");
            query.setBusinessObjectRevision("*");
            query.setOwnerPattern("*");
            query.setVaultPattern("*");
            query.setWhereExpression("");
            query.setExpandType(true);

            BusinessObjectWithSelectList list = new BusinessObjectWithSelectList(1);
            QueryIterator qItr=null;
            try
            {
                ContextUtil.startTransaction(context,false);
                qItr = query.getIterator(context,objectSelects,(short)1000);
                while(qItr.hasNext())
                    list.addElement(qItr.next());


                ContextUtil.commitTransaction(context);
             }
             catch(Exception ex)
             {
                ContextUtil.abortTransaction(context);
                throw new Exception(ex.toString());
             }
             finally {
			         qItr.close();
    		 }
            ArrayList partFamilyList = null;

            if( list != null && list.size() > 0)
            {
                partFamilyList = toMapList(list);
            }

            query.close(context);

            String command = null;

            // loop thru the list, change the policy to 'Classification'
            // if the type is "Part Issuer'
            // i.e. this change is not required for 'Part Family'
            //
            // For each of these objects, get the connected "Part Family Member"
            // relationships, change them to "Classified Item"

            if( partFamilyList != null && partFamilyList.size() > 0)
            {
                Iterator partFamilyItr = partFamilyList.iterator();
                String objectId     = null;
                String objectType   = null;
                String objectName   = null;
                String objectRev    = null;
                String objectVault  = null;
                String objectState  = null;
                String objectPolicy = null;
                StringList partFamiltMemberRelList = null;
                Iterator partFamiltMemberRelItr = null;
                BusinessObject busObject = null;
                String initialRev = cPolicy.getFirstInSequence(context);
                emxInstallUtil_mxJPO.println(context, ">initialRev " + initialRev  + "\n");

                while(partFamilyItr.hasNext())
                {
                    Map map = (Map)partFamilyItr.next();

                    objectId     = (String) map.get("id");
                    objectType   = (String) map.get("type");
                    objectName   = (String) map.get("name");
                    objectRev    = (String) map.get("revision");
                    objectVault  = (String) map.get("vault");
                    objectState  = (String) map.get("current");
                    objectPolicy = (String) map.get("policy");

                    busObject = new BusinessObject(objectId);
                    busObject.open(context);

                    // change the type, policy for 'Part Issuer' objects
                    // this forces to change the revision of the object to comply with Classification policy
                    if(isTypePartIssuerExists && objectType.equals(TYPE_PART_ISSUER))
                    {
                        try
                        {
                            if(objectRev != initialRev)
                            {
                                // core requires name to be changed if revision is changed
                                // so fool it by first changing, and then change it back to its original name
                                String fakeName = objectName + "Conversion~";
                                busObject.change(context, TYPE_PART_FAMILY, fakeName, initialRev, objectVault, POLICY_CLASSIFICATION);
                                busObject.update(context);

                                busObject.change(context, TYPE_PART_FAMILY, objectName, initialRev, objectVault, POLICY_CLASSIFICATION);
                                busObject.update(context);
							}
							else
							{
                                busObject.change(context, TYPE_PART_FAMILY, objectName, objectRev, objectVault, POLICY_CLASSIFICATION);
                                busObject.update(context);
							}

							// In case, if customer renamed state "Exists" to "Active"
							// then do not promote, bacause core will adjust the state automatically
							if(POLICY_PART_ISSUER_STATE_EXISTS.equals(objectState) && !POLICY_PART_ISSUER_STATE_EXISTS.equals(STATE_ACTIVE))
							{
								busObject.promote(context);
							}

                        }
                        catch(MatrixException e)
                        {
                            duplicatePartIssuerExists = true;
                            emxInstallUtil_mxJPO.println(context, ">WARNING: Duplicate name found for Part Issuer object: id" + objectId + " :name  :" + objectName + "\n");
                        }

                    }

                    // change the policy for 'Part Family' objects
                    // It is assumed here that "Part Family" type objects used "Part Family" policy
                    if(POLICY_PART_FAMILY.equals(objectPolicy))
                    {
                        busObject.setPolicy(context, cPolicy);
                        busObject.update(context);

                        // In case, if customer renamed state "Exists" to "Active"
                        // then do not promote, bacause core will adjust the state automatically
                        if(STATE_EXISTS.equals(objectState) && !STATE_EXISTS.equals(STATE_ACTIVE))
                        {
                            busObject.promote(context);
                        }
                    }

                    busObject.update(context);
                    busObject.close(context);

                    // if the "Part Family", "Part Issuer" does not have associated Parts
                    // map will not contain the key, this is also applicable if the conversion routine
                    // is run multiple times
                    if(map.containsKey(SELECT_PART_FAMILY_MEMBER_RELATIONSHIP_NAME))
                    {
                        partFamiltMemberRelList = (StringList) map.get(SELECT_PART_FAMILY_MEMBER_RELATIONSHIP_NAME);
                        if( partFamiltMemberRelList != null && partFamiltMemberRelList.size() > 0)
                        {
                            partFamiltMemberRelItr  = partFamiltMemberRelList.iterator();

                            while(partFamiltMemberRelItr.hasNext())
                            {
                                command = "modify connection " + (String)partFamiltMemberRelItr.next() + " type \"" + RELATIONSHIP_CLASSIFIED_ITEM + "\"";
                                emxInstallUtil_mxJPO.executeMQLCommand(context, command);
                            }
                            command = "modify businessobject " + objectId + " \"" + ATTRIBUTE_COUNT + "\" " + partFamiltMemberRelList.size();
                            emxInstallUtil_mxJPO.executeMQLCommand(context, command);
                        }
                    }
                }
            }
            else
            {
                emxInstallUtil_mxJPO.println(context, "No Part Family/Part Issuer objects found in the database\n");
            }
            migrationStatus = true;

            // delete the Part Issuer type, policy from the database
            if( isTypePartIssuerExists && !duplicatePartIssuerExists)
            {
                command = "delete type \"" + TYPE_PART_ISSUER + "\"";
                emxInstallUtil_mxJPO.executeMQLCommand(context, command);

                command = "delete policy \"" + POLICY_PART_ISSUER + "\"";
                emxInstallUtil_mxJPO.executeMQLCommand(context, command);
            }

            return migrationStatus;
        }
        catch (Exception ex)
        {
            emxInstallUtil_mxJPO.println(context, ">ERROR:in migratePartFamilyData method Exception :" + ex.getMessage() + "\n");
            throw ex;
        }
    }

    private ArrayList toMapList(BusinessObjectWithSelectList list) throws Exception
    {
        try
        {
            ArrayList mapList = new ArrayList(list.size());

            BusinessObjectWithSelectItr itr = new BusinessObjectWithSelectItr(list);
            while (itr.next())
            {
                HashMap hashMap = new HashMap(itr.obj().getHashtable().size());
                Iterator keys = itr.obj().getHashtable().keySet().iterator();
                while (keys.hasNext())
                {
                    String name = (String) keys.next();

                    if (SELECT_PART_FAMILY_MEMBER_RELATIONSHIP_NAME.equals(name))
                    {
                        hashMap.put(name, itr.obj().getSelectDataList(name));
                    }
                    else
                    {
                        hashMap.put(name, itr.obj().getSelectData(name));
                    }
                }

                mapList.add(hashMap);
            }

            return (mapList);
        }
        catch (Exception ex)
        {
            throw ex;
        }

    }

}
