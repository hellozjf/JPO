//
// $Id: ${CLASSNAME}.java.rca 1.17 Wed Oct 22 16:02:17 2008 przemek Experimental przemek $
//
/*
 **   Copyright (c) 1992-2016 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of
 **   MatrixOne, Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program
 **
 **   FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 **   Author   :  rajeshv
 **   Version  : "$Revision: 1.17 $"
 **   Date     : "$Date: Wed Oct 22 16:02:17 2008 $"
 **
 */

import com.matrixone.apps.domain.DomainObject;

import com.matrixone.apps.domain.util.MqlUtil;

import com.matrixone.apps.document.util.MxDebug;
import com.matrixone.apps.document.util.FlowControlException;

import matrix.db.BusinessType;
import matrix.db.BusinessTypeItr;
import matrix.db.BusinessTypeList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.StringTokenizer;

/**
 * The <code>emxDocumentCentralCommonBase</code> class.
 *
 * @exclude
 */

public class emxDocumentCentralCommonBase_mxJPO extends emxDocumentCentralRoot_mxJPO
{

  private static final String THIS_FILE = "emxDocumentCentralCommonBase";

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object, contains the object Id for the object
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */

    public emxDocumentCentralCommonBase_mxJPO (Context context, String[] args) throws Exception
    {
        super (args[0]);

        if ((args != null) && (args.length > 0))
        {
            setId (args[0]);
        }
    }

    /**
     * Constructor
     *
     * @param id the Java <code>String</code> object
     *
     * @throws Exception if the operation fails
     *
     */

    public emxDocumentCentralCommonBase_mxJPO (String id) throws Exception
    {
        // Call the super constructor

        super (id);
    }

    //~ Methods -------------------------------------------------------------

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

    public int mxMain (Context context, String[] args) throws Exception
    {
        if (true)
        {
            throw new Exception (
                "must specify method on emxDocumentCentralCommonBase invocation"
            );
        }

        return 0;
    }

    /**
     * Unpack parameters. Cast back to Original type. Call same method with
     * real data types.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object
     *
     * @return the Java<cose>Map</code>
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */

    public static Map subAndRelatedTypes (Context context, String[] args)
            throws Exception
    {
        /*
        *  Author     : Rajesh Venugopalan
        *  Date       : 11/12/02
        *  Notes      :
        *  History    :
        *
        */
        if ((args == null) || (args.length < 1))
        {
            throw (new IllegalArgumentException ());
        }

        return (subAndRelatedTypes (context, (Map) JPO.unpackArgs (args)));
    }

    /**
     * This method gets subtypes of the basetype
     *
     * @param context the eMatrix <code>Context</code> object
     * @param aMap the Java <code>String[]</code> object
     *
     * @return the Java <cose>Map</code>
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */
    protected static Map subAndRelatedTypes (Context context, Map aMap)
            throws Exception
    {
        /*
        *  Author     : Rajesh Venugopalan
        *  Date       : 11/12/02
        *  Notes      :
        *  History    :
        *
        */
       MxDebug.enter (MxDebug.DL_10);

       String objectType   = (String) aMap.get ("className");
       String direction    = (String) aMap.get ("direction");
       String relationShip = (String) aMap.get ("relationshipName");

       MxDebug.location (MxDebug.DL_10,
                         "Input arguments:"
                         + "\n\tobjectType:   \"" + objectType + "\""
                         + "\n\tdirection:    \"" + direction + "\""
                         + "\n\trelationship: \"" + relationShip + "\"");
       HashMap compareMap = new HashMap ();

       Map map = new TreeMap ();

       try
       {
          //if relationship is passed find the to/from types
          BusinessTypeList busList = null;

          if ((relationShip != null) && (relationShip.trim ().length () > 1))
          {
             if ((direction != null) && direction.equalsIgnoreCase ("To"))
             {
                busList = new RelationshipType (
                   relationShip).getToTypes (context);
             }
             else if ((direction != null)
                      && direction.equalsIgnoreCase ("From"))
             {
                busList = new RelationshipType (
                   relationShip).getFromTypes (context);
             }
          }
          else if (objectType != null)
          {
             //if object type is passed find the subtypes Ie No
             //Relationship is Passed


             String mqlCmd = "print type $1 select derivative dump $2";
             try {
                String mqlResult = MqlUtil.mqlCommand (context, mqlCmd, objectType, "|");


                StringTokenizer tokenizer
                   = new StringTokenizer (mqlResult, "|");

                if (!tokenizer.hasMoreTokens ()) {
                   throw new FlowControlException ();
                }

                busList = new BusinessTypeList ();
                while (tokenizer.hasMoreTokens ())
                {
                   BusinessType tmpType
                      = new BusinessType (tokenizer.nextToken (),
                                          context.getVault ());
                   busList.addElement (tmpType);
                }
             }
             catch (Exception e) { }

             String strAbstract = (String) new Boolean (
                (new BusinessType (
                objectType, context.getVault ())).isAbstract (
                   context )).toString ();

             map.put (objectType, DomainObject.EMPTY_STRING + ","
                      + strAbstract);
             compareMap.put (objectType, objectType);
          }

          if (busList == null) {
             throw new FlowControlException ();
          }

          MxDebug.location (MxDebug.DL_10,
                            "Raw return:  " + busList.toString ());

          BusinessTypeItr typeListItr = new BusinessTypeItr (busList);

          //loop thro' business Types , find the parent of each Type,
          //put these in a Map
          // TreeMap : Key-Business Type, Value-Parent Type
          //(Models a Self-Referential Table)

          // Iterate Through The To Itr list

          while (typeListItr.next ())
          {
             BusinessType busType  = typeListItr.obj ();
             String       typeName = busType.getName ();

             compareMap.put (typeName, typeName);
          }

          // Iterate Through The From Itr list
          typeListItr = new BusinessTypeItr (busList);
          while (typeListItr.next ())
          {
             BusinessType busType       = typeListItr.obj ();
             String       typeName      = busType.getName ();
             String       objParentName = (String) busType.getParent (context);

             if (compareMap.containsKey (objParentName))
             {
                map.put (typeName,
                         busType.getParent (context) + "," +
                         (new Boolean (busType.isAbstract (context))).toString ());
             }
             else
             {
                map.put (typeName,
                         DomainObject.EMPTY_STRING + "," +
                         (new Boolean (busType.isAbstract (context))).toString ());
             }
          }
       }
       catch (FlowControlException fce) {
          MxDebug.location (MxDebug.DL_10,
                            "Proper request condition was not specified!");
       }

       MxDebug.location (MxDebug.DL_10,
                         "Final subAndRelatedTypes return:  "
                         + map.toString ());

       MxDebug.exit (MxDebug.DL_10);
       return map;
    }
}
