package small;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

public class BashTest {
	@Test
	public void main() throws IOException, InterruptedException {
		//String[] command = new String[] {"bash", "-c", "\"sudo -u root /usr/sbin/mysqld --datadir=/home/ailibs/DBInstances/MariaDB/2a92eba2-6eaa-489d-b68e-ab351f8d5967 --port=9901 --socket=/home/ailibs/DBInstances/MariaDB/2a92eba2-6eaa-489d-b68e-ab351f8d5967/mysql.sock --query-cache-type=0 --query-cache-size=0 --log_bin\""};
		String[] command = new String[] {"sudo", "/usr/sbin/mysqld", "--datadir=/home/ailibs/DBInstances/MariaDB/2a92eba2-6eaa-489d-b68e-ab351f8d5967", "--port=9901", "--socket=/home/ailibs/DBInstances/MariaDB/2a92eba2-6eaa-489d-b68e-ab351f8d5967/mysql.sock", "--query-cache-type=0", "--query-cache-size=0", "--log_bin"};
		ProcessBuilder pb = new ProcessBuilder(command);
		System.out.println(String.join(" ", command));
		pb.redirectOutput(new File("/home/ailibs/output.txt"));
		pb.redirectError(new File("/home/ailibs/error.txt"));
		Process p = pb.start();
		p.waitFor();
	}
}
