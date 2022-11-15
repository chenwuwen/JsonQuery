package com.kanyun.sql.core;

import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;

public class JsonTable extends AbstractTable implements ScannableTable {

	private final File file;

	public JsonTable(File file) {
		this.file = file;
	}

	@Override
	public RelDataType getRowType(RelDataTypeFactory typeFactory) {

		String content = null;
		try {
			content = Files.asCharSource(file, StandardCharsets.UTF_8).read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		JsonElement jsonElement = JsonParser.parseString(content);
		if (jsonElement.isJsonArray()) {
			JsonElement element = jsonElement.getAsJsonArray().get(0);
			if (element.isJsonObject()) {
				ArrayList<RelDataType> realTypes = new ArrayList<>();
				ArrayList<String> fields = new ArrayList<>();
				JsonObject jsonObject = element.getAsJsonObject();
				Set<String> keys = jsonObject.keySet();
				for (String key : keys) {
					if (jsonObject.get(key).isJsonPrimitive()) {
						JsonPrimitive jsonPrimitive = jsonObject.get(key).getAsJsonPrimitive();
						if (jsonPrimitive.isBoolean()) {
							fields.add(key);
							realTypes.add(typeFactory.createJavaType(Boolean.TYPE));
							continue;
						}
						if (jsonPrimitive.isString()) {
							fields.add(key);
							realTypes.add(typeFactory.createJavaType(String.class));
							continue;
						}
						if (jsonPrimitive.isNumber()) {
							fields.add(key);
							realTypes.add(typeFactory.createJavaType(Long.TYPE));
							continue;
						}

						fields.add(key);
						realTypes.add(typeFactory.createJavaType(String.class));
					}
				}
				return typeFactory.createStructType(Pair.zip(fields, realTypes));
			}
		}

		return null;
	}

	@Override
	public Enumerable<Object[]> scan(DataContext root) {
		return new AbstractEnumerable<Object[]>() {
			@Override
			public Enumerator<Object[]> enumerator() {
				return new JsonEnumerator(file);
			}
		};
	}

}
