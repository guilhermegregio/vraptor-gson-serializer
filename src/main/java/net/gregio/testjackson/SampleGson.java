package net.gregio.testjackson;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.gregio.testjackson.model.Usuario;
import net.gregio.testjackson.serialization.NamedTreeNode;
import net.vidageek.mirror.dsl.Mirror;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SampleGson {

	private Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
	private NamedTreeNode treeNode;
	
	private void toJsonEasy() {
		Usuario usuario = new Usuario(null, "guilherme.gregio", "123456", null);

		System.out.println(gson.toJson(usuario));
	}
	
	private static Class<?> getTypeOf(Object obj) {
        if (Collection.class.isAssignableFrom(obj.getClass())) {
            Collection<Object> c = (Collection<Object>) obj;
            for (Object element : c) {
                if (element != null) {
                    return element.getClass();
                }
            }
        }

        return obj.getClass();
    }
	
	private static String getFieldName(Class<?> type) {
        String fieldName = type.getSimpleName();
        if (fieldName == null || "".equals(fieldName)) {
            return null;
        }
        fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
        if (isCollection(type)) {
            fieldName += "s";
        }
        return fieldName;
    }
	
	private static boolean isCollection(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) type;
            return Collection.class.isAssignableFrom((Class<?>) ptype.getRawType())
                    || Map.class.isAssignableFrom((Class<?>) ptype.getRawType());
        }
        return Collection.class.isAssignableFrom((Class<?>) type);
    }
	
	protected void includePrimitiveFields(Class<?> clazz, String root) {
        for (Field field : new Mirror().on(clazz).reflectAll().fields()) {
            //if (isNonPojo(field.getType())) {
                String fieldPath = (root != null) ? root + "." + field.getName() : field.getName();
                System.out.println(fieldPath);
                //addField(fieldPath);
            //}
        }
    }
	
	private void toJsonMap() {
		treeNode = new NamedTreeNode(null, null);
		
		List<Usuario> listUsuarios = new ArrayList<>();
		Usuario criador = new Usuario(1L, "admin", "098765", null);
		Usuario usuario = new Usuario(2L, "guilherme.gregio", "123456", criador);
		
		listUsuarios.add(usuario);
		
		Class<?> type = getTypeOf(listUsuarios);
        String name = getFieldName(type);
        if (isCollection(listUsuarios.getClass())) {
            name = name + "List";
        }
        
		treeNode.setName(name);
		
		includePrimitiveFields(type, null);
		
		
		System.out.println(treeNode.getName());
		
		//System.out.println(gson.toJson(listUsuarios));

		/*
		String root = "users";
		Boolean withoutRoot = true;

		

		Map<String, Object> rootNode = new HashMap<String, Object>();
		Map<String, Object> json = new HashMap<String, Object>();
		
		for (Field f : usuario.getClass().getDeclaredFields()) {
			// Get all fields DECLARED inside the target object class
			json.put(f.getName(), new Mirror().on(usuario).get().field(f.getName()));

			System.out.println(f.getName());
			System.out.println(f.getType());
			System.out.println(new Mirror().on(usuario).get().field(f.getName()));
		}
		
		if (withoutRoot) {
			rootNode = json;
		} else {
			rootNode.put(root, json);
		}

		System.out.println(gson.toJson(rootNode));
		*/
	}

	private void deserializeEasy() {
		String json = "{\"x\":\"x\",\"id\":\"1\",\"usuario\":\"guilherme.gregio\",\"senha\":\"123456\",\"lista\":[\"Guil\",\"sdafsd\"], \"ttt\":[\"123\",\"asd\"], \"obj\":{\"abc\":\"123\"},\"nossa\":[{\"qwe\":\"poi\"},{\"mnb\":\"zxc\"},\"1\"]}";

		Usuario u = gson.fromJson(json, Usuario.class);

		System.out.println(u.toString());
	}

	public static void main(String[] args) {
		SampleGson sampleGson = new SampleGson();
		sampleGson.toJsonMap();
		// toJsonEasy();
		// deserializeEasy();
	}

}
