/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.io.*;
import java.util.*;

import matrix.db.Context;
import matrix.db.MatrixWriter;

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

public class emxRDOMigrationBlankRDOInfoBase_mxJPO
{
	/**
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    * @grade 0
    */
    public emxRDOMigrationBlankRDOInfoBase_mxJPO (Context context, String[] args)
            throws Exception
    {
    	super();
    }
	
    /**
     * Evalutes a temp query to get all the objects in the system with Blank RDO
     * @param context the eMatrix <code>Context</code> object
     * @param args hold arguments passed
     * @return void
     * @exception Exception if the operation fails.
     */
    //public void getIds(Context context, int chunkSize) throws Exception
    public int mxMain(Context context, String args[]) throws Exception
    {
    	long startTime = System.currentTimeMillis();
    	BufferedWriter  writer = null;
    	try{
	    	writer = new BufferedWriter(new MatrixWriter(context));
	    	if (args.length < 2 ) {
	             throw new IllegalArgumentException();
	         }
    	}  catch (IllegalArgumentException iExp) {
            writer.write("=================================================================\n");
            writer.write("Wrong number of arguments \n");
            writer.write("execute program  emxRDOMigrationBlankRDOInfo <Type to search for> <the directory where files should be written> \n");
            writer.write("Find Objects with Blank RDO Query FAILED \n");
            writer.write("=================================================================\n");
            writer.close();
            return 0;
        }
    	String type = args[0];
    	String documentDirectory = args[1];

    	File folderExisting = new File(documentDirectory);

		try {
			if(!folderExisting.exists()) {
				throw new FileNotFoundException();
			}

		} catch (FileNotFoundException fEx) {
            // check if user has access to the directory
            // check if directory exists
            writer.write("=================================================================\n");
            writer.write("Directory does not exist or does not have access to the directory\n");
            writer.write("Step 1 of Migration     : FAILED \n");
            writer.write("=================================================================\n");
            writer.flush();
            return 0;
        }


        try
        {
        	 String fileSeparator = java.io.File.separator;
             if(documentDirectory != null && !documentDirectory.endsWith(fileSeparator))
             {
                 documentDirectory = documentDirectory + fileSeparator;
             }

        	String newline = System.getProperty("line.separator");
            FileWriter blankRDOLog = new FileWriter(documentDirectory + "blankRDOLog.txt", false);


			blankRDOLog.write("The output can be considered as an information so that data can be corrected with"+newline);
			blankRDOLog.write("right Design Responsibility value before the actual migration."+newline);
			blankRDOLog.write("-------------------------------------------------"+newline);
            blankRDOLog.write("Contents all the Objects with the Blank RDO Value"+newline);
            blankRDOLog.write("-------------------------------------------------"+newline);
            blankRDOLog.write("Type \t Name \t Revision \t PhysicalId \t Owner \t Owner's Company"+newline);

        	String strWhere = "(altowner1 == \"\" && altowner2 == \"\" && to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"]==False)";
        	String strBlankRDO = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 where $4 select $5 $6 dump $7", type, "*", "*", strWhere, "physicalid", "owner", "|");

        	StringTokenizer st = new StringTokenizer(strBlankRDO, "\n");

        	while(st.hasMoreTokens()) {
                String strToken = st.nextToken().trim();
                StringTokenizer st1 = new StringTokenizer(strToken, "|");
                while(st1.hasMoreTokens()) {
                	String strType = st1.nextToken().trim();
                	String strName = st1.nextToken().trim();
                	String strRev = st1.nextToken().trim();
                	String strPhyId = st1.nextToken().trim();
                	String strOwner = st1.nextToken().trim();
                	String strPersonId = PersonUtil.getPersonObjectID(context, strOwner);
                	DomainObject dPersonObj = new DomainObject(strPersonId);
                	String strUserCmp = dPersonObj.getInfo(context, "to["+DomainConstants.RELATIONSHIP_EMPLOYEE+"].from.name");
                	blankRDOLog.write(newline);
                	blankRDOLog.write(strType + " \t " + strName + " \t " + strRev + " \t " + strPhyId + " \t " + strOwner + " \t " + strUserCmp);
                }
        	}

            blankRDOLog.flush();

            writer.write("\n=======================================================\n\n");
        	writer.write("The output can be considered as an information so that data can be corrected with"+newline);
			writer.write("right Design Responsibility value before the actual migration."+newline);
			writer.write(" "+newline);
            writer.write("        Querying for Blank RDO Objects COMPLETE\n");
            writer.write("        Time:"+ (System.currentTimeMillis() - startTime) + "ms \n");
            writer.write("        SUCCESS \n\n");
            writer.write("=======================================================\n\n");
            writer.flush();
            return 0;
        }  catch(Exception ex) {
			writer.write(ex.getMessage());
        	writer.write("=================================================================\n");
            writer.write("Find Objects with Blank RDO Query failed \n");
            writer.write("=================================================================\n");
            writer.flush();
            return 0;
        }

    }
}
