package hr.hrg.watchcopy;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class ScpTest {

	public static void main(String[] args) {
		try {
			String user     = "root";
			String password = "LdjiKE4SMOWaa51";
			String host = "136.243.90.99";
			int port = 22;
			JSch jsch = new JSch();

			Session session = jsch.getSession(user, host, port);
			UserInfo ui = new MyUserInfo(user, password);
			session.setUserInfo(ui);
			
			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp c = (ChannelSftp) channel;
			
			System.out.println("folder:"+c.pwd());
			
			c.quit();

			System.exit(0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	static class MyUserInfo implements UserInfo{

		private String username;
		private String password;

		public MyUserInfo(String username, String password) {
			this.username = username;
			this.password = password;
		}
		
		@Override
		public String getPassphrase() {
			return "";
		}

		@Override
		public String getPassword() {
			return password;
		}

		@Override
		public boolean promptPassword(String message) {
			System.out.println("passprompt: "+message);
			return true;
		}

		@Override
		public boolean promptPassphrase(String message) {
			System.out.println("passphrase: "+message);
			return false;
		}

		@Override
		public boolean promptYesNo(String message) {
			System.out.println("prompt: "+message);
			return true;
		}

		@Override
		public void showMessage(String message) {
			System.out.println("message: "+message);
		}
	}
}
