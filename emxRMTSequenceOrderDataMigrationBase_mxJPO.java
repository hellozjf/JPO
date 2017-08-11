/*
** emxRMTSequenceOrderDataMigrationBase
**
** Copyright (c) 1992-2016 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
*
*/

import java.io.FileOutputStream;
import java.io.PrintStream;

import matrix.db.Context;
import matrix.db.RelationshipQuery;
import matrix.db.RelationshipQueryIterator;
import matrix.db.RelationshipWithSelect;
import matrix.util.StringList;

import com.dassault_systemes.requirements.ReqSchemaUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
/**
 * The <code>emxRMTSequenceOrderDataMigrationBase</code> class contains migration script
 * @author
 * @version RequirementsManagement V6R2010x - Copyright (c) 2011, MatrixOne, Inc.
 *
 */
public class emxRMTSequenceOrderDataMigrationBase_mxJPO
{
    private String MIGRATION_LOG_FILE;
    private FileOutputStream foLogFileInfo;
    private PrintStream psLogWriter;


    /**
    * Create a new emxRMTSequenceOrderDataMigrationBase object from a given id.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments.
    * @return a emxRMTSequenceOrderDataMigrationBase Object
    * @throws Exception if the operation fails
    * @since RequirementsManagement V6R2012x
    */
    public emxRMTSequenceOrderDataMigrationBase_mxJPO (Context context, String[] args) throws Exception
    {
        //super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2012x
     */
    public int mxMain (Context context, String[] args)
    throws Exception
    {
	if(UINavigatorUtil.isCloud(context) && args.length != 0 && args[0].equals("on-premise"))
	{
	    System.out.println("this is a cloud env, skip on-premise migration.");
	    return 0;
	}
        if (!context.isConnected())
        {
            String strContentLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Error.UnsupportedClient"); 
            throw  new Exception(strContentLabel);
        }
        migrate(context,args);
        return  0;
     }

    /**
     * this method migrates the data.
     * @param context the eMatrix <code>Context</code> object
     * @param args  - is a string array
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2012x
     */
    public void migrate(Context context,String[] args)
    throws Exception
    {
    	 //open file for error logging
         try
        {
	    MIGRATION_LOG_FILE = java.io.File.createTempFile("RMTMigration", ".log").getAbsolutePath();
            foLogFileInfo = new FileOutputStream(MIGRATION_LOG_FILE,false);
            psLogWriter = new PrintStream(foLogFileInfo);
        }
        catch(Exception e)
        {
            Exception ex=new Exception("Unable to create log file");
            throw ex;
        }
       

        migrateSequenceOrderToTreeOrder(context, args);
         
    }


    /**
     * this method normalizes the Sequence Order of Requirement Structure
     * @param context the eMatrix <code>Context</code> object
     * @param args  - is a string array
     * @throws Exception if the operation fails
     * @since RequirementsManagement V6R2012x
     */
	protected void migrateSequenceOrderToTreeOrder(Context context,String[] args) throws Exception
    {
        try
        {
        	String strType=ReqSchemaUtil.getSpecStructureRelationship(context) + "," + 
        				   ReqSchemaUtil.getSubRequirementRelationship(context)+ "," +
        				   ReqSchemaUtil.getDerivedRequirementRelationship(context)+ "," +
        				   ReqSchemaUtil.getRequirementGroupContentRelationship(context)+ "," +
        				   ReqSchemaUtil.getSubRequirementGroupRelationship(context)+ "," +
        				   ReqSchemaUtil.getRequirementValidationRelationship(context)+ "," +
        				   ReqSchemaUtil.getSubTestCaseRelationship(context);
        	
            StringList slSelectStmts = new StringList();
            slSelectStmts.add(DomainConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER);
            slSelectStmts.add(DomainRelationship.SELECT_ID);
         
            context.start(true);
        	RelationshipQueryIterator queryIterator = null;
            RelationshipQuery query = new RelationshipQuery(); 
            
         
            query.open(context);
            query.setRelationshipType(strType);
            query.setWhereExpression("");
            query.setVaultPattern("*");
            queryIterator = query.getIterator(context,slSelectStmts,(short)10000);
            
            System.out.println("Please Refer "+ MIGRATION_LOG_FILE);            
            migrationInfoWriter("    REL ID              SEQ ORDER");
            while (queryIterator.hasNext()) 
            {
            	ContextUtil.startTransaction(context, true);
                RelationshipWithSelect relobj = queryIterator.next();                               
                String strRelId = relobj.getSelectData (DomainRelationship.SELECT_ID);
                String SeqOrder = relobj.getSelectData(DomainConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER);  
                migrationInfoWriter((relobj).getName()+ "    " + SeqOrder);
                DomainRelationship.setAttributeValue (context, strRelId, "TreeOrder", SeqOrder);
                //free transaction
      		  	ContextUtil.commitTransaction(context);
            }
            queryIterator.close();
            context.commit();
        }
        catch(Exception ex)
        {
        	ContextUtil.abortTransaction(context);
			System.out.println(ex.toString());
            throw ex;
        }
    }

    /**
    * This method writes to the log file using the Printwriter object
    * @param strLogEnrty - Message to write
    * @throws Exception if the operation fails
    * @since RequirementsManagement V6R2012x
    * @grade 0
    */
    protected void migrationInfoWriter(String strLogEnrty)
    throws Exception
    {
        if (strLogEnrty!=null && strLogEnrty.length()>0)
        {
            psLogWriter.println(strLogEnrty);

        }
    }
    

}

