package com.autoradio.push.test.mongo;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.autoradio.push.test.BaseTest;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoTest extends BaseTest {

	@Resource(name = "mongoTemplate", type = MongoTemplate.class)
	private MongoTemplate mongoTemplate;

	@Test
	public void testFindAll() {

		DBCollection coll = mongoTemplate.getCollection("basis_channelSource");
		DBObject obj = new BasicDBObject();
		obj.put("udid", new BasicDBObject("$exists", true));
		DBCursor cursor = coll.find(obj);
		while (cursor.hasNext()) {
			Object udid = cursor.next().get("udid");
			System.out.println(udid);
		}
	}
}
