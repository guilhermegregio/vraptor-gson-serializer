package br.com.caelum.gson.serialization.gson;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.serialization.JSONSerialization;
import br.com.caelum.vraptor.serialization.NoRootSerialization;
import br.com.caelum.vraptor.serialization.Serializer;
import br.com.caelum.vraptor.serialization.SerializerBuilder;
import br.com.caelum.vraptor.view.ResultException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class GsonSerialization implements JSONSerialization {

    private final HttpServletResponse response;
    protected final Gson mapper;
    private boolean withoutRoot;

    public GsonSerialization(HttpServletResponse response) {
        this.response = response;
        this.withoutRoot = false;
        mapper = new GsonBuilder().create();
        
        /*
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        mapper.setDateFormat(sdf);
        */
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
        //mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        return this;
    }

    protected Gson getObjectMapper() {
        return mapper;
    }

    protected SerializerBuilder getSerializer() {
        try {
            return new GsonSerializer(response.getWriter(), mapper, withoutRoot);
        } catch (IOException e) {
            throw new ResultException("Unable to serialize data", e);
        }
    }

}
