package repositories;

import static org.jooq.impl.DSL.extract;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.val;

import org.jooq.DatePart;
import org.jooq.Query;

public class QueryRepository {

	public static Query getTestQuery1() {
		return select(field("employees.emp_no"), 
				field("employees.first_name"), 
				field("employees.last_name"), 
				field("salaries.salary"))
		.from("employees")
		.join("salaries")
		.on(field("employees.emp_no").eq(field("salaries.emp_no")))
		.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(val(9999)));
	}
	
}
