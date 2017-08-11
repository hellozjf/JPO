package framework.services.compact;

import com.matrixone.apps.framework.services.*;

import matrix.db.*;
import matrix.util.*;

import java.util.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class ui3_mxJPO implements MatrixService {

  private Compact compact;
  private framework.services.soap.ui3_mxJPO service;

  public ui3_mxJPO (Context context, String[] args) {
    compact = new Compact();
    service = new framework.services.soap.ui3_mxJPO(context, args);
  }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public String tableCommand(HttpServletRequest request, String name, HashMap requestMap) throws Exception
  {
    return compact.encode(service.tableCommand(request, name, requestMap));
  }

  public String pageTable(HttpServletRequest request, String id, String pageNumber) throws Exception
  {
    return compact.encode(service.pageTable(request, id, pageNumber));
  }

  public String sortTable(HttpServletRequest request, String id, String sortColumnName, String direction) throws Exception
  {
    return compact.encode(service.sortTable(request, id, sortColumnName, direction));
  }

  public String filterTable(HttpServletRequest request, String id, String filter) throws Exception
  {
    return compact.encode(service.filterTable(request, id, filter));
  }

/////////////////////////////////////////////////////////////////////////////////////

  public String getTreeMenu(HttpServletRequest request, HttpServlet servlet, String typeName) throws Exception
  {
    return compact.encode(service.getTreeMenu(request, servlet, typeName));
  }

  public String filterMenu(HttpServletRequest request, String menuName) throws Exception
  {
    return compact.encode(service.filterMenu(request, menuName));
  }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public String getMenu(Context context, String name) throws Exception
  {
    return compact.encode(service.getMenu(context, name));
  }

  public String getCommand(Context context, String name) throws Exception
  {
    return compact.encode(service.getCommand(context, name));
  }

  public String getTable(Context context, String name) throws Exception
  {
    return compact.encode(service.getTable(context, name));
  }

  public String getForm(Context context, String name) throws Exception
  {
    return compact.encode(service.getForm(context, name));
  }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
