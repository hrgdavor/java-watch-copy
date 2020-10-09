package hr.hrg.watchcopy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.pastdev.jsch.DefaultSessionFactory;

import hr.hrg.javawatcher.FileChangeEntry;
import hr.hrg.javawatcher.FileMatchGlob;
import hr.hrg.javawatcher.FileMatcher;
import hr.hrg.javawatcher.FolderWatcher;
import hr.hrg.javawatcher.FolderWatcherOld;
import hr.hrg.javawatcher.IFolderWatcher;
import hr.hrg.javawatcher.Main;

/**
 * 
<pre>
#
in-root ./
out-root d:/tmp/test

# first
in ./
out ./

</pre>
 * 
 * */

public class FolderCopy {
	
	Logger log = LoggerFactory.getLogger(FolderCopy.class);
	
	public static void main(String[] args) throws Exception {

		if(args.length == 0){
			printHelp();
			return;
		}

		String arg = null;
		String arg2 = null;

		String outRootStr = null;
		String inRootStr = null;
		String confFileStr = null;
		
		for(int i=0; i<args.length; i++){
			arg = args[i];
			arg2 = args.length > i+1 ? args[i+1]:"";//to avoid ArrayIndexOutOfBounds
		
			if("-o".equals(arg) || "--output".equals(arg)){
				outRootStr = arg2; i++;
				
			}else if("-i".equals(arg) || "--input".equals(arg)){
				inRootStr = arg2; i++;
			
			}else if("-h".equals(arg) || "--help".equals(arg)){
				printHelp();
				return;

			}else{
				confFileStr = arg;
			}
		}
		
		if(confFileStr == null){
			System.out.println("Please specify a conf file");
			printHelp();
			return;
		}
		
		
		File confFile = new File(confFileStr);
		Path confParent = confFile.getAbsoluteFile().toPath().getParent();
		
		Path inRoot  = inRootStr  == null ? confParent : Paths.get(inRootStr);
		Path outRoot = null;
		
		CopyConf conf = loadConfig(confFileStr);

		if(conf.inRoot != null){
			if(inRootStr != null){
				System.out.println("Ignoring configured in-root "+conf.outRoot+" in favor of command line parameter "+inRootStr);				
			}else{
				inRoot = confParent.resolve(conf.inRoot);
			}
		}

		FileSystem sshfs = null;

		if(conf.ssh != null){
			DefaultSessionFactory defaultSessionFactory = new DefaultSessionFactory( conf.ssh.username, conf.ssh.host, conf.ssh.port);
		    defaultSessionFactory.setPassword( conf.ssh.password );
		    defaultSessionFactory.setConfig("StrictHostKeyChecking", "no");
		    
		    Map<String, Object> environment = new HashMap<String, Object>();
		    environment.put( "defaultSessionFactory", defaultSessionFactory );
		    URI uri = new URI( "ssh.unix://" + conf.ssh.username + "@" + conf.ssh.host + ":" + conf.ssh.port + "/" );
			sshfs = FileSystems.newFileSystem( uri, environment );
		}

		if(conf.outRoot != null){
			if(outRootStr != null){
				if(sshfs != null)
					outRoot = sshfs.getPath(outRootStr);
				else
					outRoot = Paths.get(outRootStr);					
				System.out.println("Ignoring configured out-root "+conf.outRoot+" in favor of command line parameter "+outRootStr);
			}else{
				if(sshfs != null)
					outRoot = sshfs.getPath(conf.outRoot);
				else
					outRoot = confParent.resolve(conf.outRoot);
			}
		}

		if(outRoot == null){
			System.out.println("Please specify output folder in command line or in config");
			printHelp();
			return;
		}

		
		if(!Files.exists(outRoot)){
			System.out.println("Output root does not exist "+outRootStr);
			printHelp();
			return;			
		}
		
		if(!Files.isDirectory(outRoot)){
			System.out.println("Output path is not a folder "+outRootStr);
			printHelp();
			return;			
		}
		
//		match.setCollectMatched(true);
//		watcher.add(match);

		FolderCopy folderCopy = new FolderCopy();
		
		List<MyFileMatch> tasks = new ArrayList<>();
		for(MatchConf mc: conf.rules){
			MyFileMatch match = new MyFileMatch(inRoot.resolve(mc.in), mc.recursive);
			match.getContext().setOut(outRoot.resolve(mc.out));
			match.excludes(mc.excludes);
			match.includes(mc.includes);
			tasks.add(match);
		}

		folderCopy.start(outRoot, tasks, conf.burstDelay);		
		
	}

	private void start(Path outRoot, List<MyFileMatch> tasks, long burstDelay) throws IOException {
		IFolderWatcher<MyContext> watcher = Main.makeWatcher();
		for(MyFileMatch m:tasks){
			watcher.add(m);
		}
		watcher.init(true);
		
		for(FileMatcher<MyContext> loopMatch:watcher.getMatchers()){
			Path root = loopMatch.getRootPath();
			for(Path path: loopMatch.getMatched()){
				copyFile(path, loopMatch.getContext().getOut().resolve(root.relativize(path)));
			}
		}


		Collection<FileChangeEntry<MyContext>> changedFiles = null;

		while(!Thread.interrupted()){			
			
			changedFiles = watcher.takeBatch(burstDelay);
			if(changedFiles == null ) break; // interrupted
			
			// make sure we have unique values
			HashSet<FileChangeEntry<MyContext>> todo = new HashSet<>(changedFiles);
			
			for(FileChangeEntry<MyContext> e: todo){
				copyFile(e.getPath(), e.getMatcher().getContext().getOut().resolve(e.getMatcher().getRootPath().relativize(e.getPath())));
			}
		}		
		
	}

	private void copyFile(Path fromPath, Path toPath) throws IOException {
//		toPath.getFileSystem()
		Path dir = toPath.getParent();
		if(!Files.exists(dir)){
			Files.createDirectories(dir);
		}
		long lastModifiedTime = Files.getLastModifiedTime(fromPath).toMillis();
		long lastModifiedTime2 = 0;
		try {
			lastModifiedTime2 = Files.getLastModifiedTime(toPath).toMillis();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(lastModifiedTime > lastModifiedTime2){
			System.out.println("copy "+fromPath.normalize()+" -> "+toPath.normalize());
			Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);			
		}
	}

	public static final boolean classAvailable(String name){
		try {
			Class.forName(name);
			return true;
		} catch (ClassNotFoundException e) {
			// ignore
		}
		return false;
	}
	
	public static CopyConf loadConfig(String confFileStr) throws IOException {
		if(confFileStr.endsWith("json")){
			JsonSupportFactory factory = new JsonSupportFactory();
			JsonSupport json = factory.create(true);
			return json.readValue(new FileReader(confFileStr), CopyConf.class);
		}else{
			Yaml yaml = new Yaml();
			InputStream input = new FileInputStream(new File(confFileStr));
			return yaml.loadAs(input, CopyConf.class);			
		}
	}

	static void printHelp(){
		System.out.println("    -i, --input            Override input root defined in config");
		System.out.println("    -o, --output           Override output root defined in config");
		System.out.println("    -h, --help             Print usage info");
		System.out.println("");
	}
	
	static class MyFileMatch extends FileMatchGlob<MyContext>{
		public MyFileMatch(Path root, boolean recursive) {
			super(root, new MyContext(), recursive);
		}		
	}

	static class MyContext{
		
		Path out;
		boolean overwrite = true;
		
		public void setOut(Path out) {
			this.out = out;
		}
		
		public Path getOut() {
			return out;
		}
		
		public void setOverwrite(boolean overwrite) {
			this.overwrite = overwrite;
		}
		
		public boolean isOverwrite() {
			return overwrite;
		}
		
	}	
}
