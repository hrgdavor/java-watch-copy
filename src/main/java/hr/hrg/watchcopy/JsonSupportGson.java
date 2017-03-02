package hr.hrg.watchcopy;

import java.io.IOException;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonSupportGson implements JsonSupport{
	Logger log = LoggerFactory.getLogger(JsonSupportGson.class);
	
	private Gson gson;

	public JsonSupportGson(boolean lenient) {
		GsonBuilder b = new GsonBuilder();
		b.setLenient();
		gson = b.create();
	}
	
	@Override
    public <T> T readValue(Reader src, Class<T> valueType) throws IOException{
    	return gson.fromJson(src, valueType);
	}	

}
