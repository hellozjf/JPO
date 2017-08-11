/*
 ** emxTriggerBasedIndexingUtilBase.java
 **
 ** Copyright (c) 1999-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 */

import java.util.Locale;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Map;

import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.BackgroundProcess;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.search.index.Config;
import com.matrixone.search.index.impl.IndexerImpl;

import matrix.db.JPO;
import matrix.db.Context;
import matrix.util.StringList;
import java.lang.String;

/**
 * The <code>emxTriggerBasedIndexingBase</code> class contains methods related to Trigger Based Indexing.
 * @version  - V6R2009_HF0.7 Copyright (c) 2008, MatrixOne, Inc.
 */
public class emxTriggerBasedIndexingUtilBase_mxJPO
{


    /**
     * Create a new emxIssueBase object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @return a emxIssueBase object.
     * @throws Exception if the operation fails.
     * @since V6R2009_HF0.7
     */
    public emxTriggerBasedIndexingUtilBase_mxJPO(Context context, String[] args) throws Exception {
        super();
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since V6R2009_HF0.7
     */
    public int mxMain(Context context, String[] args) throws Exception {
        if (true) {
            String languageStr = context.getSession().getLanguage();
            String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.Invocation", new Locale(languageStr));
            exMsg += EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.TriggerBasedIndexingUtil", new Locale(languageStr));
            throw new Exception(exMsg);
        }
        return 0;
    }

    public static int partialIndex(Context context, String[] args) throws Exception
    {
        try
        {
            String jponame        = "emxTriggerBasedIndexingUtil";
            String methodName     = "index";
            DomainObject obj = DomainObject.newInstance(context, args[0]);
            BackgroundProcess bgp = new BackgroundProcess();
            Context clonedContext = context.getFrameContext(obj.getInfo(context, DomainObject.SELECT_NAME));
            bgp.submitJob(clonedContext, jponame, methodName,args, (String)null);
            return 0;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public static int partialIndexOnCreate(Context context, String[] args) throws Exception
    {
        try
        {
            String jponame        = "emxTriggerBasedIndexingUtil";
            String methodName     = "indexOnCreate";
            DomainObject obj = DomainObject.newInstance(context, args[0]);
            BackgroundProcess bgp = new BackgroundProcess();
            Context clonedContext = context.getFrameContext(obj.getInfo(context, DomainObject.SELECT_NAME));
            bgp.submitJob(clonedContext, jponame, methodName,args, (String)null);
            return 0;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    public int index(Context context, String[] args) throws Exception
    {
        try
        {
            ContextUtil.pushContext(context);
            String arg0 = args[0];
            String[] oids;

            if( arg0.indexOf(TRANS_HISTORY_OBJECT_DELIMITER) >= 0 )
            {
                oids = parseArgument(arg0);
            } else {
                oids = new String[]{arg0};
            }

            IndexerImpl in = new IndexerImpl();
            in.index(context, oids, null, false);

           return 0;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        finally
       {
                  ContextUtil.popContext(context);
        }
    }


    public int indexOnCreate(Context context, String[] args) throws Exception
    {
        try
        {
            ContextUtil.pushContext(context);
            String arg0 = args[0];
            String[] oids;

            if( arg0.indexOf(TRANS_HISTORY_OBJECT_DELIMITER) >= 0 )
            {
                oids = parseArgumentForCreate(arg0);
            } else {
                oids = new String[]{arg0};
            }

            IndexerImpl in = new IndexerImpl();
            in.index(context, oids, null, false);

            return 0;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        finally
        {
            ContextUtil.popContext(context);
        }
    }

    static final String TRANS_HISTORY_OBJECT_DELIMITER = "id=";

    public String[] parseArgument(String transHistories) throws Exception
    {
        try
        {
            StringTokenizer transHistoryTokens = new StringTokenizer(transHistories, "\n");
            String oid = null;
            StringList oidList = new StringList();
            while (transHistoryTokens.hasMoreTokens())
            {
                String transHistory = transHistoryTokens.nextToken().trim();
                int idIndex = transHistory.indexOf(TRANS_HISTORY_OBJECT_DELIMITER);
                oid = transHistory.substring(idIndex+3, transHistory.length());
                oidList.addElement(oid);
            }
            String[] oids = new String[oidList.size()];
            oidList.toArray(oids);
            return oids;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }
    static final String TRANS_HISTORY_CREATE_EVENT_DELIMITER = "create - user:";

    public String[] parseArgumentForCreate(String transHistories) throws Exception
    {
        try
        {
            StringTokenizer transHistoryTokens = new StringTokenizer(transHistories, "\n");
            String oid = null;
            StringList oidList = new StringList();
            while (transHistoryTokens.hasMoreTokens())
            {
                String transHistory = transHistoryTokens.nextToken().trim();
                int idIndex = transHistory.indexOf(TRANS_HISTORY_OBJECT_DELIMITER);
                if( idIndex >= 0 )
                {
                    oid = transHistory.substring(idIndex+3, transHistory.length());
                }
                int createIndex = transHistory.indexOf(TRANS_HISTORY_CREATE_EVENT_DELIMITER);
                if( createIndex >= 0 )
                {
                    oidList.addElement(oid);
                }
            }
            String[] oids = new String[oidList.size()];
            oidList.toArray(oids);
            return oids;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }


    public void relatedObjectIndex(Context context, String[] args) throws Exception
    {
        try
        {
            if( args.length < 3 )
            {
                HashMap paramMap = (HashMap)JPO.unpackArgs(args);
                Map paramList = (Map)paramMap.get("paramList");
                String languageStr = (String)paramList.get("languageStr");
                String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.TriggerBasedIndexing.InsufficentArguments", new Locale(languageStr));
                throw new Exception(exMsg);
            }
            String relatedOID = args[0];
            String jponame        = "emxTriggerBasedIndexingUtil";
            String methodName     = "relatedIndex";
            DomainObject obj = DomainObject.newInstance(context, relatedOID);
            BackgroundProcess bgp = new BackgroundProcess();
            Context clonedContext = context.getFrameContext(obj.getInfo(context, DomainObject.SELECT_NAME));
            bgp.submitJob(clonedContext, jponame, methodName, args , (String)null);

        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    static final String FIELD_NAME = "fieldName";
    static final String OBJECT_IDS = "objectIds";
    static final String NEW_VALUE = "newValue";
    static Config config = null;
    public int relatedIndex(Context context, String[] args) throws Exception
    {
        try
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            Map paramList = (Map)paramMap.get("paramList");
            String languageStr = (String)paramList.get("languageStr");
            ContextUtil.pushContext(context);
            if( args.length < 3 )
            {

                String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.TriggerBasedIndexing.InsufficentArguments", new Locale(languageStr));
                throw new Exception(exMsg);
            }
            String relatedOID = args[0];
            String fieldName = args[1];
            String selectable =  args[2];
            String idType = "Object";
            String unknowFieldNames = "";
            if( args.length > 3 )
            {
              idType = args[3];
            }
            idType = idType.toLowerCase();

            if( config == null)
            {
              config = Config.getInstance(context);
            }

            StringList fieldNames = FrameworkUtil.split(fieldName, ",");
            StringList selectables = FrameworkUtil.split(selectable, ",");
            for (int i=0; i<fieldNames.size(); i++)
            {
                fieldName = (String)fieldNames.get(i);
                selectable = "";
                if( selectables.size() >= i)
                {
                    selectable = (String)selectables.get(i);;
                }

                Config.Field field = config.indexedBOField(fieldName);
                if( field != null )
                {
                    String valueSelectable = field.selectable;
                    String newValue = "";
                    StringList oidList = null;
                    if( "object".equals(idType) )
                    {
                      DomainObject obj = DomainObject.newInstance(context, relatedOID);
                      oidList = obj.getInfoList(context, selectable);
                    } else {
                      if(!UIUtil.isNullOrEmpty(relatedOID)){
                      String cmd = "print connection $1 select $2 dump $3";
                      String result = MqlUtil.mqlCommand(context, cmd, true, relatedOID, selectable, "|");
                      oidList = FrameworkUtil.split(result, "|");
                      }
                    }
                    String oid = "";
                    if( oidList.size() > 0 )
                    {
                        oid = (String)oidList.get(0);
                        DomainObject obj = DomainObject.newInstance(context, oid);
                        newValue = obj.getInfo(context, valueSelectable);
                    }
                    String[] oids = new String[oidList.size()];
                    for(int j=0; j<oidList.size(); j++ )
                    {
                      oids[j] = (String)oidList.get(j);
                    }

                    IndexerImpl in = new IndexerImpl();
                    in.updateFieldValues(context, oids, null, fieldName, newValue);
                } else {
                    unknowFieldNames += fieldName +",";
                }
            }
            if( unknowFieldNames.indexOf(",") > 0)
            {

                String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.UnknownFieldNames", new Locale(languageStr));
                throw new Exception(exMsg + unknowFieldNames );
            }
            return 0;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        finally
        {
            ContextUtil.popContext(context);
        }
    }

  /**
   * updateLastRevisionStatus(), Trigger Method to update modified status of last revision of PART/DOCUMENTS Object
   *
   * @param context the matrix user context
   * @param args holds trigger arguments
   *      - args[0] - Business Object Type
   *          - args[1] - Business Object Name
   * @return 0 on success
   * @author Chakra
   * @since  R209
   * @throws Exception if the operation fails
   */

  public int updateLastRevisionStatus(Context context, String[] args)
    throws Exception {

    boolean isContextedPushed = false;

    /* Get Trigger Parameters */
    String type = args[0];
    String name = args[1];
    ContextUtil.pushContext(context);
    isContextedPushed = true;

    try {
      /* Find Last Revision */
      MapList objectList = DomainObject.findObjects(context,
                              type,
                              name,
                              DomainObject.QUERY_WILDCARD ,
                              DomainObject.QUERY_WILDCARD ,
                              DomainObject.QUERY_WILDCARD ,
                              "revision==last",
                              null,
                              false,
                              new StringList(DomainObject.SELECT_ID),
                              (short)0);

      if(objectList.size() > 0) {
        Map objectMap = (Map)objectList.get(0);
        String objectId = (String)objectMap.get(DomainObject.SELECT_ID);

        /* Build Executable Command To Get All Subtypes of a given type */
		String command ="modify bus $1 add history $2";
        /* Execute MQL Command */
		MqlUtil.mqlCommand(context, command, objectId, "Object Modified");

      }
    } catch(Exception e) {
      e.printStackTrace();
      // Don't do anything
    } finally {
      if(isContextedPushed) {
        ContextUtil.popContext(context);
      }
    }

    return 0;

  } //  End Of updateLastRevisionStatus

  public static int indexPerson(Context context, String[] args) throws Exception {
	  if (args.length < 1) {
		  throw new IllegalArgumentException();
	  }

	  MqlUtil.mqlCommand(context, "log bus $1 event bus custom", true, args[0]);

	  return 0;
  }
}// End Class
