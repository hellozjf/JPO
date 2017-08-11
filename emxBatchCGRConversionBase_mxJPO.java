
/*
 * Cgr Batch program
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program. *

 *
 */

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import java.util.*;
import java.io.*;


import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.*;
import com.matrixone.apps.common.util.*;

import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.Namespace;
import com.matrixone.jdom.output.XMLOutputter;

/**
 *
 *
 * @version   - Copyright (c) 2007, MatrixOne, Inc.
 */
public class emxBatchCGRConversionBase_mxJPO
{

	PrintWriter writer;
	private static String VAULT_PRODUCTION;
    private static String TYPE_3DXML;
    private static String TYPE_3DXMLCGR;
    private static String TYPE_CGR;
    private static String TYPE_THUMBNAIL;
    private static String TYPE_VIEWABLE;

    private static String REL_VIEWABLE;

    private static String FORMAT_3DXML;
    private static String FORMAT_3DXMLCGR;
    private static String FORMAT_CGR;
    private static String FORMAT_THUMBNAIL;

	/**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     *
     */
    public emxBatchCGRConversionBase_mxJPO(Context context, String[] args)
        throws Exception
    {
    	VAULT_PRODUCTION = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_vault_eServiceProduction);
        TYPE_3DXML = PropertyUtil.getSchemaProperty(context, "type_3dxmlViewable");
        TYPE_3DXMLCGR = PropertyUtil.getSchemaProperty(context, "type_3dxmlcgrViewable");
        TYPE_CGR = PropertyUtil.getSchemaProperty(context, "type_CgrViewable");
        TYPE_THUMBNAIL = PropertyUtil.getSchemaProperty(context, "type_ThumbnailViewable");
        TYPE_VIEWABLE = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_Viewable);

        REL_VIEWABLE = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_Viewable);

        FORMAT_3DXML = PropertyUtil.getSchemaProperty(context, "format_3DXML");
        FORMAT_3DXMLCGR = PropertyUtil.getSchemaProperty(context, "format_3DXMLCGR");
        FORMAT_CGR = PropertyUtil.getSchemaProperty(context, "format_CGR");
        FORMAT_THUMBNAIL = PropertyUtil.getSchemaProperty(context, "format_THUMBNAIL");
    }

    /**
     * Main method
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds command line arguments for vault if passed
     * @returns program execution status
     * @throws Exception if the operation fails
     * @since AppsCommon 10.7.SP1
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
		writer=new PrintWriter(new MatrixWriter(context));
		if(args.length > 0)
		{
			VAULT_PRODUCTION=args[0];
		}
        ConvertCGRto3Dxml(context);
        return 0;
    }
    /**
     * This method is primary method which gets cgr objects and prcesses them
	 *  and converts cgr images to 3dxml images
     *
     * @param context the eMatrix <code>Context</code> object
     * @returns nothing
     * @throws Exception if the operation fails
     * @since AppsCommon 10.7.SP1
     */
	public void ConvertCGRto3Dxml(Context context) throws Exception
	{
		  ArrayList viewableObjects;
		  StringList tempList;
		  StringList cgrFiles;
		  StringList xmlFiles;
		  StringList xmlcgrFiles;
		  String workspacepath;
		  String mqlCmd="";
		  String CGR_FORMAT=FORMAT_CGR;
		  String XML_FORMAT=FORMAT_3DXML;
		  String XMLCGR_FORMAT=FORMAT_3DXMLCGR;
		  String tempString="";
		  String cgrId;
		  String xmlId;
		  String xmlcgrId;
		  String parentId;
		  try
		  {
		   workspacepath=context.createWorkspace();
		    // Get Cgr objects connected with Viewable relationship
		   viewableObjects=getViewableObjects(context);
		   //Process the Cgr Objects
		   Iterator itr=viewableObjects.iterator();
		   while(itr.hasNext())
		   {
			   tempList=(StringList)itr.next();
			   //if cgr object only connected to CAD object with Viewable relationship
				if(tempList.size() == 5)
			   {
					parentId=(String)tempList.get(3);
					cgrId=(String)tempList.get(4);
					String xmlcgrname=(String)tempList.get(1);
					cgrFiles=FrameworkUtil.split(MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3", cgrId, "format["+CGR_FORMAT+"].file.name", "|"),"|");
					Iterator cgritr=cgrFiles.iterator();
					if(cgrFiles.size()>0)
				   {
						MqlUtil.mqlCommand(context,"add businessobject $1 $2 $3 policy $4 vault $5", TYPE_3DXMLCGR, xmlcgrname, "A", "Viewable Policy", VAULT_PRODUCTION);
						xmlcgrId=(String)(FrameworkUtil.split(MqlUtil.mqlCommand(context,"temp query bus $1 $2 $3 select $4 dump $5", TYPE_3DXMLCGR, xmlcgrname, "A", "id", "|"),"|")).get(3);
						MqlUtil.mqlCommand(context,"connect businessobject $1 relationship $2 to $3", parentId, "Viewable", xmlcgrId);
						while(cgritr.hasNext())
					   {
							String cgrfile=(String)cgritr.next();
							tempString=getFileBaseName(cgrfile)+".3dxml";
							mqlCmd="checkout businessobject $1 format $2 file $3 $4";
							MqlUtil.mqlCommand(context,mqlCmd, cgrId, CGR_FORMAT, cgrfile, workspacepath);
							prepare3dxmlFromCGR(context,workspacepath,cgrfile);
							mqlCmd="checkin businessobject $1 format $2 append $3";
							MqlUtil.mqlCommand(context,mqlCmd, xmlcgrId, XMLCGR_FORMAT, workspacepath+java.io.File.separatorChar+tempString);
					   }
				   }

			   }
			   //If cgr and 3dxml or 3dxmlcgr objects are connected to CAD object with Viewable relationship
			   else if(tempList.size() == 7)
			   {
					parentId=(String)tempList.get(3);
					cgrId=(String)tempList.get(4);
					String xmlcgrname=(String)tempList.get(1);
					cgrFiles=FrameworkUtil.split(MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3", cgrId, "format["+CGR_FORMAT+"].file.name", "|"),"|");
					String flag=(String)tempList.get(5);
					if(flag.equals(TYPE_3DXML))
				   {
						xmlId=(String)tempList.get(6);
						xmlFiles=FrameworkUtil.split(MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3", xmlId, "format["+XML_FORMAT+"].file.name", "|"),"|");
						if(cgrFiles != null && cgrFiles.size() > 0)
					   {
						MqlUtil.mqlCommand(context,"add businessobject $1 $2 $3 policy $4 vault $5", TYPE_3DXMLCGR, xmlcgrname, "A", "Viewable Policy", VAULT_PRODUCTION);
						xmlcgrId=(String)(FrameworkUtil.split(MqlUtil.mqlCommand(context,"temp query bus $1 $2 $3 select $4 dump $5", TYPE_3DXMLCGR, xmlcgrname, "A", "id", "|"),"|")).get(3);
						MqlUtil.mqlCommand(context,"connect businessobject $1 relationship $2 to $3", parentId, REL_VIEWABLE, xmlcgrId);
						 Iterator cgritr=cgrFiles.iterator();
							while(cgritr.hasNext())
						   {
								String cgrfile=(String)cgritr.next();
								tempString=getFileBaseName(cgrfile)+".3dxml";
								if(!xmlFiles.contains(tempString))
							   {
									mqlCmd="checkout businessobject $1 format $2 file $3 $4";
									MqlUtil.mqlCommand(context,mqlCmd, cgrId, CGR_FORMAT, cgrfile, workspacepath);
									prepare3dxmlFromCGR(context,workspacepath,cgrfile);
									mqlCmd="checkin businessobject $1 format $2 append $3";
									MqlUtil.mqlCommand(context,mqlCmd, xmlcgrId, XMLCGR_FORMAT, workspacepath+java.io.File.separatorChar+tempString);
							   }
						   }
					   }


				   }
				   else if(flag.equals(TYPE_3DXMLCGR))
				   {
						xmlcgrId=(String)tempList.get(6);
						xmlcgrFiles=FrameworkUtil.split(MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3", xmlcgrId, "format["+XMLCGR_FORMAT+"].file.name", "|"),"|");

						Iterator cgritr=cgrFiles.iterator();
						while(cgritr.hasNext())
					   {
							String cgrfile=(String)cgritr.next();
							tempString=getFileBaseName(cgrfile)+".3dxml";
							if(!xmlcgrFiles.contains(tempString))
						   {
								mqlCmd="checkout businessobject $1 format $2 file $3 $4";
								MqlUtil.mqlCommand(context,mqlCmd, cgrId, CGR_FORMAT, cgrfile, workspacepath);
								prepare3dxmlFromCGR(context,workspacepath,cgrfile);
								mqlCmd="checkin businessobject $1 format $2 append $3";
								MqlUtil.mqlCommand(context,mqlCmd, xmlcgrId, XMLCGR_FORMAT, workspacepath+java.io.File.separatorChar+tempString);
								//MqlUtil.mqlCommand(context,mqlCmd);
						   }
					   }
				   }
			   }
			   //if cgr , 3dxml , 3dxmlcgr objects are connected to CAD object with viewable relationship
			   else if(tempList.size() == 9)
			   {
					cgrId=(String)tempList.get(4);
					xmlId=(String)tempList.get(6);
					xmlcgrId=(String)tempList.get(8);
					cgrFiles=FrameworkUtil.split(MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3", cgrId, "format["+CGR_FORMAT+"].file.name", "|"),"|");
					xmlFiles=FrameworkUtil.split(MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3", xmlId, "format["+XML_FORMAT+"].file.name", "|"),"|");
					xmlcgrFiles=FrameworkUtil.split(MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3", xmlcgrId, "format["+XMLCGR_FORMAT+"].file.name", "|"),"|");
					Iterator cgritr=cgrFiles.iterator();
					while(cgritr.hasNext())
				   {
						   String cgrfile=(String)cgritr.next();
						   tempString=getFileBaseName(cgrfile)+".3dxml";
						   if(!xmlFiles.contains(tempString) && !xmlcgrFiles.contains(tempString))
						   {
							   mqlCmd="checkout businessobject $1 format $2 file $3 $4";
							   MqlUtil.mqlCommand(context,mqlCmd, cgrId, CGR_FORMAT, cgrfile, workspacepath);
								prepare3dxmlFromCGR(context,workspacepath,cgrfile);
								mqlCmd="checkin businessobject $1 format $2 append $3";
							   MqlUtil.mqlCommand(context,mqlCmd, xmlcgrId, XMLCGR_FORMAT, workspacepath+java.io.File.separatorChar+tempString);
						   }
				   }
			   }
		   }
		   //Deleting the workspace folder after process
			context.deleteWorkspace();
		  }
		  catch(Exception e)
		  {
			  writer.println("In ConvertCGRto3dxml method:"+e.toString());
		  }


	}

	  /**
     * This method is used to get all CgrViewable objects in the database
     *
     * @param context the eMatrix <code>Context</code> object
     * @returns ArryaList  contains StringLists which contains CAD object,Cgr object,3dxml object,3dxmlcgr object
     * @throws Exception if the operation fails
     * @since AppsCommon 10.7.SP1
     */
	public ArrayList getViewableObjects(Context context)
	{
		ArrayList viewableObjects=new ArrayList();
		StringList templist;

		String mqlcmd="temp query bus $1 $2 $3 $4 where $5 select $6 $7 $8 $9 $10 $11 dump $12";
		try{
			String result=MqlUtil.mqlCommand(context,mqlcmd, TYPE_CGR, "*", "*", "!expandtype", "to[" + REL_VIEWABLE + "]==True", "to[" + REL_VIEWABLE + "].from.id", "id", "to[" + REL_VIEWABLE + "].from.from[" + REL_VIEWABLE + "].to[" + TYPE_3DXML + "].type", "to[" + REL_VIEWABLE + "].from.from[" + REL_VIEWABLE + "].to[" + TYPE_3DXML + "].id", "to[" + REL_VIEWABLE + "].from.from[" + REL_VIEWABLE + "].to[" + TYPE_3DXMLCGR + "].type", "to[" + REL_VIEWABLE + "].from.from[" + REL_VIEWABLE + "].to[" + TYPE_3DXMLCGR + "].id", "|");
		StringTokenizer tokens=new StringTokenizer(result,"\n",false);
		 while(tokens.hasMoreTokens())
		 {
			String token=tokens.nextToken();
			templist=FrameworkUtil.split(token,"|");
			viewableObjects.add(templist);
		  }
		}
		catch(Exception e)
		{
			writer.println("Exception in getCat3dxmls:"+e.toString());
		}
		return viewableObjects;
	}


	  /**
     * This method is used to get the base name of the file from the
     * complete file name.
     *
     * @param context The ematrix context of the request.
     * @param strFileName Thie complete name of the file.
     * @return Base file name.
     * @since AppsCommon 10.7.SP1
     */
    public String getFileBaseName(String strFileName) {
        int index = strFileName.lastIndexOf('.');

        if (index == -1) {
            return strFileName;
        } else {
            return strFileName.substring(0, index);
        }
    }

     /**
     * This method is used to get the extension of the file from the
     * complete file name.
     *
     * @param context The ematrix context of the request.
     * @param strFileName Thie complete name of the file.
     * @return file extension.
     * @since AppsCommon 10.7.SP1
     */
    public String getFileExtension(String strFileName) {
        int index = strFileName.lastIndexOf('.');

        if (index == -1) {
            return strFileName;
        } else {
            return strFileName.substring(index + 1, strFileName.length());
        }
    }

	/**
     * This method is used to prepare the manifest.xml contents
     * which is used in 3dxml file creation
     *
     * @param String fileName of the 3dxml
     * @return Document which contains manitest file contenst in xml
     * @since AppsCommon 10.7.SP1
     */

    public  Document prepareManifest(String fileName)
    {
        Vector contentVector = new Vector();
        Element root = new Element("Manifest", "", "http://www.3ds.com/xsd/3DXML");
        contentVector.add(root);

        Element elmRoot = new Element("Root");
        elmRoot.addContent(fileName);
        root.addContent(elmRoot);
        return new Document(contentVector);
    }

/**
     * This method is used to prepare the 3dxml file contents
     *for the creation of 3dxml file.
     *
     * @param String encoded cgr contents and String filename of the 3dxml
     * @return Document which contains 3dxml file contents
     * @since AppsCommon 10.7.SP1
     */

    public static Document prepare3DXMLDocument(String fileName,String user)throws Exception
    {
       Vector contentVector = new Vector();
        Element root = new Element("Model_3dxml", "", "http://www.3ds.com/xsd/3DXML");
        contentVector.add(root);

        //add header
        Element elmHdr = new Element("Header");

        Element elmSV = new Element("SchemaVersion");
        elmSV.addContent("4.0");

        Element elmTitle = new Element("Title");
        elmTitle.addContent(fileName);

        Element elmAuthor = new Element("Author");
        elmAuthor.addContent(user);

        Element elmGenerator = new Element("Generator");
        elmGenerator.addContent("CATIA V5");

        Element elmCreated = new Element("Created");
        elmCreated.addContent((new java.util.Date()).toString());

        elmHdr.addContent(elmSV);
        elmHdr.addContent(elmTitle);
        elmHdr.addContent(elmAuthor);
        elmHdr.addContent(elmGenerator);
        elmHdr.addContent(elmCreated);
         // add DefaultSessionProperties
        Element elmDSP = new Element("DefaultSessionProperties");

        Element elmBGC = new Element("BackgroundColor");
        elmBGC.setAttribute("alpha","0.");
        elmBGC.setAttribute("red", "0.2");
        elmBGC.setAttribute("green", "0.2");
        elmBGC.setAttribute("blue", "0.4");

        Element elmRS = new Element("RenderingStyle");
        elmRS.addContent("SHADING");

        elmDSP.addContent(elmBGC);
        elmDSP.addContent(elmRS);

        // add Product Structure
        Element elmPS = new Element("ProductStructure");
        elmPS.setAttribute("root", "1");


        Element elmR3D = new Element("Reference3D");
        elmR3D.setAttribute("type", "Reference3DType", Namespace.getNamespace("xsi", "http://www.matrixone.com/xsi"));
        elmR3D.setAttribute("id", "10");
        elmR3D.setAttribute("name", fileName);

        Element elmRR = new Element("ReferenceRep");
        elmRR.setAttribute("type", "ReferenceRepType", Namespace.getNamespace("xsi", "http://www.matrixone.com/xsi"));
        elmRR.setAttribute("id", "12");
        elmRR.setAttribute("name", fileName+"_ReferenceRep");
        elmRR.setAttribute("format", "TESSELLATED");
        elmRR.setAttribute("associatedFile", "urn:3DXML:" + fileName +".3DRep");
        elmRR.setAttribute("version", "2.2");

        Element elmIR = new Element("InstanceRep");
        elmIR.setAttribute("type", "InstanceRepType", Namespace.getNamespace("xsi", "http://www.matrixone.com/xsi"));
        elmIR.setAttribute("id", "11");
        elmIR.setAttribute("name", fileName+"_InstanceRep");

        Element elmIAB = new Element("IsAggregatedBy");
        elmIAB.addContent("10");
        Element elmIIO = new Element("IsInstanceOf");
        elmIIO.addContent("12");
        elmIR.addContent(elmIAB);
        elmIR.addContent(elmIIO);

        elmPS.addContent(elmR3D);
        elmPS.addContent(elmRR);
        elmPS.addContent(elmIR);


        root.addContent(elmHdr);
        root.addContent(elmDSP);
        root.addContent(elmPS);
        return new Document(contentVector);
    }


    /**
     * This method is used to prepare the 3dxml file from the given
     * cgr file
     *
     * @param matrix.db.Context,String workspacepath and String filename of the cgr
     * @return nothing
     * @ places the created 3dxml from cgr in given workspace path
     * @since AppsCommon 10.7.SP1
     */
    public void prepare3dxmlFromCGR(Context context,String workspacepath,String cgrfileName) throws Exception
    {
        byte[] buf = new byte[1024];
        StringBuffer zip3dxmlfile=new StringBuffer();
        StringBuffer cgrFile=new StringBuffer();
        String cgrBaseName="";
        try{
            if(cgrfileName.length()>0)
            {
             cgrBaseName=getFileBaseName(cgrfileName);
             cgrFile.append(workspacepath);
             cgrFile.append(java.io.File.separatorChar);
             cgrFile.append(cgrfileName);
             zip3dxmlfile.append(workspacepath);
             zip3dxmlfile.append(java.io.File.separatorChar);
             zip3dxmlfile.append(cgrBaseName + ".3dxml");

             ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip3dxmlfile.toString()));
             FileInputStream in = new FileInputStream(cgrFile.toString());

             out.putNextEntry(new ZipEntry(cgrBaseName + ".3DRep"));
             // Transfer bytes from the cgrfile to the 3DRep file of 3dxml ZIP file
             int len;
             while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
             }
             out.closeEntry();
             in.close();

             out.putNextEntry(new ZipEntry("Manifest.xml"));
             XMLOutputter outputter = new XMLOutputter();
             outputter.output(prepareManifest(cgrBaseName), out);
             out.closeEntry();

             out.putNextEntry(new ZipEntry(cgrBaseName + ".3dxml"));
             outputter = new XMLOutputter();
             outputter.output(prepare3DXMLDocument(cgrBaseName,context.getUser()), out);
             out.closeEntry();
             out.close();
            }
           }
           catch (IOException e)
           {
             System.out.println("Exception in emxImageManagerBase"+e.toString());
           }
    }

}//end of class
