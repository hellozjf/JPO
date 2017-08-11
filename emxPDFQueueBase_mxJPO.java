//
// $Id: ${CLASSNAME}.java.rca 1.8 Wed Oct 22 15:53:03 2008 przemek Experimental przemek $ 
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
 *  Version  : "$Revision: 1.8 $"
 *  Date     : "$Date: Wed Oct 22 15:53:03 2008 $"
 *
 */
import java.util.LinkedList;

/**
 *  A Queue based on java.util.LinkedList that is intended to contain
 *  PDFDocuments that are to be printed/rendered
 *
 *@author     Devon Jones
 *@created    February 3, 2003
 *
 *  @exclude
 */
public class emxPDFQueueBase_mxJPO extends LinkedList
{
  /**  Object Id who's files are contained */
  protected String _objectId;


  /**
   *  Constructor for the emxPDFQueueBase object
   *
   *@param  objectId  Object Id
   *
   *@since AEF 9.5.4.0
   */
  public emxPDFQueueBase_mxJPO(String objectId)
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    this._objectId = objectId;
  }


  /**
   *  Adds a document to the PDF Queue
   *
   *@param  document  The feature to be added to the PDFDocument attribute
   *
   *@since AEF 9.5.4.0
   */
  public void addPDFDocument(emxPDFDocument_mxJPO document)
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    super.add(document);
  }


  /**
   *  Gets the next attribute of the emxPDFQueueBase object
   *
   *@return    Passes out the next value from the queue, and removes it
   *
   *@since AEF 9.5.4.0
   */
  public emxPDFDocument_mxJPO getNext()
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    return (emxPDFDocument_mxJPO)super.removeFirst();
  }


  /**
   *  Gets the objectId attribute of the emxPDFQueueBase object
   *
   *@return    The objectId value
   *
   *@since AEF 9.5.4.0
   */
  public String getObjectId()
  {
    /*
     *  Author    : DJ
     *  Date      : 02/04/2003
     *  Notes     :
     *  History   :
     */
    return _objectId;
  }
}

