import matrix.db.Context;

public class emxTypeLineage_mxJPO
{

    public emxTypeLineage_mxJPO(Context ctx, String[] args) {
    }

    public String getLineage(Context ctx, String [] args) {
        try
        {
            return com.matrixone.search.index.impl.TypeLineage.getLineage(ctx, args[0]);
        }
        catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        }
        return "";
    }
}
