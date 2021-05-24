package repositories;

import static org.jooq.impl.DSL.currentDate;
import static org.jooq.impl.DSL.extract;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.insertInto;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.update;
import static org.jooq.impl.DSL.val;

import java.util.Arrays;
import java.util.List;

import org.jooq.DatePart;
import org.jooq.Field;
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
	
	public static Query getInsertQuery() {
		return insertInto(table("employees"), field("employees.emp_no"), field("employees.birth_date"), field("employees.first_name"), field("employees.last_name"), field("employees.gender"), field("employees.hire_date"))
				.select(select(field("employees.emp_no").add(1), field("employees.birth_date"), field("employees.first_name"), field("employees.last_name"), field("employees.gender"), field("employees.hire_date")).from("employees")
						.leftJoin(select(max(field("employees.emp_no")).as("maximia")).from("employees").asTable())
						.on(field("employees.emp_no").eq(field("maximia"))));
	}
	
	public static Query getUpdateQuery() {
		/*UpdateQuery updateSalaries = new UpdateQuery(salariesTable)
		.addSetClause(salaries_salaryCol, new CustomSql(String.format("%s + (%s*20/100)", salaries_salaryCol, salaries_salaryCol)))
		.addCondition(BinaryCondition.equalTo(titles_titleCol, "Staff"))
		.addCommonTableExpression("hello").validate();
		System.out.println("UpdateQuery: " + updateSalaries);*/
		/*return update(table("employees")).set(field("employees.to_date"),currentDate())
					.where(extract(field("salaries.to_date"),DatePart.YEAR).eq(9999));*/
		return update(table("salaries"))
					.set(field("salary"), field("salary").cast(Double.class).mul(1.2));
					
	}
	
	public static List<Query> getAllQueries() {
		return Arrays.asList(new Query[] {
			getTestQuery1(), getInsertQuery(), getUpdateQuery()
		});
	}
	
}
