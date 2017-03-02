package hr.hrg.watchcopy;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;

import com.pastdev.jsch.DefaultSessionFactory;
import com.pastdev.jsch.IOUtils;

public class ScpTest2 {

	public static void main(String[] args) throws Exception{
		try {
			String username     = "root";
			String password = "LdjiKE4SMOWaa51";
			String host = "136.243.90.99";
			int port = 22;
			Path path1 = Paths.get("/xx");
			BasicFileAttributes attributes2 = path1.getFileSystem().provider().readAttributes(path1, BasicFileAttributes.class);

			DefaultSessionFactory defaultSessionFactory = new DefaultSessionFactory( username, host, port);
		    defaultSessionFactory.setPassword( password );
		    defaultSessionFactory.setConfig("StrictHostKeyChecking", "no");
		    
		    Map<String, Object> environment = new HashMap<String, Object>();
		    environment.put( "defaultSessionFactory", defaultSessionFactory );
		    URI uri = new URI( "ssh.unix://" + username + "@" + host + ":" + port + "/" );
		    try (FileSystem sshfs = FileSystems.newFileSystem( uri, environment )) {
		        Path path = sshfs.getPath( "/opt/ghosttest.sc" ); // refers to /home/joe/afile
		        FileSystemProvider provider = path.getFileSystem().provider();
				BasicFileAttributes attributes = provider.readAttributes(path, BasicFileAttributes.class);
		        System.out.println("LastMod: "+attributes.lastModifiedTime());
		        System.out.println("Size:    "+attributes.size());
		        System.out.println("exists:  "+Files.exists(path));
		        
		        
		        try (InputStream inputStream = provider.newInputStream( path )) {
		            String fileContents = IOUtils.copyToString( inputStream );
//		            System.out.println(fileContents);
		        }
		    }		    
			System.exit(0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
