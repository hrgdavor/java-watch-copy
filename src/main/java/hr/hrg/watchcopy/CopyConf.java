package hr.hrg.watchcopy;

import java.util.ArrayList;
import java.util.List;

public class CopyConf {

	public SshConf ssh; 
	
	public String inRoot;
	public String outRoot;
	public long burstDelay = 40;
	
	public List<MatchConf> rules = new ArrayList<>();
	
}
