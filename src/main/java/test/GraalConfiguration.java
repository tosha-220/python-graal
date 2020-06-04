package test;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraalConfiguration {

  @Bean
  public Engine engine() {
    return Engine.create();
  }

  @Bean
  public HostAccess hostAccess() {
    return HostAccess.newBuilder(HostAccess.ALL)
        .targetTypeMapping(Value.class, Object.class,
            value -> value.getMetaObject().toString().equals(ScriptConstants.DICT_CLASS_NAME),
            this::fromDictToMap)
        .targetTypeMapping(Value.class, List.class,
            value -> value.getMetaObject().toString().equals(ScriptConstants.LIST_CLASS_NAME),
            this::fromPythonListToList)
        .targetTypeMapping(Value.class, Set.class,
            value -> value.getMetaObject().toString().equals(ScriptConstants.LIST_CLASS_NAME),
            value -> new HashSet(fromPythonListToList(value)))
        .targetTypeMapping(Value.class, Object.class,
            Value::hasArrayElements,
            this::fromJsArrayToList)
        .targetTypeMapping(Value.class, List.class,
            Value::hasArrayElements,
            this::fromJsArrayToList)
        .targetTypeMapping(Value.class, Set.class,
            Value::hasArrayElements,
            value -> new HashSet(fromJsArrayToList(value)))
        .targetTypeMapping(Value.class, Object.class, Value::hasMembers,
            this::toJavaMap)
        .targetTypeMapping(Value.class, Map.class, null, this::fromGuestObjectToJavaMap)
        .build();
  }

  private Map fromGuestObjectToJavaMap(Value value) {
    String languageType = Context.getCurrent().getPolyglotBindings()
        .getMember(ScriptConstants.LANGUAGE_GLOBAL).asString();
    if ("js".equals(languageType)) {
      return fromJsObjectToMap(value);
    } else {
      return fromDictToMap(value);
    }
  }

  @SuppressWarnings("unchecked")
  private Map fromDictToMap(Value value) {
    if (value.isNull()) {
      return null;
    }
    Map map = new HashMap();
    if (value.getMetaObject().toString().equals(ScriptConstants.DICT_CLASS_NAME)) {
      Value keys = value.getMember(ScriptConstants.DICT_KEYS_FUNCTION).execute();
      for (int i = 0; i < keys.getArraySize(); i++) {
        String dictKey = keys.getArrayElement(i).asString();
        Value dictValue = value.getMember(ScriptConstants.DICT_GET_FUNCTION).execute(dictKey);
        if (dictValue.getMetaObject().toString().equals(ScriptConstants.DICT_CLASS_NAME)) {
          map.put(dictKey, fromDictToMap(dictValue));
        } else if (value.getMetaObject().toString().equals(ScriptConstants.LIST_CLASS_NAME)) {
          map.put(dictKey, fromPythonListToList(dictValue));
        } else {
          map.put(dictKey, toObject(dictValue));
        }
      }
    }
    return map;
  }

  @SuppressWarnings("unchecked")
  private List fromPythonListToList(Value value) {
    List list = new ArrayList();
    if (value.getMetaObject().toString().equals(ScriptConstants.LIST_CLASS_NAME)) {
      for (int i = 0; i < value.getArraySize(); i++) {
        Value arrayElement = value.getArrayElement(i);
        if (arrayElement.getMetaObject().toString().equals(ScriptConstants.DICT_CLASS_NAME)) {
          list.add(fromDictToMap(arrayElement));
        } else if (arrayElement.getMetaObject().toString()
            .equals(ScriptConstants.LIST_CLASS_NAME)) {
          list.add(fromPythonListToList(arrayElement));
        } else {
          list.add(toObject(arrayElement));
        }
      }
    }
    return list;
  }


  @SuppressWarnings("unchecked")
  private Map fromJsObjectToMap(Value value) {
    if (value.isNull()) {
      return null;
    }
    Map map = new HashMap();
    value.getMemberKeys().forEach(key -> {
      Value member = value.getMember(key);
      if (member.hasArrayElements() && !member.isHostObject()) {
        map.put(key, fromJsArrayToList(member));
      } else if (member.hasMembers() && !member.isHostObject()) {
        map.put(key, fromJsObjectToMap(member));
      } else {
        map.put(key, toObject(member));
      }
    });
    return map;
  }

  @SuppressWarnings("unchecked")
  private List fromJsArrayToList(Value value) {
    List list = new ArrayList();
    for (int i = 0; i < value.getArraySize(); ++i) {
      Value arrayElement = value.getArrayElement(i);
      if (arrayElement.hasArrayElements() && !arrayElement.isHostObject()) {
        list.add(fromJsArrayToList(arrayElement));
      } else if (arrayElement.hasMembers() && !arrayElement.isHostObject()) {
        list.add(fromJsObjectToMap(arrayElement));
      } else {
        list.add(toObject(arrayElement));
      }
    }
    return list;
  }

  private Map toJavaMap(Value valueMap) {
    return valueMap.as(Map.class);
  }

  private Object toObject(Value value) {
    if (value.isHostObject()) {
      return value.asHostObject();
    }
    if (value.isProxyObject()) {
      return value.asProxyObject();
    }
    if (value.isNumber()) {
      try {
        return NumberFormat.getInstance().parse(value.toString());
      } catch (ParseException e) {
        throw new RuntimeException("Error while converting js number to java number", e);
      }
    }
    if (value.isException()) {
      return value.throwException();
    }
    if (value.isBoolean()) {
      return value.asBoolean();
    }
    if (value.isDate()) {
      return value.asDate();
    }
    if (value.isTime()) {
      return value.asTime();
    }
    if (value.isDuration()) {
      return value.asDuration();
    }
    if (value.isNull()) {
      return null;
    }
    if (value.isString()) {
      return value.asString();
    }
    if (value.hasArrayElements()) {
      return value.as(List.class);
    }
    return value.as(Map.class);
  }

}
