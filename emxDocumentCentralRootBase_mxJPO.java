/*
 **  emxDocumentCentralRootBase.java
 **
 **  Copyright (c) 1992-2016 Dassault Systemes.
 **  All Rights Reserved.
 **  This program contains proprietary and trade secret information of
 **  MatrixOne, Inc.  Copyright notice is precautionary only
 **  and does not evidence any actual or intended publication of such program
 **
 **  FileName : "$RCSfile: ${CLASSNAME}.java.rca $"
 **  Author   : Anil KJ
 **  Version  : "$Revision: 1.25 $"
 **  Date     : "$Date: Wed Oct 22 16:02:41 2008 $"
 **
 **  staic const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.25 Wed Oct 22 16:02:41 2008 przemek Experimental przemek $";
 */

import matrix.db.Context;


/**
 * The <code>emxDocumentCentralRootBase</code> class.
 *
 * @exclude
 */

public class emxDocumentCentralRootBase_mxJPO extends  emxDocumentCentralObject_mxJPO
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

    public emxDocumentCentralRootBase_mxJPO (Context context,
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

    public emxDocumentCentralRootBase_mxJPO (String id)
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

    public int mxMain ( Context context , String[] args ) throws Exception
    {
        if ( true )
        {
            throw new Exception ("Must specify method on " +
                                 "emxDocumentCentralRootBase Invocation");
        }

        return 0;
    }


    /**
     * This method gets Which System Vaults the user shoulb be able to use
     *
     * @param context the eMatrix <code>Context</code> object
     * @param typeName the Java <code>String</code> object for Object's type
     *
     * @return the Java <cose>String[]</code>
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */
     /*
    protected static String[] systemVaults ( Context context , String typeName )
            throws Exception
    {

        *  Author     : Anil KJ
        *  Date       : 11/12/02
        *  Notes      :
        *  History    :
        *

       MxDebug.enter ();


      // Get Locale
      //
      Locale locale = ${CLASS:emxMailUtil}.getLocale ( context );

      // get symbolic name for type
      //
      String type = FrameworkUtil.getAliasForAdmin(context,
                                                   "type",
                                                   typeName,
                                                   true);
      // Reading Property Values from Application Property files

      // default vault for type
      //
      String strDefVault ="eServiceSuiteDocumentCentral.DefaultVault." + type;

      String typeVault =
        ${CLASS:emxMailUtil}.getString("emxDocumentCentral",
                                       strDefVault,
                                       null,
                                       locale );
      // from symbolic vault name get actual vault name
      //
      if ( typeVault != null && typeVault.trim().length() > 0 )
      {
          typeVault = PropertyUtil.getSchemaProperty(context,typeVault);
      }

      // exposeCompanyEmployee setting
      //
      String strExposeEmployees =
        "eServiceDocumentCentral.Schema.ExposeCompanyEmployees";
      String exposeCompanyEmployees =
        ${CLASS:emxMailUtil}.getString("emxDocumentCentral",
                                       strExposeEmployees,
                                       null,
                                       locale);
      // vault aware
      //
      String strVaultAware = "eServiceDocumentCentral.Schema.VaultAwareness";
      String vaultAwareness =
        ${CLASS:emxMailUtil}.getString("emxDocumentCentral" ,
                                       strVaultAware,
                                       null,
                                       locale );
      StringList vaultList = new StringList ();
      // company for context person
      //
      Company company = Person.getPerson(context).getCompany(context);
      // If a default vault for this type of object has been specified in
      // the DC Configurator then that vault will be used.
      //
      if ( typeVault !=null && typeVault.trim ().length () > 0 )
      {
        vaultList.addElement ( typeVault );
      }
      else if ( !exposeCompanyEmployees.trim ().equalsIgnoreCase ( "true" )
                || vaultAwareness.trim ().equalsIgnoreCase ( "true" ) )
      {
        // If no default vault for this type of object has been specified
        // If ExposeCompanyEmployees setting in DC Configurator is set to
        // FALSE, the vault associated to the Person's Company will be used
        //                  (or)
        // If ExposeCompanyEmployees setting is TRUE then
        // If the VaultAwareness setting in DC Configurator is set to TRUE
        // the object will be created only in the vault associated to the
        // Person's Company object

        String strDefaultVault =
          (new DomainObject().getDefaultVault(context,company)).toString();
        vaultList.addElement (strDefaultVault);
      }
      else
      {
        // If the VaultAwareness setting is FALSE, then the user will be
        // presented with a list of vaults in the system and asked to
        // choose the vault for the object being created
        //
        VaultItr vaultItr =
          new VaultItr ( Vault.getVaults ( context , true ) );

        while ( vaultItr.next () )
        {
          vaultList.addElement ( vaultItr.obj ().toString () );
        }
      }


      String[] vaultArray = null;
      // convert list to array
      //
      if ( vaultList.size () > 0 )
      {
        vaultArray = new String[ vaultList.size () ];
        vaultList.toArray ( vaultArray );
      }
      MxDebug.exit ();
      return vaultArray;
    }

    */
    /**
     * Unpack parameters. Cast back to Original type. Call same method with
     * real data types.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args the Java <code>String[]</code> object holding Object type name
     *
     * @return the Java <cose>String[]</code>
     *
     * @throws Exception if the operation fails
     *
     * @since AEF 9.5.0.0
     */
     /*

    public static String[] systemVaults ( Context context , String[] args )
            throws Exception
    {

        *  Author     : Rajesh Venugopalan
        *  Date       : 11/12/02
        *  Notes      :
        *  History    :
        *


       MxDebug.enter ();
       String typeName = ( String ) JPO.unpackArgs ( args );

       String[] astrSystemVaults = systemVaults ( context , typeName );

       MxDebug.exit ();
       return astrSystemVaults;
    }
    */
}
