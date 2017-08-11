/*
 ** ${CLASSNAME}.java
 **
 ** Copyright (c) 1999-2010 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 ** DAR/FRM: 21/04/2010: creation
 */

import java.util.ListIterator;
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.StringList;
import com.matrixone.jsystem.util.StringUtils;
import com.dassault_systemes.vplmsecurity.PLMSecurityManager;
import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Query;
import matrix.db.Visuals;



/**
 * @author DAR
 * Publish Subscribe objects migration in order to support unicorn new relationship model 02/2010
 */

public class emxPubSubNewRelMigration_mxJPO {

	// Constants
	public StringList  		_modBusCommandList = new StringList();
	public int  				_globalRc = 0 ;
    private static final String MARKER_TYPE="VPLMDataMigration";
    private static final String MARKER_NAME="emxPubSubNewRelMigration";
    private static final String MARKER_ATTRIBUTE_STATUS="VPLMsys/MigrationStatus";
    private static final String MARKER_STATUS_FINISHED="FINISHED";
    private static final String MARKER_POLICY="VPLMDataMigration_Policy";
    private static final String VAULT_VPLM="vplm";
	private static final String APPLICATION_VPLM="VPLM";
	
	/**
     * Create a new emxIssueBase object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @return a emxIssueBase object.
     * @throws Exception if the operation fails.
     * @since V6R2010x
     */
	public emxPubSubNewRelMigration_mxJPO() throws Exception {
    }
    public emxPubSubNewRelMigration_mxJPO(Context context, String[] args) throws Exception {
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since V6R2010x
     */
    public int mxMain(Context context, String[] args) throws Exception {
        if (true) {
			if(0==getGlobalRc()) setGlobalRc(1);
            throw new Exception(
                    "must specify method on emxPubSubNewRelMigration invocation");
        }
        return getGlobalRc ();
    }
	
	private int getGlobalRc (){
		return _globalRc;
	}
	private void setGlobalRc (int rc){
		_globalRc = rc;
	}

	/**
	 * @param args
	 * @return
	 * @throws Exception
	 */

	private void buildMQLCommandList (Context context) throws Exception {
		// liste d'id matrix des objets "Publish Subscribe" de nom interface_VPLMitf@* *
		// et construit une liste contenant les commandes mql a executer
		String mqlcmdStr = "temp query bus \"Publish Subscribe\" interface_VPLMitf* * select  id dump '|' ;" ;
		// Publish Subscribe|interface_VPLMitf@PLMProductInstanceDS||49960.43964.3696.54080
		MQLCommand mql = new MQLCommand();

		try{
			mql.open(context);
			//mql.executeCommand(context, mqlcmdStr) ;
			if(false == mql.executeCommand(context, mqlcmdStr)){
				if(0==getGlobalRc()) setGlobalRc(2);
				throw new Exception(mql.getError().trim());
			}
			mql.close(context);
			if (mql.getError().length() != 0){
				if(0==getGlobalRc()) setGlobalRc(3);
				throw new Exception(mql.getError().trim());
			}
		}
		catch(Exception Mexc){
			StringBuffer errorMsg= new StringBuffer("Error while running mql command: ");
			errorMsg.append( mqlcmdStr );
			errorMsg.append(" into buildMQLCommandList !!");
			if(0==getGlobalRc()) setGlobalRc(4);
			throw new Exception(errorMsg.toString());
		}

		String result = mql.getResult().trim();
		if(result.length() > 0)
		{
			String longStringResult = StringUtils.replaceAll(result, "\n", "|");
			String [] tmp = StringUtils.split(longStringResult,"\\|");
			for(int i=1 ; i<=tmp.length-1;i=i+4){
				String tmpStr2 = StringUtils.replaceFirst( tmp[i] ,"interface_VPLMitf@", "relationship_VPLMrel@");
				String tmpStr = "mod bus " + tmp[i+2] + " name " + tmpStr2 + " ;";
				//System.out.println("buildMQLCommandList tmpStr =" + tmpStr);
				_modBusCommandList.addElement(tmpStr);
			}
		}
		else
		{
			System.out.println("##############################################");
			System.out.println("### NO PUBLISH SUBSCRIBE OBJECT TO MIGRATE ###");
			System.out.println("##############################################");
		}

	}

	private void applyMQLCommandList (Context context, boolean simulate) throws Exception {
//		start transaction ;
//		mod bus 49960.43964.3696.54080 name relationship_VPLMrel@PLMProductInstanceDS ;
//		commit transaction ;
		if (null == _modBusCommandList || _modBusCommandList.size()<=0)return;
		String tmpCmd = null;
		MQLCommand mql = new MQLCommand();
		try {
			context.resume();
			context.start(true);
			ListIterator itr= _modBusCommandList.listIterator();
			while(itr.hasNext()){
				tmpCmd = (String)itr.next() ;
				//System.out.println("applyMQLCommandList tmpCmd=" + tmpCmd);
				mql.open(context);
				//mql.executeCommand(context, tmpCmd);
				
				if (false == mql.executeCommand(context, tmpCmd)) {
					context.abort();
					if(0==getGlobalRc()) setGlobalRc(5);
					throw new Exception(mql.getError().trim());
				}
				mql.close(context);
				//System.out.println("Applying command: " + tmpCmd);
			}
			if (true == simulate ) 
				context.abort();
			else
				context.commitWithCheck();


		}
		catch(Exception Mexc){
		StringBuffer errorMsg= new StringBuffer("Error while applying mql command: ");
		errorMsg.append( tmpCmd );
		errorMsg.append(" into applyMQLCommandList !!");
		if(0==getGlobalRc()) setGlobalRc(6);
		throw new Exception(errorMsg.toString());
		}
		finally{
			try {if (null != context)context.resume();} 
			catch (Exception Mexc) {
			Mexc.printStackTrace(System.err);	
			if(0==getGlobalRc()) setGlobalRc(7);
			throw new Exception (Mexc.getMessage());}
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	private int run (Context context , boolean simulate) throws Exception {
	PLMSecurityManager scm = new PLMSecurityManager(context);
		try {
		    // Set application
			String application=context.getApplication();
			if (!application.equals(APPLICATION_VPLM))
			{
				context.setApplication(APPLICATION_VPLM);
			}
			scm.pushUserAgentContext();
			buildMQLCommandList(context);
			applyMQLCommandList(context,simulate);
		} 
		catch (Exception DCexc) { if(0==getGlobalRc()) setGlobalRc(8); throw new Exception (DCexc.getMessage());}
		finally {
			scm.popUserAgentContext();
		}
		return getGlobalRc();
	}
	/**
	 * migrate data with commit
	 * @param args
	 * @throws Exception
	 */
	public int dataMigration (Context context, String[] args) throws Exception {
		try {
				run(context,false);
				createMarker(context);
		} 
		catch (Exception DCexc) {if(0==getGlobalRc()) setGlobalRc(9); throw new Exception (DCexc.getMessage());}
		finally {System.out.println("emxPubSubNewRelMigration rc="+getGlobalRc());}
		return getGlobalRc();
	}
	
	// simulate migration
	public int dataSimulation (Context context, String[] args) throws Exception {
		try {
				run(context,true);
		} catch (Exception DCexc) {if(0==getGlobalRc()) setGlobalRc(10); throw new Exception (DCexc.getMessage());}
		finally {System.out.println("emxPubSubNewRelMigration rc="+getGlobalRc());}
		return getGlobalRc();
	}

    private BusinessObject queryMarker(Context context) throws Exception
    {
        BusinessObject marker=null;

        // Query marker
        StringList lstSelectable=new StringList();
        lstSelectable.add("attribute["+MARKER_ATTRIBUTE_STATUS+"]");
        Query query=new Query();
        query.open(context);
        query.setVaultPattern(VAULT_VPLM);
        query.setBusinessObjectType(MARKER_TYPE);
        query.setWhereExpression("(name == \""+MARKER_NAME+"\")");
        query.setExpandType(true);
        BusinessObjectWithSelectList lstMarker=query.selectTmp(context,lstSelectable);
        query.close(context);

        if (0<lstMarker.size())
        {
            marker=(BusinessObjectWithSelect)lstMarker.getElement(0);
        }

        return(marker);
    }
	
	private void createMarker(Context context) throws Exception
    {

        {
            System.out.println("Creating marker...");
        }

        try
        {
            // Query marker
            BusinessObject marker=queryMarker(context);

            if (marker==null)
            {
                start(context);
                marker=new BusinessObject(MARKER_TYPE,MARKER_NAME,"---",VAULT_VPLM);
                AttributeList lstMarkerAttribute=new AttributeList(1);
                lstMarkerAttribute.addElement(new Attribute(new AttributeType(MARKER_ATTRIBUTE_STATUS),MARKER_STATUS_FINISHED));
                marker.create(context,MARKER_POLICY,new Visuals(),lstMarkerAttribute);
				context.commitWithCheck();
            }
            else
            {
                System.out.println("Warning: marker " +MARKER_NAME+ " already exists");
            }
        }
		finally
        {
            System.out.println("... done");
        }
    }
	
	private void start(Context context) throws Exception
    {
        if (!context.isTransactionActive())
        {
            context.start(true);
        }
    }
}


