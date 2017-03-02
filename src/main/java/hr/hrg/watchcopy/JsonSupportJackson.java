package hr.hrg.watchcopy;

import java.io.IOException;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSupportJackson implements JsonSupport{
	Logger log = LoggerFactory.getLogger(JsonSupportJackson.class);
	
	private ObjectMapper mapper;

	public JsonSupportJackson(boolean lenient) {
		mapper = new ObjectMapper();
		if(lenient){
			mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
			mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
			mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
			mapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
			mapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);			
		}
	}
	
    public <T> T readValue(Reader src, Class<T> valueType) throws IOException{
    	return mapper.readValue(src, valueType);
	}	
}
