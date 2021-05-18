package small;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

public class BashTest {
	@Test
	public void main() throws IOException {
		Scanner sc = new Scanner(System.in);
		String command = "bash -c \"sudo /usr/sbin/mysqld --datadir=/home/ailibs/DBInstances/MariaDB/2a92eba2-6eaa-489d-b68e-ab351f8d5967 --port=9901 --socket=/home/ailibs/DBInstances/MariaDB/2a92eba2-6eaa-489d-b68e-ab351f8d5967/mysql.sock --query-cache-type=0 --query-cache-size=0\"";
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectOutput(new File("/home/ailibs/output.txt"));
		pb.start();
	}
}
