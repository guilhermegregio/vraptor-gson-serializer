package net.gregio.testjackson.serialization;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.serialization.JSONSerialization;
import br.com.caelum.vraptor.serialization.NoRootSerialization;
import br.com.caelum.vraptor.serialization.Serializer;
import br.com.caelum.vraptor.serialization.SerializerBuilder;
import br.com.caelum.vraptor.view.ResultException;

@Component
public class JacksonSerialization implements JSONSerialization {

    private final HttpServletResponse response;
    protected final ObjectMapper mapper;
    private boolean withoutRoot;

    @SuppressWarnings("deprecation")
    public JacksonSerialization(HttpServletResponse response) {
        this.response = response;
        this.withoutRoot = false;
        mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, false);
        mapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
        mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        mapper.setDateFormat(sdf);
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
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        return this;
    }

    protected ObjectMapper getObjectMapper() {
        return mapper;
    }

    protected SerializerBuilder getSerializer() {
        try {
            return new JacksonSerializer(response.getWriter(), mapper, withoutRoot);
        } catch (IOException e) {
            throw new ResultException("Unable to serialize data", e);
        }
    }

}
