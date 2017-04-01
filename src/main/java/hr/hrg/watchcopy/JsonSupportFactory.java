package hr.hrg.watchcopy;

import hr.hrg.javawatcher.WatchUtil;

public class JsonSupportFactory {

	public JsonSupport create(boolean lenient){
		if(gsonAvailable())
			return new JsonSupportGson(lenient);
		else if(jacksonAvailable())
			return new JsonSupportJackson(lenient);
		else
			throw new RuntimeException("Can not parse JSON because neither GSON or jackson-databind are available in classpath");		
	}

	public static final boolean gsonAvailable() {
		return WatchUtil.classAvailable("com.google.gson.Gson");
	}

	public static final boolean jacksonAvailable() {
		return WatchUtil.classAvailable("com.fasterxml.jackson.databind.ObjectMapper");
	}

}
