package br.com.caelum.gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import br.com.caelum.gson.model.Usuario;

public class SampleJackson {

	static Usuario usuario;
	static ObjectMapper mapper = new ObjectMapper();

	private static void toJsonEasy() {
		usuario = new Usuario(null, "guilherme.gregio", "123456", null);

		try {
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
			System.out.println(mapper.writeValueAsString(usuario));
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void toJsonListEasy() {

		usuario = new Usuario(1L, "guilherme.gregio", "123456", null);

		List<Usuario> usuarios = new ArrayList<>();
		usuarios.add(usuario);
		// usuarios.add(usuario);

		try {
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
			System.out.println(mapper.writeValueAsString(usuarios));
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void toJsonMap() {

		Set<String> t = new HashSet<>();
		t.addAll(Arrays.asList("a"));

		Map<String, Object> rootNode = new HashMap<String, Object>();

		Map<String, Object> enderecoNode = new HashMap<String, Object>();
		enderecoNode.put("logradouro", "rua 1");
		enderecoNode.put("bairro", "pq");

		rootNode.put("id", 1);
		rootNode.put("nomes", Arrays.asList("Guilherme", "Gregio"));
		rootNode.put("endereco", enderecoNode);
		rootNode.put("endereco1", null);
		rootNode.put("set", t);

		try {
			// mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT,
			// true);
			mapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, true);
			System.out.println(mapper.writeValueAsString(rootNode));
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void deserializeEasy() {
		String json = "{\"id\" : 1,\"usuario\" : \"guilherme.gregio\", \"senha\":\"123\", \"lista\":[\"Teste\",\"Guilherme\"]}";
		
		try {
			Usuario u = mapper.readValue(json, Usuario.class);
			
			System.out.println(u.toString());
			
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		toJsonEasy();
		toJsonListEasy();
		toJsonMap();
		deserializeEasy();
	}

}
