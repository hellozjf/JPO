import matrix.db.Context;

/**
*
*${CLASSNAME}.java
*
*class for handling user interaction with the Requirement Group data model
*
*/

/**
*@author NZR
*/

public class emxRequirementGroup_mxJPO extends emxRequirementGroupBase_mxJPO
{

	/*serialization compatibility level*/
	private static final long serialVersionUID=1L; 
	
	/*constructor*/
	public emxRequirementGroup_mxJPO (Context context, String[] args)
	throws Exception
	{
		super(context,args);	
	
	}

	/**
	*@param context : MatrixOne context
	*@param args : MatrixOne packed arguments
	*@return an integer - status code. 0 if OK. 
	*@throws Exception if problem in AEF or unconnected context.
	*
	*/
	public int mxMain(Context context, String[] args)
	throws Exception
	{
		if(!context.isConnected())
		{
			throw new Exception("Not supported on desktop client"); 
		}
		return 0;
	}

}
