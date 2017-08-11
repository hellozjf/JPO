import matrix.db.Context;

public class emxRelationshipLineage_mxJPO
{

    public emxRelationshipLineage_mxJPO(Context ctx, String[] args) {
    }

    public String getLineage(Context ctx, String [] args) {
        try
        {
            return com.matrixone.search.index.impl.RelationshipLineage.getLineage(ctx, args[0]);
        }
        catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        }
        return "";
    }
}
