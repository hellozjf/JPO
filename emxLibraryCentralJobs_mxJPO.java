import matrix.db.Context;

/*
**  ${CLASSNAME}
**
**
 * (c) Dassault Systemes, 1993 - 2010.  All rights reserved
 *
**   static const char RCSID[] = $Id: emxJobBase.java.rca 1.5.1.1.1.4 Wed Oct 22 15:57:11 2008 przemek Experimental przemek przemek $
*/


/**
 * The <code>Job</code> class represents Job JPO in common
 * implements methods to access Job lists, and also triggers related to background jobs
 *
 * @version AEF 11.0.0.0 - Copyright (c) 2005, MatrixOne, Inc.
 */

public class emxLibraryCentralJobs_mxJPO extends emxLibraryCentralJobsBase_mxJPO
{

    /**
     * Constructs a new JobBase JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF V11-0-0-0
     */
    public emxLibraryCentralJobs_mxJPO (Context context, String[] args)
        throws Exception
    {
      // Call the super constructor
      super( context, args);

    }

 } // end of class
