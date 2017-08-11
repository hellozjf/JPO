import java.io.FileNotFoundException;
import java.io.FileWriter;

import matrix.db.Context;
import matrix.db.MatrixWriter;
import matrix.util.StringList;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;


public class emxLibraryCentralMigrateToInterfaceParamFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO
{
	String LibraryType = "";
	String ClassType = "";
	String defaultChunkSize = "100";
	 String documentDirectory = "";

	public emxLibraryCentralMigrateToInterfaceParamFindObjectsBase_mxJPO(Context context, String[] args) throws Exception 
	{
		super(context, args);
	}



	/**
     * This method is executed if a specific method is not specified.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds two arguments
     *        args[0] - chunk size (i.e No of Object Ids to be written to each flat file)
     *        args[1] - Directory to where files containing ObjectIds are to be written
     *
     * @returns 0 always
     * @throws Exception if the operation fails
     */
    public int mxMain(Context context, String[] args) throws Exception
    {
        try
        {
			
            writer	=	new BufferedWriter(new MatrixWriter(context));

            if (args.length < 1 )
            {
                throw new IllegalArgumentException("Wrong number of arguments");
            }

            String[] newArgs    = null;

            if(args.length == 1)
            {

                newArgs    = new String[2];
                newArgs[0] = defaultChunkSize;
                newArgs[1] = args[1];
				documentDirectory =  args[1];
				
            }
            else
            {
				
                //If there are more than 1 arguments , then first Argument is chunk size
                newArgs    = new String[args.length+1];
                newArgs[0] = args[0];

                //everything from and after index 1 in args is put to newArgs from index 2
                for(int i = args.length; i > 1; i--)
                {
                    newArgs[i] = args[i-1];
					documentDirectory = newArgs[i];
                }
				
				
            }
           
            
            super.mxMain(context, newArgs);

        }
        catch (IllegalArgumentException iExp)
        {
            writer.write("=================================================================\n");
            writer.write("Wrong number of arguments \n");
            writer.write("Step 1 of Migration :   " + iExp.toString() + "   : FAILED \n");
            writer.write("=================================================================\n");
            writer.flush();
            writer.close();
        }
        return 0;
    }



    /**
     * Evaluates temp query to get all
     *      - Objects of type 'Document Library'
     *      - Objects of type 'Bookshelf' which are not connected to any 'document Library'
     *      - Objects of type 'Book' which are not connected to any 'Bookshelf'

     * @param context the eMatrix <code>Context</code> object
     * @param chunksize has the no. of objects to be stored in file.
     * @return void
     * @exception Exception if the operation fails.
     */
    public void getIds(Context context, int chunkSize) throws Exception
    {
        //reset/set static variables back to original values every time this JPO is run
        emxCommonMigration_mxJPO._counter       = 0;
		emxCommonMigration_mxJPO._chunk       = 0;
        emxCommonMigration_mxJPO._sequence      = 1;
        emxCommonMigration_mxJPO._oidsFile      = null;
        emxCommonMigration_mxJPO._fileWriter    = null;
        emxCommonMigration_mxJPO._objectidList  = null;
        
        

        //set statics
        //create BW and file first time
		if (emxCommonMigration_mxJPO._fileWriter == null)
        {
            try
            {
				
                emxCommonMigration_mxJPO.documentDirectory  = documentDirectory;
                emxCommonMigration_mxJPO._oidsFile          = new java.io.File(documentDirectory + "/objectids_1.txt");							
                emxCommonMigration_mxJPO._fileWriter        = new BufferedWriter(new FileWriter(emxCommonMigration_mxJPO._oidsFile));								
                emxCommonMigration_mxJPO._chunk             = chunkSize;
                emxCommonMigration_mxJPO._objectidList      = new StringList(chunkSize);

	           String queryCmd = "";
			   queryCmd        = "print interface $1 select immediatederivative dump $2";
			    String data     = MqlUtil.mqlCommand(context, queryCmd, true, DomainConstants.INTERFACE_CLASSIFICATION_ATTRIBUTE_GROUPS, ",").trim();
			    StringList agNames = FrameworkUtil.split(data, ",");
				
				//${CLASS:emxCommonMigration}._counter++;
				
				//if (${CLASS:emxCommonMigration}._counter == ${CLASS:emxCommonMigration}._chunk)
	            //{
				
	                //${CLASS:emxCommonMigration}._counter=0;
	                //${CLASS:emxCommonMigration}._sequence++;
	
	                //write oid from _objectidList
	                for (int s=0;s<agNames.size();s++)
	                {
	                    emxCommonMigration_mxJPO._fileWriter.write((String)agNames.elementAt(s));
	                    emxCommonMigration_mxJPO._fileWriter.newLine();
					}

	                //create new file
	                //${CLASS:emxCommonMigration}._oidsFile = new java.io.File(documentDirectory + "/objectids_" +${CLASS:emxCommonMigration}._sequence + ".txt");
	                //${CLASS:emxCommonMigration}._fileWriter = new BufferedWriter(new FileWriter(${CLASS:emxCommonMigration}._oidsFile));
	            //}
	        }
	        catch(IOException me)
	        {
				me.printStackTrace();
	            throw me;
	        }
            finally
    		{ 
    		   try{
    		      if(emxCommonMigration_mxJPO._fileWriter!=null)
    		    	  emxCommonMigration_mxJPO._fileWriter.close();
    		   }catch(Exception ex){
    		       System.out.println("Error in closing the BufferedWriter"+ex);
    		    }
    		}
         }
    }

}
