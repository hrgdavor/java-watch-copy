package hr.hrg.watchcopy;

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
		return classAvailable("com.google.gson.Gson");
	}

	public static final boolean jacksonAvailable() {
		return classAvailable("com.fasterxml.jackson.databind.ObjectMapper");
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

}
