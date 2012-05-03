package net.gregio.testjackson.serialization.gson;

import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.gregio.testjackson.serialization.NamedTreeNode;
import net.vidageek.mirror.dsl.Mirror;
import br.com.caelum.vraptor.serialization.Serializer;
import br.com.caelum.vraptor.serialization.SerializerBuilder;
import br.com.caelum.vraptor.view.ResultException;

import com.google.gson.Gson;

public class Gson2Serializer implements SerializerBuilder {

    private final Gson mapper;
    private final NamedTreeNode treeFields;
    private final Writer writer;
    private Class<?> rootClass;
    private Object object;
    private boolean recursive = false;
    private boolean withoutRoot = false;
    
    public Gson2Serializer(Writer writer, Gson mapper) {
        this.writer = writer;
        this.treeFields = new NamedTreeNode(null, null);
        this.mapper = mapper;
    }

    public Gson2Serializer(Writer writer, Gson mapper, boolean withoutRoot) {
        this(writer, mapper);
        this.withoutRoot = withoutRoot;
    }

	@Override
	public Serializer exclude(String... names) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializer include(String... names) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializer recursive() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
    public void serialize() {
    	Map<String, Object> rootNode = new HashMap<String, Object>();
    	
        if (object != null) {
            if (recursive) {
                if (withoutRoot) {
                    rootNode.put(null, object);
                } else {
                    rootNode.put(treeFields.getName(), object);
                }
            } else if (Collection.class.isAssignableFrom(object.getClass()) && !isNonPojo(rootClass)) {
            	Map<String, Object> arrayNode = new HashMap<String, Object>();
            	
            	
                Collection<Object> collection = (Collection<Object>) object;

                
                rootNode.put(treeFields.getName(), serializeCollection(arrayNode, treeFields, collection));
            } else if (!isNonPojo(rootClass)) {
                //Map<String, Object> dataRoot = (Map<String, Object>) ((withoutRoot) ? rootNode : rootNode.put("", treeFields.getName()));
                
                serialize(rootNode, treeFields, object);
            } else {
                if (withoutRoot) {
                    rootNode.put("", object);
                } else {
                    rootNode.put(treeFields.getName(), object);
                }
            }
        } else {
            if (treeFields.getName() != null) {
            	Map<String, Object> novoNode = new HashMap<String, Object>();
                rootNode.put(treeFields.getName(), novoNode);
            }
        }

        try {
        	writer.write(mapper.toJson(rootNode));
        	writer.flush();
        	writer.close();
        } catch (Exception e) {
            throw new ResultException("Unable to generate JSON", e);
        }
    }
	
	protected List<Map<String, Object>> serializeCollection(Map<String, Object> arrayNode, NamedTreeNode node, Collection<Object> collection) {
		List<Map<String, Object>> arrayNodes = new ArrayList<>();
        for (Object o : collection) {
            arrayNodes.add(serialize(arrayNode, node, o));
        }
        return arrayNodes;
    }
	
	@SuppressWarnings({ "unchecked" })
    protected Map<String, Object> serialize(Map<String, Object> jsonNode, NamedTreeNode root, Object value) {
        boolean allowNull = true;//mapper.getSerializationConfig().getSerializationInclusion() != JsonSerialize.Inclusion.NON_NULL;
        for (NamedTreeNode node : root.getChilds()) {
            if (node.containsChilds()) {
                Entry<Field, Object> entry = field(node.getName(), value.getClass(), value);
                Object fieldValue = entry.getValue();

                if (fieldValue != null && Collection.class.isAssignableFrom(fieldValue.getClass())) {
                	
                	Map<String, Object> arrayNode = new HashMap<String, Object>();
                	
                	
                    Collection<Object> collection = (Collection<Object>) entry.getValue();

                    jsonNode.put(entry.getKey().getName(), serializeCollection(arrayNode, node, collection));
                } else {
                    if (fieldValue != null || (fieldValue == null && allowNull)) {
                        Map<String, Object> objectNode = new HashMap<String, Object>();
                        jsonNode.put(entry.getKey().getName(), objectNode);
                        serialize(objectNode, node, fieldValue);
                    }
                }
            } else {
                Entry<Field, Object> entry = field(node.getName(), value.getClass(), value);
                Object fieldValue = entry.getValue();

                if (fieldValue != null || (fieldValue == null && allowNull)) {
                    jsonNode.put(entry.getKey().getName(), fieldValue);
                }
            }
        }
        return jsonNode;
    }

	@Override
	public <T> Serializer from(T object) {
		return from(object, null);
	}

	@Override
	public <T> Serializer from(T object, String alias) {
		this.object = object;

        if (alias == null && object != null) {
            Class<?> type = getTypeOf(object);
            String name = getFieldName(type);
            if (isCollection(object.getClass())) {
                name = name + "List";
            }
            treeFields.setName(name);
        } else {
            treeFields.setName(alias);
        }

        if (object != null) {
            rootClass = getTypeOf(object);
            
            if (!isNonPojo(rootClass)) {
                includePrimitiveFields(rootClass, null);
            }
        } else {
            rootClass = null;
        }

        return this;
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
	
	private static boolean isNonPojo(Class<?> type) {
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
            if (isNonPojo(field.getType())) {
                String fieldPath = (root != null) ? root + "." + field.getName() : field.getName();
                addField(fieldPath);
            }
        }
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
                    lastValue = new Mirror().on(lastValue).invoke().getterFor(lastField.getName());
                } catch (Exception e) {
                    throw new ResultException("Unable to retrieve the value of field: " + fieldName, e);
                }
            }
            clazz = getFieldType(lastField);
        }

        return new AbstractMap.SimpleEntry<Field, Object>(lastField, lastValue);
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
    
    private void addField(String fieldName) {
        //Ignore null objects
        if (rootClass == null) {
            return;
        }
        
        // check field
        Entry<Field, Object> fieldEntry = field(fieldName, rootClass);

        Class<?> fieldType = getFieldType(fieldEntry.getKey());
        if (!isNonPojo(fieldType)) {
            includePrimitiveFields(fieldType, fieldName);
        } else {
            treeFields.addChild(fieldName);
        }
    }
}
