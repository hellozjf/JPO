// emxBookmark.java
//
// Copyright (c) 2002-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//
// static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.8.2.2 Thu Dec  4 07:56:06 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.8.2.1 Thu Dec  4 01:54:57 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.8 Wed Oct 22 15:49:22 2008 przemek Experimental przemek $

/* Revision 1.1  2004/09/27  23:04:12  rcheluva
 * Task PC - Migrate to Configurable Tables
 */

import matrix.db.*;

/********************************************************************************************
*       New JPO for Config Table Conversion Task
*********************************************************************************************/

/**
 * The <code>emxBookmark</code> class represents the Bookmarks JPO
 * functionality for the PMC type.
 *
 * @version PMC 10-6 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxBookmark_mxJPO extends emxBookmarkBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public emxBookmark_mxJPO (Context context, String[] args)
       throws Exception
    {
       super(context, args);
    }

}
