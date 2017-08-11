/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 * @quickreview ZUD      16:12:02   :IR-478873-3DEXPERIENCER2018x : Table borders are not visible in Requirement Properties page/view.
*/

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.File;
import matrix.db.FileItr;
import matrix.db.FileList;
import matrix.db.JPO;

import com.dassault_systemes.enovia.webapps.richeditor.converter.ConversionUtil;
import com.dassault_systemes.enovia.webapps.richeditor.util.ReferenceDocumentUtil;
import com.dassault_systemes.enovia.webapps.richeditor.util.RichEditUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;


/**
 *
 */
public class emxRichEditorBase_mxJPO extends emxDomainObject_mxJPO
{
        
	/**
	 * 
	 */
	private static final long serialVersionUID = -7804240457333999351L;

	public emxRichEditorBase_mxJPO (Context context, String[] args) throws Exception
	{
		super(context, args);
	}

    /**
     * Returns script to display rich text content for a business object
     * @param context - the eMatrix <code>Context</code> object
     * @param args - None
     * @return HTML script
     * @throws Exception
     */
    public String getRichTextControl(Context context, String[] args) throws Exception {

        Map<String, Map<String, String>> programMap = JPO.unpackArgs(args);
        
        Map<String, String> requestMap = programMap.get("requestMap");
        Map<String, String> paramMap = programMap.get("paramMap");
        
        //Finding mode
        String mode    = (String) requestMap.get("mode");
        
        String objectId = (String) paramMap.get("objectId");
        String relId = (String) paramMap.get("relId");
        String lud = "3";
        String contentType = "";
       
        if(objectId != null) {
            DomainObject dmoObject = DomainObject.newInstance(context, objectId);
            String modifed = dmoObject.getInfo(context, DomainConstants.SELECT_MODIFIED); 
            lud = eMatrixDateFormat.getJavaDate(modifed).getTime() + "";     
            
            contentType = dmoObject.getAttributeValue(context, "Content Type");
        }
        
        if(objectId == null) {
        	mode = "Create";
        }
        
        // Form Create and Edit. //Unnecessary scroll bar removal, 'style' modified.
    	StringBuilder sb = new StringBuilder();
    	//sb.append("<div id=\"NewRichTextEditor\" style=\"max-height:170px; height:150px; min-height:20px; overflow-y: auto; overflow-x: auto;\">");
    	sb.append("<div id=\"NewRichTextEditor\" class=\"richTextContainer\" style=\"height:245px; min-height:20px; overflow-y: auto; overflow-x: auto;\">");
    	if("Create".equalsIgnoreCase(mode) || "Edit".equalsIgnoreCase(mode)){
    		sb.append("<img src='images/loading.gif' id='loadingGifFormRMT' load='getRichTextEditor(\"Edit\", \"").append(objectId != null ? objectId : "")
    		  .append("\", \"").append(relId != null ? relId : "").append("\",\"" + lud + "\", \"").append(contentType != null ? contentType : "").append("\")' />" );
    	}
    	sb.append("</div><script type=\"text/javascript\">")
    	  .append("$.getScript('../productline/RichTextEditorVariables.jsp', function () { ")
    	  		.append("require(['DS/RichEditorCusto/Form'], function () { ")
    	  			.append("getRichTextEditor('NewRichTextEditor', '").append("Create".equalsIgnoreCase(mode) || "Edit".equalsIgnoreCase(mode) ? "Edit" : "View")
    	  				.append("', '").append(objectId != null ? objectId : "").append("', '").append(relId != null ? relId : "")
    	  				.append("'," + lud + ", '").append(contentType != null ? contentType : "").append("');" )
    	  		.append("});")
    	  .append("}); </script>");
       return  sb.toString();
        		     
        //}
        
       //Form view
       //return contentData;
       //Fix field size for Content Data field
    }
    
    /**
     * Trigger to copy files for rich text editing to new object Revision or Clone, if they are not copied by default.
     * @param context
     * @param args
     * @throws Exception
     */
    public void copyFileForRichText(Context context, String args[]) throws Exception {
    	DomainObject oldObj = this; 
    	DomainObject newObj = DomainObject.newInstance(context, args[0]);
    	
    	FileList oldFiles = oldObj.getFiles(context);
    	if(newObj.getFiles(context).size() != oldFiles.size()) {
    		FileItr fileItr = new FileItr(oldFiles);
            while (fileItr.next())
            {
                File file = fileItr.obj();
                String fileName = file.getName();
                String format = file.getFormat();
                if (fileName.length() > 0 && ("rco".equalsIgnoreCase(format) || fileName.startsWith(RichEditUtil.PREFIX_ATT))) {
                	ReferenceDocumentUtil.copyFile(context, oldObj, newObj, format, fileName);
                }
            }
    	}
    }    
    
    
    /**
     * Trigger to generate HTML preview for .docx being checked in. 
     * This trigger is skipped when RPE SkipHTMLPreviewGen is set to true.
     * @param context
     * @param args
     * @throws Exception
     */
    public void generateHTMLPreview(Context context, String args[]) throws Exception {
    	DomainObject obj = DomainObject.newInstance(context, args[0]);
    	String fileName = args[1];
    	String format = args[2];
    	boolean skip = "TRUE".equalsIgnoreCase(PropertyUtil.getGlobalRPEValue(context, RichEditUtil.RPE_SKIP));
    	if(skip || !RichEditUtil.FORMAT_DOC.equalsIgnoreCase(format) || !fileName.startsWith(RichEditUtil.PREFIX_ATT)) {
    		return;
    	}
    	String attrName = fileName.substring(RichEditUtil.PREFIX_ATT.length(), fileName.lastIndexOf("."));
    	Path p = ReferenceDocumentUtil.checkoutFile(context, obj, format, fileName);
        
 	   try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(p.toFile()))){
	       if(RichEditUtil.DEFAULT_ATT_NAME.equalsIgnoreCase(attrName)) {
		   		String[] converted = ConversionUtil.getHTMLAndPlainText(bis); 
		   		obj.setAttributeValue(context, attrName, converted[0]);
	        	obj.setAttributeValue(context, RichEditUtil.ATT_CONTENT_TEXT, converted[1]);
	        }
	       else{
	    	   try(InputStream is = ConversionUtil.convert(context, null, bis, ConversionUtil.Format.DOCX, ConversionUtil.Format.HTML_PREVIEW)){
		   	   		String html = ConversionUtil.streamToString(is);
		   	   		obj.setAttributeValue(context, attrName, html);
	    	   }
	       }
	   }
       Files.delete(p);
       Files.delete(p.getParent());
    }    
    
    /**
     * Trigger to delete RCO file when the RCO is deleted.
     * @param context
     * @param args
     * @throws Exception
     */
    public void tidyRCOFiles(Context context, String args[]) throws Exception {
    	String content = args[0];
		int start = 0;
		Vector<String> RCOs = new Vector<>();
		do{
			int fileNameStart = content.indexOf("|rcoFileName|:|", start),
					fileNameEnd = content.indexOf(",", fileNameStart);
			if(fileNameStart == -1) {
				break;
			}
			String fileName = content.substring(fileNameStart + "|rcoFileName|:|".length(), fileNameEnd -1 );
			RCOs.add(fileName);
			start = fileNameEnd;
		}while(start != -1);    	
		
		DomainObject obj = this; 
    	FileList files = obj.getFiles(context);
		FileItr fileItr = new FileItr(files);
        while (fileItr.next())
        {
            File file = fileItr.obj();
            String fileName = file.getName();
            String format = file.getFormat();
            if ("rco".equalsIgnoreCase(format) && !RCOs.contains(fileName)) {
            	ReferenceDocumentUtil.deleteFile(context, obj, format, fileName);
            }
        }
    }    
}//End of class

