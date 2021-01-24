package main;

import com.healthmarketscience.sqlbuilder.InsertQuery;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbJoin;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;

import helpers.TestDescription;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	    // create default schema
	    DbSpec spec = new DbSpec();
	    DbSchema schema = spec.addDefaultSchema();
	 
	    // add table with basic customer info
	    DbTable customerTable = schema.addTable("customer");
	    DbColumn custIdCol = customerTable.addColumn("cust_id", "number", null);
	    DbColumn custNameCol = customerTable.addColumn("name", "varchar", 255);
	 
	    // add order table with basic order info
	    DbTable orderTable = schema.addTable("order");
	    DbColumn orderIdCol = orderTable.addColumn("order_id", "number", null);
	    DbColumn orderCustIdCol = orderTable.addColumn("cust_id", "number", null);
	    DbColumn orderTotalCol = orderTable.addColumn("total", "number", null);
	    DbColumn orderDateCol = orderTable.addColumn("order_date", "timestamp", null);
	 
	    // add a join from the customer table to the order table (on cust_id)
	    DbJoin custOrderJoin = spec.addJoin(null, "customer",
	                                        null, "order",
	                                        "cust_id");
		SelectQuery selectQuery = new SelectQuery()
	      .addColumns(custNameCol)
	      .validate();
		
		InsertQuery insertQuery = new InsertQuery(custOrderJoin);
		
		
		TestDescription td = new TestDescription(null);
		td.addQuery(1, selectQuery);
		td.addQuery(-1, insertQuery);
		td.addQuery(-1, selectQuery);
		td.addQuery(-2, insertQuery);
		td.addQuery(2, selectQuery);
		td.addQuery(4, insertQuery);
		td.addQuery(8, selectQuery);
		td.addQuery(15, insertQuery);
		td.addQuery(-21, selectQuery);
		td.addQuery(-31, insertQuery);
		td.addQuery(13, selectQuery);
		td.addQuery(-21, insertQuery);
		td.addQuery(13, selectQuery);
		td.addQuery(-18, insertQuery);
		td.addQuery(4, selectQuery);
		td.addQuery(-1, insertQuery);
		td.addQuery(1, selectQuery);
		td.addQuery(-1, insertQuery);
		td.print();
		
		for(int priority: td.queries.keySet()) {
			
		}
	}

}
