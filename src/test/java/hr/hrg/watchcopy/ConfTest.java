package hr.hrg.watchcopy;

import static org.testng.Assert.*;

import java.io.FileReader;
import java.io.IOException;

import org.testng.annotations.Test;

@Test
public class ConfTest {

	@Test
	public void testGson() throws IOException{
		testValues(new JsonSupportGson(true));
	}

	@Test
	public void testJackson() throws IOException{
		testValues(new JsonSupportJackson(true));
	}

	@Test
	public void testYml() throws IOException{
		CopyConf conf = FolderCopy.loadConfig("src/test/resources/test-conf.yml");
		testValues(conf);
	}

	private void testValues(JsonSupport json) throws IOException{
		testValues(json.readValue(new FileReader("src/test/resources/test-conf.json"), CopyConf.class));
	}

	private void testValues(CopyConf conf) throws IOException{
		assertEquals(conf.inRoot, "./");
		assertEquals(conf.outRoot, "./");
		assertEquals(conf.burstDelay, 41);
		
		assertEquals(conf.rules.size(), 1);
		
		MatchConf rule = conf.rules.get(0);
		assertEquals(rule.in, "src/test/folder1");
		assertEquals(rule.out, "/tmp/folder1-copy");
		assertEquals(rule.recursive, true);
		
		assertEquals(rule.excludes.size(), 0);
		assertEquals(rule.includes.size(), 1);
		assertEquals(rule.includes.get(0), "**.xml");
		
		assertNotNull(conf.ssh);
		assertEquals(conf.ssh.host, "127.0.0.1");
		assertEquals(conf.ssh.port, 22);
		assertEquals(conf.ssh.username, "user");
		assertEquals(conf.ssh.password, "pass");
		assertEquals(conf.ssh.strictHost, false);
	}
	
}
