package jpo.plmprovider;
// ${CLASSNAME}.java
//
// Created on 12-19-2006
//
// Copyright (c) 2006-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import com.matrixone.apps.plmprovider.AttributeDefinition;
import com.matrixone.apps.plmprovider.MetaInfo;
import com.matrixone.apps.plmprovider.NodeType;

import matrix.util.MatrixWrappedService;

/**
 * @author mkeirstead
 *
 * The <code>${CLASSNAME}</code> class provides web services associated with meta data.
 *
 * @version AEF 10.7.1.0 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class MetaData_mxJPO extends jpo.plmprovider.MetaDataBase_mxJPO implements MatrixWrappedService
{
    /**
     * Constructor.
     *
     * @since AEF 10.7.1.0
     */

    public MetaData_mxJPO()
    {
    }

    /**
     * Get the meta info modeled on this server.  This is the web service
     * entry point.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param language the language for translations
     * @return the meta info
     * @since AEF 10.7.1.0
     */
   public MetaInfo getMetaInfo(String username, String password, String language)
   {
       return (super.getMetaInfo(username, password, language));
   }

   // The public unused* methods exist merely to expose the return type to ServiceGenerator,
   // for proper registration of the Axis serializer.
    /**
     * This method exists merely to expose the return type to ServiceGenerator,
     * for proper registration of the Axis serializer.
     *
     * @return NodeType object
     * @since AEF 10.7.1.0
     */
public NodeType unusedNodeType()
   {
       return null;
   }

   // The public unused* methods exist merely to expose the return type to ServiceGenerator,
   // for proper registration of the Axis serializer.
    /**
     * This method exists merely to expose the return type to ServiceGenerator,
     * for proper registration of the Axis serializer.
     *
     * @return RelationType object
     * @since AEF 10.7.1.0
     */
   public com.matrixone.apps.plmprovider.RelationType unusedRelationType()
   {
       return null;
   }

   // The public unused* methods exist merely to expose the return type to ServiceGenerator,
   // for proper registration of the Axis serializer.
    /**
     * This method exists merely to expose the return type to ServiceGenerator,
     * for proper registration of the Axis serializer.
     *
     * @return AttributeDefinition object
     * @since AEF 10.7.1.0
     */
   public AttributeDefinition unusedAttributeDefinition()
   {
       return null;
   }
}
