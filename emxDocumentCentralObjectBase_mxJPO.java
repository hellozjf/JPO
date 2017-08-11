/*
 **  emxDocumentCentralObjectBase.java
 **
 **  Copyright (c) 1992-2016 Dassault Systemes.
 **  All Rights Reserved.
 **  This program contains proprietary and trade secret information of
 **  MatrixOne, Inc.  Copyright notice is precautionary only
 **  and does not evidence any actual or intended publication of such program
 **
 **  FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 **  Author   : Anil KJ
 **  Version  : "$Revision: 1.20 $"
 **  Date     : "$Date: Wed Oct 22 16:02:44 2008 $"
 **
 **  static const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.20 Wed Oct 22 16:02:44 2008 przemek Experimental przemek $";
 */

import com.matrixone.apps.document.DocumentCentralConstants;
import com.matrixone.apps.document.util.MxDebug;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.CommonDocument;

import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;

import matrix.util.StringList;
import matrix.util.StringResource;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * The <code>emxDocumentCentralObjectBase</code> class.
 *
 * @exclude
 */
public class emxDocumentCentralObjectBase_mxJPO extends DomainObject
   implements DocumentCentralConstants
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */

    public emxDocumentCentralObjectBase_mxJPO (Context context,
                         String[] args) throws Exception
    {
        super(args[0]);
    }

    /**
     * Constructor
     *
     * @param id the Java <code>String</code> object
     *
     * @throws Exception if the operation fails
     *
     */

    public emxDocumentCentralObjectBase_mxJPO (String id)
            throws Exception
    {
        // Call the super constructor
        super(id);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <String[]</code> object
     *
     * @return the Java <code>int</code>
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */

    public int mxMain (Context context, String[] args ) throws Exception
    {
        if ( true )
        {
            throw new Exception ("Do not call this method!");
        }

        return 0;
    }



    /**
      * Implemented for copying into Document object checked in files from source
      * Files will be checked in as Version Documents as per Common Document implementation
      *
      * @param context the eMatrix <code>Context</code> object
      * @param cloneObj, the BusinessObject which is the clone
      * @param copyFiles, a boolean to decide whether to copy files or not
      * @throws FrameworkException if the operation fails
      *
      * @since  AEF 10-5
      */

    public CommonDocument copyDocumentFiles(Context context, BusinessObject cloneObj)
        throws FrameworkException
    {
       try
       {
         ContextUtil.startTransaction(context, true);

         String RELATIONSHIP_ACTIVE_VERSION = PropertyUtil.getSchemaProperty(context,SYMBOLIC_relationship_ActiveVersion);
         String SELECT_ACTIVE_FILE_VERSION_ID = "from[" + RELATIONSHIP_ACTIVE_VERSION + "].to.id";

         StringList selects = new StringList(8);
         selects.add(SELECT_VAULT);
         selects.add(SELECT_REVISION);
         selects.add(SELECT_FILE_NAME);
         selects.add(SELECT_ACTIVE_FILE_VERSION_ID);
         Map objectMap = getInfo(context, selects);
         CommonDocument newMasterObject = new CommonDocument(cloneObj);
         String fileName = "";
         StringList fileList = (StringList) objectMap.get(SELECT_FILE_NAME);
         Iterator itr = fileList.iterator();
         while( itr.hasNext() )
         {
           fileName = (String) itr.next();
           newMasterObject.createVersion(context, null, fileName, null);
         }
         ContextUtil.commitTransaction(context);
         return newMasterObject;
      }
      catch (Exception ex )
      {
         ContextUtil.abortTransaction(context);
         ex.printStackTrace();
          throw new FrameworkException(ex);
      }
    }
    /**
      * Implemented for copying into Document object checked in files from source
      * Files will be checked in as Version Documents as per Common Document implementation
      * @param context the eMatrix <code>Context</code> object
      * @param args, the BusinessObject which is the clone
      * @throws FrameworkException if the operation fails
      *
      * @since  AEF 10-5
      */
    public void copyDocumentFiles( Context context , String[] args )
            throws Exception
    {
       try {
          Map paramMap = ( Map ) JPO.unpackArgs ( args );
          BusinessObject boNewObject = (BusinessObject)paramMap.get ("newObject");
          copyDocumentFiles(context,boNewObject);
   }
   catch (Exception ex ){
       ex.printStackTrace();
       throw new FrameworkException(ex);
   }
}

    /**
     * Pulls from the specification map the required values, and then invokes
     *   necessary method for notifying the users.
     *
     * @param context A Matrix DB context
     * @param objectId The Matrix ObjectId of the object for which
     * notification should be sent.
     * @param specificationMap A specification map, containing the keys for
     * the message (JPO_SPEC_NOTIFY_MESSAGE) and the users to whom the
     * notification is to be sent (JPO_SPEC_NOTIFY_TO_USERS).
     *
     * @since  AEF 9.5.6.0
     * @grade 0
     */
    public void handleNotifyUsers (Context        context,
                                   BusinessObject busObject,
                                   Map            specificationMap)
       throws Exception
    {
       /**
        *  Author   : Kevin H. Olson
        *  Date     : 2003/03/03
        *  Notes    :
        *  History  :
        */
       MxDebug.utilEnter ();

       String subject =
          (String) specificationMap.get (JPO_SPEC_NOTIFY_SUBJECT);
       String message =
          (String) specificationMap.get (JPO_SPEC_NOTIFY_MESSAGE);
       String toUsersList =
          (String) specificationMap.get (JPO_SPEC_NOTIFY_TO_USERS);

       notifyUsers (context,
                    busObject,
                    subject,
                    message,
                    toUsersList);

       MxDebug.utilExit ();
    } //handleNotifyUsers


    /**
     * Entry point for notifying users that a object is being created.
     * <p>
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    subject           - subject
     *    message           - message to send
     *    user list         - users to send messages to
     * @throws Exception if a major failure occurs
     * @since AEF
     * @author  Michael Bingman on 02/02/2004
     */
    public void notifyUsers ( Context context , String[] args )
            throws Exception
    {
       try
       {
          Map specificationMap = ( Map ) JPO.unpackArgs ( args );

          String objectId = ( String ) specificationMap.get (
             DocumentCentralConstants.OBJECT_ID );

          String subject =
             (String) specificationMap.get (JPO_SPEC_NOTIFY_SUBJECT);
          String message =
             (String) specificationMap.get (JPO_SPEC_NOTIFY_MESSAGE);
          String toUsersList =
             (String) specificationMap.get (JPO_SPEC_NOTIFY_TO_USERS);

          BusinessObject busObject = new BusinessObject (objectId);

          notifyUsers (context,
                       busObject,
                       subject,
                       message,
                       toUsersList);
       }
       catch ( Exception e )
       {
          // missing argument key or bad cast
          MxDebug.exception (e, true);
          throw new IllegalArgumentException ();
       }

    }

    /**
     * Notify Users
     *
     * @param context the eMatrix <code>Context</code> object
     * @param busObj
     * @param message
     * @param toUsersList
     * @throws Exception if the operation fails
     */
    protected void notifyUsers (Context        context,
                                BusinessObject busObj,
                                String         subject,
                                String         message,
                                String         toUsersList)
            throws Exception
    {
       MxDebug.enter ();

      if ( toUsersList != null
           && toUsersList.length() > 0
           && ! toUsersList.equals("null"))
      {


        StringTokenizer st = new StringTokenizer ( toUsersList , ";" );
        StringList toList  = new StringList ();

        // Iterate Through The User List
        //
        while ( st.hasMoreTokens () )
        {
          String temp = st.nextToken ();
          toList.addElement ( temp );
        }


        Locale locale = emxMailUtil_mxJPO.getLocale ( context );

        // Reading Property Values from Application Property files
        //
        StringList objList = new StringList ( 1 );

        busObj.open (context);

        String type     = busObj.getTypeName ();
        String name     = busObj.getName ();
        String revision = busObj.getRevision ();
        String objectId = busObj.getObjectId ();
        busObj.close (context);

        objList.addElement (objectId);

        Person person      = Person.getPerson(context);
        String companyName = person.getCompany(context).getName();

        if (subject == null || subject.length () == 0)
        {
           String strNotificationSub =
              "emxDocumentCentral.DocumentCentralCommon.NotificationSubject";
           subject =
              emxMailUtil_mxJPO.getString("emxDocumentCentralStringResource",
                                             strNotificationSub,
                                             null,
                                             locale );
        }

        if (message == null || message.length () == 0)
        {
          String [] msgKeys = new String[] {"type",
                                            "name",
                                            "revision",
                                            "user",
                                            "type" };
          type = i18nNow.getTypeI18NString(type,context.getSession().getLanguage());
          String[] msgValues = new String[] {type,
                                             name,
                                             revision,
                                             context.getUser(),
                                             type };

          String strNotificationMessage =
            "emxDocumentCentral.DocumentCentralCommon.NotificationMessage";
          String msg =
            emxMailUtil_mxJPO.getString ("emxDocumentCentralStringResource",
                                            strNotificationMessage,
                                            null,
                                            locale);

          message = StringResource.format (msg, msgKeys, msgValues);
        } //if
        else
        {
          String strAutoName =
            "emxDocumentCentral.DocumentCentralCommonWizard.AutoName";

          String autoName =
             emxMailUtil_mxJPO.getString("emxDocumentCentralStringResource",
                                           strAutoName,
                                           null,
                                           locale);

          int autoNameIndex = message.indexOf(autoName);

          if( autoNameIndex != -1 )
          {
            message = message.substring (0, autoNameIndex)
               + name + message.substring (autoNameIndex + autoName.length());
          }
        } //else

        MxDebug.message (MxDebug.DL_7,
                         "Sending the mail notification now for "
                         + type + ": " + name + " : " + revision);

        MailUtil.sendNotification (context,
                                   toList,
                                   null,
                                   null,
                                   subject,
                                   null,
                                   null,
                                   message,
                                   null,
                                   null,
                                   objList,
                                   companyName);
      }

      MxDebug.exit ();
    }


}
