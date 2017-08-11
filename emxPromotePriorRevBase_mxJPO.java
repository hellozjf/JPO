/* emxPromotePriorRevBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of
   MatrixOne, Inc.  Copyright notice is precautionary only and does
   not evidence any actual or intended publication of such program.

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.10 Wed Oct 22 16:02:34 2008 przemek Experimental przemek $
*/

import java.io.BufferedWriter;
import matrix.db.BusinessObject;
import matrix.db.Context;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import java.util.Locale;
import matrix.db.*;

/**
 *  The <code>${CLASSNAME}</code> class is used
 *  to generate all DR managed business types
 *  list for display rule.
 *
 * @exclude
 */

public class emxPromotePriorRevBase_mxJPO extends DomainObject
{


   static final String POLICY_CONTAINER =
     PropertyUtil.getSchemaProperty ( "policy_Container" );
   static final String POLICY_CONTAINERREV2 =
     PropertyUtil.getSchemaProperty ( "policy_ContainerRev2" );
   static final String POLICY_CONTROLLEDPRODUCTIONRELEASE =
     PropertyUtil.getSchemaProperty ( "policy_ControlledProductionRelease" );
   static final String POLICY_CONTROLLEDPRODUCTIONRELEASEREV2 =
     PropertyUtil.getSchemaProperty ("policy_ControlledProductionReleaseRev2");
   static final String POLICY_CONTROLLEDDESIGNRELEASE =
     PropertyUtil.getSchemaProperty ( "policy_ControlledDesignRelease" );
   static final String POLICY_CONTROLLEDDESIGNRELEASEREV2 =
     PropertyUtil.getSchemaProperty ("policy_ControlledDesignReleaseRev2");

   /**
    *  Constructs a new JPO object.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args holds no arguments
    *  @throws Exception if the operation fails
    *
    *  @since AEF 9.5.6.0
    */
   public emxPromotePriorRevBase_mxJPO ( Context context, String[] args )
          throws Exception
   {
     /*
      *  Author    : Mike Terry
      *  Date      : 03/13/03
      *  Notes     :
      *  History   :
      */

      if ( !context.isConnected() )
      {
         throw new Exception( "not supported on desktop client" );
      }
   }

   /**
    *  This mehtod is executed if a specific method is not specified.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args holds no arguments
    *  @return int 0
    *  @throws Exception if the operation fails
    *
    *  @since AEF 9.5.6.0
    */
   public int mxMain( Context context, String[] args )
          throws Exception
   {
     /*
      *  Author    : Mike Terry
      *  Date      : 03/13/03
      *  Notes     :
      *  History   :
      */

      if ( !context.isConnected() )
      {
         throw new Exception( "not supported on desktop client" );
      }

      return 0;
   }


   /**
    *  This is a business rule trigger that promotes the prior revision of the
    *  object to the next state, usually state_Obsolete.  Each policy has a
    *  'Published' state where the object is static (no changes).  When a new
    *  revision is promoted to that state, the prior revision (if exists) is
    *  moved to the next state.  Only one revision for a Type/Name pair will
    *  be in the 'Published' state.
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return either 0 or exception
    *  @throws Exception if the operation fails
    *
    *  @since AEF 9.5.6.0
    */
   public int promotePriorRev(Context context, String[] args)
          throws Exception
   {
     /*
      *  Author    : Mike Terry
      *  Date      : 03/13/03
      *  Notes     :
      *  History   :
      */

      // the object being promoted to the 'Published' state.
      String objectId = args[0];

      DomainObject currentRev = new DomainObject ( objectId );

      BusinessObject previousRevBo = currentRev.getPreviousRevision ( context );
      String previousRevId = previousRevBo.getObjectId();

      DomainObject previousRev = new DomainObject ( previousRevId );

      // check if there is a previous revision.
      if (! previousRevId.equals ( "" ) )
      {

        String previousRevState = previousRev.getInfo(context, SELECT_CURRENT);

        previousRev.open(context);

        String policyName = previousRev.getPolicy().getName();
        String symbolicPolicyName = FrameworkUtil.getAliasForAdmin (
                                context,
                                "Policy",
                                policyName,
                                true );

        String publishedState = getPublishedState ( context,
                                                    symbolicPolicyName );

        // To get the last state of any object

        StateList stateList = currentRev.getStates(context);
        State state = (State)stateList.elementAt(stateList.size()-1);

        String stateName = state.getName();

        // if the previous rev is 'Published', push to next state.
        if ( previousRevState.equals ( publishedState ) )
        {

            ContextUtil.pushContext(context);

            previousRev.promote (context);

            ContextUtil.popContext(context);

        }
        else // Do not display error message in case previous revision is already in last state
        {
            if (!previousRevState.equalsIgnoreCase (stateName))
            {             
                String errorString = EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(context.getSession().getLanguage()),"emxDocumentCentral.ErrorMsg.PreviousRevNotReleased");
                throw (new FrameworkException( errorString ) );
            }
        }

        previousRev.close(context);

      }

      return 0;
   }

   /**
    *  Returns the physical state name of the 'Published' state given
    *  the policy.  Each policy has a 'Published' state that implies
    *  that object shouldn't be changed anymore.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param symbolicPolicyName the Java <code>String<code> object
    * @return the physical state name.
    * @throws Exception if the operation fails
    * @since AEF 9.5.6.0
    */
   protected String getPublishedState (Context context,
                                       String symbolicPolicyName)
     throws Exception
   {
     /*
      *  Author     : Mike Terry
      *  Date       : 03/13/03
      *  Notes      :
      *  History    :
      *
     */

     //*** return the 'published' state name for the policy.
     if ( symbolicPolicyName.equals ( "policy_Container" ) )
     {
       return PropertyUtil.getSchemaProperty (context,
                                              "policy",
                                              POLICY_CONTAINER,
                                              "state_RELEASED" );
     }
     else if ( symbolicPolicyName.equals ( "policy_ContainerRev2" ) )
     {
       return PropertyUtil.getSchemaProperty (context,
                                              "policy",
                                              POLICY_CONTAINERREV2,
                                              "state_Locked" );
     }
     else if ( symbolicPolicyName.equals ( "policy_ControlledProductionRelease" ) )
     {
       return PropertyUtil.getSchemaProperty (context,
                                              "policy",
                                              POLICY_CONTROLLEDPRODUCTIONRELEASE,
                                              "state_RELEASED" );
     }
     else if ( symbolicPolicyName.equals ( "policy_ControlledDesignRelease" ) )
     {
       return PropertyUtil.getSchemaProperty (context,
                                              "policy",
                                              POLICY_CONTROLLEDDESIGNRELEASE,
                                              "state_RELEASED" );
     }

     else if ( symbolicPolicyName.equals ( "policy_ControlledProductionReleaseRev2" ) )
     {
       return PropertyUtil.getSchemaProperty (context,
                                        "policy",
                                        POLICY_CONTROLLEDPRODUCTIONRELEASEREV2,
                                        "state_Released" );
     }
     else if ( symbolicPolicyName.equals ( "policy_ControlledDesignReleaseRev2" )  )
     {
       return PropertyUtil.getSchemaProperty (context,
                                              "policy",
                                              POLICY_CONTROLLEDDESIGNRELEASEREV2,
                                              "state_Released" );
     }

     else
     {
       Locale loc = context.getLocale();
       String languageKey = loc.getLanguage();
       
       String errorString = EnoviaResourceBundle.getProperty(context,"emxDocumentCentralStringResource",new Locale(languageKey),"emxDocumentCentral.ErrorMsg.PolicyNotKnown");

       throw (new FrameworkException( errorString ) );
     }


   }

}
