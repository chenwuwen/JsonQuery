package com.kanyun.sql.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Linq4j;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 元素迭代器,定义数据的输出
 */
public class JsonEnumerator implements Enumerator<Object[]> {

	private Enumerator<LinkedHashMap<String, Object>> enumerator;



	public JsonEnumerator(File file) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

		List<LinkedHashMap<String, Object>> list;
		try {
			list = mapper.readValue(file, new TypeReference<List<LinkedHashMap<String, Object>>>() {

			});
			System.out.println("records count " + list.size());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		enumerator = Linq4j.enumerator(list);
	}

	@Override
	public Object[] current() {
		LinkedHashMap<String, Object> current = enumerator.current();


		return current.values().toArray();
	}

	@Override
	public boolean moveNext() {
		return enumerator.moveNext();
	}

	@Override
	public void reset() {
		enumerator.reset();
	}

	@Override
	public void close() {
		enumerator.close();
	}

}
