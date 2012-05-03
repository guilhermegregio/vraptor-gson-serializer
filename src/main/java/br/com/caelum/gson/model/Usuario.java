package br.com.caelum.gson.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class Usuario {
	private Long id;
	private String usuario;
	private String senha;
	private Boolean ativo;
	private Collection<String> lista;
	private Usuario criador;
	
	private Usuario() {
		lista = Arrays.asList("Guil", "sdafsd");
	}

	public Usuario(Long id, String usuario, String senha, Usuario criador) {
		this();
		this.id = id;
		this.usuario = usuario;
		this.senha = senha;
		this.criador = criador;
	}

	public Long getId() {
		return id;
	}

	public String getUsuario() {
		return usuario;
	}

	public String getSenha() {
		return senha;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public Collection<String> getLista() {
		return lista;
	}

	public Usuario getCriador() {
		return criador;
	}

	@Override
	public String toString() {
		String retorno = "Usuario: ";
		ObjectMapper mapper = new ObjectMapper();
		try {
			retorno += mapper.writeValueAsString(this);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retorno;
	}

}
