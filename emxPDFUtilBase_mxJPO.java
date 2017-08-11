//
// $Id: ${CLASSNAME}.java.rca 1.11 Wed Oct 22 15:53:02 2008 przemek Experimental przemek $ 
//
/*
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  All Rights Reserved.
 *  This program contains proprietary and trade secret information of MatrixOne,
 *  Inc.  Copyright notice is precautionary only
 *  and does not evidence any actual or intended publication of such program
 *
 *  FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 *  Author   : "$Author: przemek $"
 *  Version  : "$Revision: 1.11 $"
 *  Date     : "$Date: Wed Oct 22 15:53:02 2008 $"
 *
 */
import matrix.db.Context;
import matrix.db.IconMail;
import matrix.db.JPO;
import matrix.util.StringList;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.HashMap;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.document.util.MxDebug;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;

/**
 *  This class is a catch all class for utility methods.  It contains what
 *  should be static methods, (but they can't be called from the JPO
 *  environment)  instantiate this class, and you can then call any of the
 *  utility functions it contains.
 *
 *@author     Devon Jones
 *@created    February 3, 2003
 *
 *  @exclude
 */
public class emxPDFUtilBase_mxJPO {
  /**  language field, to cach for localization */
  private static String _languages = "";


  /**
   *  Constructor for the emxPDFUtilBase object
   *
   *@param  objectId  Object Id
   *
   *@since AEF 9.5.4.0
   */
  public emxPDFUtilBase_mxJPO() {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
  }

  /**
   *  Constructor for the emxPDFUtilBase object
   *
   *@param  objectId  Object Id
   *
   *@since AEF 9.5.4.0
   */
  public emxPDFUtilBase_mxJPO(Context context,
                      String[] args) {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
  }

  /**
   *  Returns if Batch Printing is Enabled
   *@param context the eMatrix <code>Context</code> object
   *@param args holds no arguments
   *
   *@return Boolean
   *@since AEF 9.5.4.0
   */
  public Boolean isBPEnabled(Context context,
                             String[] args)
  {
    /*
     *  Author    : DJ
     *  Date      : 03/21/2003
     *  Notes     :
     *  History   :
     */
     MxDebug.enter ();
     String strGeneric_Document = PropertyUtil.getSchemaProperty(context,"type_GenericDocument");
     boolean bpEnable   = false;

     HashMap programMap = null;
     try{
        programMap = (HashMap)  JPO.unpackArgs(args);
     }catch(Exception ex){
        MxDebug.exception (ex, true);
     }
     MxDebug.exit ();

     String baseType = (String)programMap.get("baseType");

     if(baseType != null && !"null".equals(baseType) && !"".equals(baseType)){
         if(baseType.equalsIgnoreCase(strGeneric_Document)){
            bpEnable = true;
         }
     }
     else {
        bpEnable = true;
     }
     String enabled = getString(context,
         "eServiceBatchPrintPDF.Batch.BatchPrintAvailable");
     if(bpEnable && enabled.equalsIgnoreCase("True")) {
       return new Boolean(true);
     }
     else {
       return new Boolean(false);
     }
  }


  /**
   *  Returns if PDF Rendering is Enabled
   *@param context the eMatrix <code>Context</code> object
   *@param args holds no arguments
   *
   *@return Boolean
   *@since AEF 9.5.4.0
   */
  public Boolean isRNDEnabled(Context context,
                              String[] args)
  {
    /*
     *  Author    : DJ
     *  Date      : 03/21/2003
     *  Notes     :
     *  History   :
     */
    String enabled = getString(context,
        "eServiceBatchPrintPDF.PDF.PDFRenderingAvailable");
    if(enabled.equalsIgnoreCase("True")) {
      return new Boolean(true);
    }
    else {
      return new Boolean(false);
    }
  }


  /**
   *  Method copied from emxMailUtil class.  From version 9.5.4.0 AEF
   *  This method is used to send an Icon Mail
   *
   *@param context the eMatrix <code>Context</code> object
   *@param  toList         TO:
   *@param  ccList         CC:
   *@param  bccList        BCC:
   *@param  subject        Subject:
   *@param  message        Message Body:
   *@exception  Exception  MatrixException - Matrix Icon Mail exceptions
   *
   *@since AEF 9.5.4.0
   */
  protected static void sendMail(Context context,
                                 StringList toList,
                                 StringList ccList,
                                 StringList bccList,
                                 String subject,
                                 String message)
    throws Exception {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    // viewing Icon mail in application '<' and '>' are read as tags by html
    // These char's need to be eliminated before sending the mail message.
    subject = subject.replace('<', ' ');
    subject = subject.replace('>', ' ');
    message = message.replace('<', ' ');
    message = message.replace('>', ' ');

    // Create iconmail object.
    IconMail mail = new IconMail();
    mail.create(context);

    // Set the "to" list.
    mail.setToList(toList);

    // Set the "cc" list.
    if(ccList != null) {
      mail.setCcList(ccList);
    }

    // Set the "bcc" list.
    if(bccList != null) {
      mail.setBccList(bccList);
    }

    // Set the object list.
    mail.setObjects(null);

    // Set the message.
    mail.setMessage(message);

    // Set the subject and send the iconmail.
    mail.send(context, subject);
  }


  /**
   *  Returns a localized string from the application resource string file
   *
   *@param  context  Matrix Context
   *@param  key      string to get from emxBatchPrintPDF.properties
   *@return          The string value
   *
   *@since AEF 9.5.4.0
   *@grade 0
   */
  protected static String getString(Context context,
                                    String key) {
    return getString(context, key, "emxDocumentCentral");
  }

  /**
   *  Method derived from emxMailUtil class.  From version 9.5.4.0 AEF
   *  Returns a string from a properties file, localized if the string
   *  can be localized
   *
   *@param  context  Matrix Context
   *@param  key      key from properties file
   *@param  propFile Name of properties file
   *@return          The string value
   *
   *@since AEF 9.5.4.0
   *@grade 0
   */
  protected static String getString(Context context,
                                    String key,
                                    String propFile) {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    Locale locale = getLocale(context);
    // Get the string value from the bundle using the key.
    String value = null;
    try 
    {
    	//value = bundle.getString(key);
    	if(propFile.contains("StringResource"))
    	{
    		value = EnoviaResourceBundle.getProperty(context,propFile,context.getLocale(),key);
    	}
    	else
    	{
    		value=EnoviaResourceBundle.getProperty(context,key);
    	}
    } 
    catch(Exception e) 
    {
    	e.printStackTrace();
    	value = key;
    }

    return value;
  }


  /**
   *  Method copied from emxMailUtil class.  From version 9.5.4.0 AEF
   *
   *@param  context  Description of the Parameter
   *@return          The <code>Locale</code> value
   *
   *@since AEF 9.5.4.0
   *@grade 0
   */
  private static Locale getLocale(Context context) {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    String result = _languages.trim();

    if(result.length() == 0) {
      result = context.getSession().getLanguage();
    } else {
      // in case they put more than one language in the property,
      // only use the first one
      int index = result.indexOf(' ');
      if(index != -1) {
        result = result.substring(0, index);
      }
    }
    return (getLocale(result));
  }


  /**
   *  Method copied from emxMailUtil class.  From version 9.5.4.0 AEF
   *
   *@param  language  Description of the Parameter
   *@return           The locale value
   *
   *@since AEF 9.5.4.0
   *@grade 0
   */
  private static Locale getLocale(String language) {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    // this is the country
    String strContry = "";
    // this is the string id of the localize tag, if there is one
    String strLanguage = "";

    // Get locale
    try {
      StringTokenizer st1 = new StringTokenizer(language);
      String locale = st1.nextToken();

      int idxDash = locale.indexOf('-');
      int idxComma = locale.indexOf(',');
      int idxSemiColumn = locale.indexOf(';');
      if((idxComma == -1) && (idxSemiColumn == -1) && (idxDash == -1)) {
        strLanguage = locale;
      } else if(idxDash != -1) {
        boolean cont = true;
        if((idxComma < idxDash) && (idxComma != -1)) {
          if((idxSemiColumn == -1) || (idxComma < idxSemiColumn)) {
            strLanguage = locale.substring(0, idxComma);
            cont = false;
          }
        }

        if((cont) && (idxSemiColumn < idxDash) && (idxSemiColumn != -1)) {
          if((idxComma == -1) || (idxComma > idxSemiColumn)) {
            strLanguage = locale.substring(0, idxSemiColumn);
            cont = false;
          }
        } else if(cont) {
          boolean sec2 = true;
          StringTokenizer st = new StringTokenizer(locale, "-");
          if(st.hasMoreTokens()) {
            strLanguage = st.nextToken();
            if(st.hasMoreTokens()) {
              strContry = st.nextToken();
            } else {
              sec2 = false;
            }
          } else {
            sec2 = false;
          }
          int idx = strContry.indexOf(',');
          if(idx != -1) {
            strContry = strContry.substring(0, idx);
          }
          idx = strContry.indexOf(';');
          if(idx != -1) {
            strContry = strContry.substring(0, idx);
          }
          if(!sec2) {
            System.out.println("MATRIX ERROR - LOCAL INFO CONTAINS WRONG DATA");
          }
        }
      } else {
        if((idxComma != -1) && ((idxComma < idxSemiColumn) || (idxSemiColumn ==
          -1))) {
          strLanguage = locale.substring(0, idxComma);
        } else {
          strLanguage = locale.substring(0, idxSemiColumn);
        }
      }
    } catch(Exception e) {
      strLanguage = "en";
      strContry = "US";
    }

    // Get Resource bundle.
    return new Locale(strLanguage, strContry);
  }
}
