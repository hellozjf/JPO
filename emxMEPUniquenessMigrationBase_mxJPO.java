/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import matrix.db.*;
import matrix.util.*;
import java.util.*;
import java.io.*;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

//import com.matrixone.apps.domain.util.FrameworkUtil;

/**
 * The <code>emxMEPUniquenessMigrationBase</code> class contains implementation code external part interface to MEPs.
 *
 * @version EC 10.7.- Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxMEPUniquenessMigrationBase_mxJPO //extends ${CLASS:DomainObject}
{
    static protected emxContextUtil_mxJPO contextUtil = null;
    static protected String newline = System.getProperty("line.separator");
    protected RandomAccessFile ranFile = null;

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since EC 9.5.JCI.0.
     */
    public emxMEPUniquenessMigrationBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        //super(context, args);
        contextUtil = new emxContextUtil_mxJPO(context, null);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return int.
     * @throws Exception if the operation fails.
     * @since EC 9.5.JCI.0.
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (args.length == 0)
        {
            throw new Exception("must specify directory for log file creation.");
        }
        String sDirectory = args[0];
        java.io.File logFile = new java.io.File(sDirectory, "MEPUniquenessMigration.log");

        contextUtil.pushContext(context, null);
        try
        {
          ranFile = new RandomAccessFile(logFile,"rw");
          associateInterface(context);
          updatePlantID(context);
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
          contextUtil.popContext(context, null);
        }

        return 0;
    }

    public void associateInterface(Context context) throws Exception
    {
        MQLCommand mqlCommand = new MQLCommand();
        int count = 0;
        String objWhere = "policy.property[PolicyClassification] == Equivalent";
        String sExternalPartData = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_interface_ExternalPartData);

        StringList objSelects = new StringList(3);
        objSelects.add(DomainObject.SELECT_ID);
        objSelects.add(DomainObject.SELECT_NAME);
        objSelects.add(DomainObject.SELECT_REVISION);
        MapList mepList = DomainObject.findObjects(context,
                                                   DomainObject.TYPE_PART,
                                                   "*",
                                                   objWhere,
                                                   objSelects);

            ContextUtil.startTransaction(context, true);
        try
        {
            mqlCommand.executeCommand(context, "trigger off");
            Iterator mepItr = mepList.iterator();
            while (mepItr.hasNext())
            {
                Map map = (Map) mepItr.next();
                String objId = (String)map.get(DomainObject.SELECT_ID);
                String name =  (String)map.get(DomainObject.SELECT_NAME);
                String rev  =  (String)map.get(DomainObject.SELECT_REVISION);
                boolean mqlResult = mqlCommand.executeCommand(context, "print bus $1 select $2 dump $3",objId,"interface.name","|");
                String result = null;
                if(mqlResult) {
                     result = mqlCommand.getResult();
                }

                if(result != null && !"null".equals(result.trim())) {
                    if(result.indexOf(sExternalPartData) == -1) {
                        //Associate interface
                        mqlCommand.executeCommand(context, "modify bus $1 add interface $2",objId,sExternalPartData);
                        String log = "Part "+name+" "+rev +" Interface '"+sExternalPartData+"' successfully added "+newline;
                        ranFile.seek(ranFile.length());
                        ranFile.writeBytes(log);
                        count++;
                    } else {
                        // Already hae interface
                        String log = "Part "+name+" "+rev +" already has interface '"+sExternalPartData+"' "+newline;
                        ranFile.seek(ranFile.length());
                        ranFile.writeBytes(log);
                    }
                    if(count==1000) {
                        ContextUtil.commitTransaction(context);
                        ContextUtil.startTransaction(context, true);
                    }
                }
            }
            ContextUtil.commitTransaction(context);
        }
        catch(Exception e)
        {
            ContextUtil.abortTransaction(context);
            throw e;
        }
        finally
        {
            mqlCommand.executeCommand(context, "trigger on");
        }
    }


    /**
     * Update location plant id attribute as location rev if location plant attribute is empty.
     * The following steps are performed:
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param File Log file to be updated.
     * @throws Exception if the operation fails.
     * @since EC 10.7.
     */
    public void updatePlantID(Context context) throws Exception
    {
        MQLCommand mql = new MQLCommand();
        String attirbPlanID = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_attribute_PlantID);
        String objWhere = "attribute["+attirbPlanID+"] == ''";

        StringList objSelects = new StringList(3);
        objSelects.add(DomainObject.SELECT_ID);
        objSelects.add(DomainObject.SELECT_NAME);
        objSelects.add(DomainObject.SELECT_REVISION);
        MapList mepList = DomainObject.findObjects(context,
                                                   DomainObject.TYPE_LOCATION,
                                                   "*",
                                                   objWhere,
                                                   objSelects);

        try
        {
            ContextUtil.startTransaction(context, true);
            mql.executeCommand(context, "trigger off");

            DomainObject dom = new DomainObject();
            Iterator mepItr = mepList.iterator();
            while (mepItr.hasNext())
            {
                Map map = (Map) mepItr.next();
                String objId = (String)map.get(DomainObject.SELECT_ID);
                String name =  (String)map.get(DomainObject.SELECT_NAME);
                String rev  =  (String)map.get(DomainObject.SELECT_REVISION);

                objWhere =  "attribute["+attirbPlanID+"] == '"+rev+"'";
                MapList locList = DomainObject.findObjects(context,
                                                           DomainObject.TYPE_LOCATION,
                                                           "*",
                                                           objWhere,
                                                           objSelects);

                if(locList.size() == 0) {
                    dom.setId(objId);
                    dom.setAttributeValue(context, attirbPlanID, rev);
                } else {
                    String log = "Location "+name+" "+rev +" 'Plant' ID' attribute value can not be set with unique value, set the value manually "+newline;
                    ranFile.seek(ranFile.length());
                    ranFile.writeBytes(log);
                }
            } //end of while
            ranFile.close();
            ContextUtil.commitTransaction(context);
        }
        catch(Exception e)
        {
            ContextUtil.abortTransaction(context);
            throw e;
        }
        finally
        {
            mql.executeCommand(context, "trigger on");
        }
    }
}
