package br.com.caelum.vraptor.serialization.gson;

import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import net.vidageek.mirror.dsl.Mirror;
import br.com.caelum.vraptor.interceptor.TypeNameExtractor;
import br.com.caelum.vraptor.serialization.ProxyInitializer;
import br.com.caelum.vraptor.serialization.Serializer;
import br.com.caelum.vraptor.serialization.SerializerBuilder;
import br.com.caelum.vraptor.view.ResultException;

import com.google.gson.GsonBuilder;

public class GsonSerializer implements SerializerBuilder {

	private GsonBuilder gsonBuilder;

	private final NamedTreeNode treeFields;

	private final Writer writer;

	private Class<?> rootClass;

	private Object object;

	private boolean recursive = false;

	private boolean withoutRoot = false;

	private TypeNameExtractor extractor;

	private ProxyInitializer initializer;

	public GsonSerializer(Writer writer, boolean indented, boolean withoutRoot, TypeNameExtractor extractor,
			ProxyInitializer initializer, Locale locale) {
		this.writer = writer;
		this.extractor = extractor;
		this.initializer = initializer;

		this.treeFields = new NamedTreeNode(null, null);

		String pattern = ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM, locale))
				.toLocalizedPattern();
		GsonBuilder gsonBuilder = new GsonBuilder().setDateFormat(pattern);

		gsonBuilder.registerTypeAdapter(byte[].class, new ConverterByteArray());

		if (indented) {
			gsonBuilder.setPrettyPrinting();
		}

		this.withoutRoot = withoutRoot;

		this.gsonBuilder = gsonBuilder;
	}

	@SuppressWarnings("unchecked")
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

	private static boolean isPrimitive(Class<?> type) {
		return type.isPrimitive() || type.isEnum() || Number.class.isAssignableFrom(type) || type.equals(String.class)
				|| Date.class.isAssignableFrom(type) || Calendar.class.isAssignableFrom(type)
				|| Boolean.class.equals(type) || Character.class.equals(type) || Map.class.isAssignableFrom(type)
				|| Object.class.equals(type) || (type.isArray() && type.getComponentType().equals(Byte.TYPE));
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
			if (isPrimitive(field.getType())) {
				String fieldPath = (root != null) ? root + "." + field.getName() : field.getName();
				addField(fieldPath);
			}
		}
	}

	private Class<?> getFieldType(Field f) {
		Type type = f.getGenericType();

		if (type instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType) type;

			if (isCollection(type)) {
				Type atype = ptype.getActualTypeArguments()[0];

				if (atype instanceof TypeVariable<?>) {
					return (Class<?>) ptype.getRawType();
				}

				return (Class<?>) atype;
			}
		}

		return (Class<?>) type;
	}

	private Entry<Field, Object> field(String fieldName, Class<?> clazz) {
		return field(fieldName, clazz, null);
	}

	private Entry<Field, Object> field(String fieldName, Class<?> clazz, Object value) {
		String[] path = fieldName.split("\\.");
		Field lastField = null;
		Object lastValue = value;

		for (String p : path) {
			lastField = new Mirror().on(clazz).reflect().field(p);
			if (lastField == null) {
				throw new ResultException("Field " + fieldName + " not found. Class: " + clazz);
			}
			if (value != null) {
				try {
					lastValue = new Mirror().on(lastValue).get().field(lastField.getName());
				} catch (Exception e) {
					throw new ResultException("Unable to retrieve the value of field: " + fieldName, e);
				}
			}
			clazz = getFieldType(lastField);
		}

		return new AbstractMap.SimpleEntry<Field, Object>(lastField, lastValue);
	}

	private void addField(String fieldName) {
		// Ignore null objects
		if (rootClass == null) {
			return;
		}

		if ((Collection.class.isAssignableFrom(rootClass) && !((Collection<?>) object).isEmpty())
				|| !Collection.class.isAssignableFrom(rootClass)) {
			Entry<Field, Object> fieldEntry = field(fieldName, rootClass);

			Class<?> fieldType = getFieldType(fieldEntry.getKey());
			if (!isPrimitive(fieldType)) {
				includePrimitiveFields(fieldType, fieldName);
			} else {
				treeFields.addChild(fieldName);
			}
		}
	}

	protected List<Map<String, Object>> serializeCollection(NamedTreeNode node, Collection<Object> collection) {
		List<Map<String, Object>> arrayNodes = new ArrayList<>();
		for (Object o : collection) {
			arrayNodes.add(serialize(new HashMap<String, Object>(), node, o));
		}
		return arrayNodes;
	}

	@SuppressWarnings({ "unchecked" })
	protected Map<String, Object> serialize(Map<String, Object> jsonNode, NamedTreeNode root, Object value) {
		for (NamedTreeNode node : root.getChilds()) {
			if (node.containsChilds()) {
				Entry<Field, Object> entry = field(node.getName(), value.getClass(), value);
				Object fieldValue = entry.getValue();

				if (fieldValue != null && Collection.class.isAssignableFrom(fieldValue.getClass())) {
					Collection<Object> collection = (Collection<Object>) entry.getValue();
					jsonNode.put(entry.getKey().getName(), serializeCollection(node, collection));
				} else {
					if (fieldValue != null) {
						Map<String, Object> objectNode = new HashMap<String, Object>();
						jsonNode.put(entry.getKey().getName(), objectNode);
						serialize(objectNode, node, fieldValue);
					}
				}
			} else {
				Entry<Field, Object> entry = field(node.getName(), value.getClass(), value);
				Object fieldValue = entry.getValue();

				Class<?> type = entry.getKey().getType();

				if ((isPrimitive(type) || isCollection(type)) && fieldValue != null) {
					jsonNode.put(entry.getKey().getName(), fieldValue);
				}
			}
		}
		return jsonNode;
	}

	@SuppressWarnings("unchecked")
	public void serialize() {
		Map<String, Object> rootNode = new HashMap<>();

		if (object != null) {
			if (recursive) {
				if (withoutRoot) {
					toJson(object);
				} else {
					rootNode.put(treeFields.getName(), object);
					toJson(rootNode);
				}
			} else if (Collection.class.isAssignableFrom(object.getClass()) && !isPrimitive(rootClass)) {
				Collection<Object> collection = (Collection<Object>) object;

				if (withoutRoot) {
					toJson(serializeCollection(treeFields, collection));
				} else {
					rootNode.put(treeFields.getName(), serializeCollection(treeFields, collection));
					toJson(rootNode);
				}
			} else if (!isPrimitive(rootClass)) {
				if (withoutRoot) {
					serialize(rootNode, treeFields, object);
				} else {
					Map<String, Object> temp = new HashMap<>();
					temp.put(treeFields.getName(), serialize(rootNode, treeFields, object));
					rootNode = temp;
				}
				toJson(rootNode);
			} else {
				if (withoutRoot) {
					rootNode.put("", object);
				} else {
					rootNode.put(treeFields.getName(), object);
				}
				toJson(rootNode);
			}
		} else {
			if (treeFields.getName() != null) {
				Map<String, Object> novoNode = new HashMap<String, Object>();
				rootNode.put(treeFields.getName(), novoNode);
			}

			toJson(rootNode);
		}
	}

	private void toJson(Object object) {
		try {
			writer.write(gsonBuilder.create().toJson(object));
			writer.flush();
			writer.close();
		} catch (Exception e) {
			throw new ResultException("Unable to generate JSON", e);
		}
	}

	public Serializer exclude(String... fields) {
		for (String field : fields) {
			treeFields.removeChild(field);
		}
		return this;
	}

	public Serializer include(String... fields) {
		for (String fieldName : fields) {
			addField(fieldName);
		}
		return this;
	}

	@Override
	public Serializer recursive() {
		recursive = true;
		return this;
	}

	@Override
	public <T> Serializer from(T object) {
		return from(object, null);
	}

	@Override
	public <T> Serializer from(T object, String alias) {
		this.object = object;

		if (object != null) {

			if (alias == null) {
				String name = null;

				if (initializer.isProxy(object.getClass())) {
					name = extractor.nameFor(initializer.getActualClass(object));
				} else {
					Class<?> type = getTypeOf(object);
					name = extractor.nameFor(type);

					if (isCollection(object.getClass())) {
						name = "list";
					}
				}

				treeFields.setName(name);
			} else {
				treeFields.setName(alias);
			}

			rootClass = getTypeOf(object);

			if (!isPrimitive(rootClass)) {
				includePrimitiveFields(rootClass, null);
			}

		} else {
			if (alias != null) {
				treeFields.setName(alias);
			}

			rootClass = null;
		}

		return this;
	}
}