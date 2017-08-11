//
// $Id: ${CLASSNAME}.java.rca 1.10 Wed Oct 22 15:53:03 2008 przemek Experimental przemek $ 
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
 *  Version  : "$Revision: 1.10 $"
 *  Date     : "$Date: Wed Oct 22 15:53:03 2008 $"
 *
 */
import matrix.db.Context;

/**
 *  A Queue based on java.util.LinkedList that is intended to contain
 *  PDFDocuments that are to be printed/rendered
 *
 *@author     Ashish Shrivastava
 *@created    October 23,2003
 *
 *@since AEF 10.Minor1
 */
public class emxPDFQueue_mxJPO extends emxPDFQueueBase_mxJPO
{
    /**
     *  Constructor for the emxPDFQueueBase object
     *
     *@param  objectId  Object Id
     *
     *@since AEF 10.Minor1
     */
    public emxPDFQueue_mxJPO(String objectId)
    {
        super(objectId);
    }

}
