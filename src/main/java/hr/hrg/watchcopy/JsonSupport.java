package hr.hrg.watchcopy;

import java.io.IOException;
import java.io.Reader;

public interface JsonSupport {

	public <T> T readValue(Reader src, Class<T> valueType) throws IOException;

}
