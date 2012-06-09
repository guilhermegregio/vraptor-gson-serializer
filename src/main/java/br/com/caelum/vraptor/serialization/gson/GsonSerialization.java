package br.com.caelum.vraptor.serialization.gson;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import br.com.caelum.vraptor.interceptor.TypeNameExtractor;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.serialization.JSONSerialization;
import br.com.caelum.vraptor.serialization.NoRootSerialization;
import br.com.caelum.vraptor.serialization.ProxyInitializer;
import br.com.caelum.vraptor.serialization.Serializer;
import br.com.caelum.vraptor.serialization.SerializerBuilder;
import br.com.caelum.vraptor.view.ResultException;

@Component
public class GsonSerialization implements JSONSerialization {

	private final HttpServletResponse response;

	private boolean withoutRoot;

	private boolean indented;

	private TypeNameExtractor extractor;

	private ProxyInitializer initializer;

	public GsonSerialization(HttpServletResponse response, TypeNameExtractor extractor,
			ProxyInitializer initializer) {
		this.response = response;
		this.extractor = extractor;
		this.initializer = initializer;
		this.withoutRoot = false;
	}

	@Override
	public boolean accepts(String format) {
		return "json".equals(format);
	}

	@Override
	public <T> Serializer from(T object) {
		return from(object, null);
	}

	@Override
	public <T> Serializer from(T object, String alias) {
		response.setContentType("application/json");
		return getSerializer().from(object, alias);
	}

	@Override
	public <T> NoRootSerialization withoutRoot() {
		this.withoutRoot = true;
		return this;
	}

	@Override
	public JSONSerialization indented() {
		indented = true;
		return this;
	}

	protected SerializerBuilder getSerializer() {
		try {
			return new GsonSerializer(response.getWriter(), indented, withoutRoot, extractor, initializer,
					response.getLocale());
		} catch (IOException e) {
			throw new ResultException("Unable to serialize data", e);
		}
	}

}
