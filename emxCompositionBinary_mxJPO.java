import matrix.db.Context;

// emxCompositionBinary.java
//
// Created on June 30, 2011
//
// Copyright (c) 1992,2015 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// Dassault Systemes. Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

/**
  * The <code>emxCompositionBinary</code> class contains utilities to handle
  * Composition Binary.  The methods will create and update the composition binary storage
  * when a relationship is configured for Composition Binary.
  *   
  */
public class emxCompositionBinary_mxJPO extends emxCompositionBinaryBase_mxJPO {

    /**
     * Default Constructor
     * @param context the ENOVIA <code>Context</code> object
     * @param args String array of arguments
     * @throws Exception
     */
    public emxCompositionBinary_mxJPO (Context context, String[] args)
	    throws Exception 
    {
		super(context, args);
    }

}
