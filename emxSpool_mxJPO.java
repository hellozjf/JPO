/*
 **   emxSpool
 **
 **   Copyright (c) 1992-2016 Dassault Systemes.
 **	  @since R210
 */
import matrix.util.MatrixException;

import matrix.db.Context;

/**
 *  The emxSpool class represents the Spool JPO Functionality
 *  Copyright (c) 1992-2016 Dassault Systemes.
 *  @since R210
 **/

public class emxSpool_mxJPO extends emxSpoolBase_mxJPO
{
	/**
	 * Constructs a new emxSpool JPO object
	 * 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args an array of String arguments for this method
	 * @throws Exception if the operation fails
	 * @since R210
	 */
	public emxSpool_mxJPO (Context context, String[] args) throws Exception 
	{
		super(context, args);
	}
}
