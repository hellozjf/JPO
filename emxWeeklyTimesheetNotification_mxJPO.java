/*
 * Copyright (c) 1992-2016 Dassault Systemes.
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of MatrixOne,
 * Inc.  Copyright notice is precautionary only
 * and does not evidence any actual or intended publication of such program
 */
import matrix.db.Context;

public class emxWeeklyTimesheetNotification_mxJPO extends emxWeeklyTimesheetNotificationBase_mxJPO
{
	/**
	 * Constructs a new emxSubmitedTimesheetNotification JPO object
	 * 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args an array of String arguments for this method
	 * @throws Exception if the operation fails
	 * @since R210
	 */
	public emxWeeklyTimesheetNotification_mxJPO (Context context, String[] args) throws Exception 
	{
		super(context, args);
	}
}
