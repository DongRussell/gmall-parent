package com.atguigu.gmall.search;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

//@SpringBootTest
class GmallSearchApplicationTests {

	@Autowired
	JestClient jestClient;
	@Test
	public void contextLoads()  {
		Search build = new Search.Builder("").addIndex("product").addType("info").build();
		SearchResult execute = null;
		try {
			execute = jestClient.execute(build);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(execute.getTotal());

	}

	@Test
	public void testSearchSource(){
		SearchSourceBuilder builder = new SearchSourceBuilder();
		builder.query();


		String s = builder.toString();
		System.out.println(s);
	}



}
