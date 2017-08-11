//
// $Id: ${CLASSNAME}.java.rca 1.8 Wed Oct 22 16:02:38 2008 przemek Experimental przemek $ 
//


import matrix.db.Context;

public class emxFileContainer_mxJPO extends emxFileContainerBase_mxJPO
{
   public emxFileContainer_mxJPO ()
   {
      super();
   }

   public emxFileContainer_mxJPO (Context context, String[] args)
                          throws Exception
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

    public emxFileContainer_mxJPO (String id) throws Exception
    {
        // Call the super constructor

        super (id);
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
    public int mxMain (Context context, String[] args) throws Exception
    {
       if (true)
          throw new Exception ("Don't use this!  emxFileContainer");
        return 0;
    }

}
