package com.vancl.mongodb.simple;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import com.vancl.mongodb.BaseTest;
import com.vancl.mongodb.model.Account;
import com.vancl.mongodb.model.Person;
import com.vancl.mongodb.model.User;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.mongodb.MongoCollectionUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class SimpleMongoTest extends BaseTest {

	private final Logger logger = Logger.getLogger(getClass());

	@Resource(name = "mongoTemplate", type = MongoTemplate.class)
	MongoTemplate mongoTemplate;

	@Before
	public void setUp() {
		// if (mongoTemplate.getCollectionNames().contains("MyNewCollection")) {
		// mongoTemplate.dropCollection("MyNewCollection");
		// }
		//
		// String collectionName = MongoCollectionUtils.getPreferredCollectionName(Person.class);
		// if (mongoTemplate.getCollectionNames().contains(collectionName)) {
		// mongoTemplate.dropCollection(collectionName);
		// }
		// mongoTemplate.createCollection(collectionName);
		// String userColl = MongoCollectionUtils.getPreferredCollectionName(User.class);
		// if (mongoTemplate.getCollectionNames().contains(userColl)) {
		// mongoTemplate.dropCollection(userColl);
		// }
		// mongoTemplate.createCollection(userColl);
	}

	// @Test
	public void createAndDropCollection() {
		assertFalse(mongoTemplate.getCollectionNames().contains("MyNewCollection"));

		/* create a new collection */
		DBCollection collection = null;
		if (!mongoTemplate.getCollectionNames().contains("MyNewCollection")) {
			collection = mongoTemplate.createCollection("MyNewCollection");
		}

		assertNotNull(collection);
		assertTrue(mongoTemplate.getCollectionNames().contains("MyNewCollection"));

		mongoTemplate.dropCollection("MyNewCollection");

		assertFalse(mongoTemplate.getCollectionNames().contains("MyNewCollection"));
	}

	// @Test
	public void createAnIndex() {
		String collectionName = MongoCollectionUtils.getPreferredCollectionName(Person.class);
		if (!mongoTemplate.getCollectionNames().contains(collectionName)) {
			mongoTemplate.createCollection(collectionName);
		}
		mongoTemplate.ensureIndex(new Index().on("name", Order.ASCENDING), collectionName);
	}

	// @Test
	public void saveAndRetrieveDocuments() {
		String collectionName = MongoCollectionUtils.getPreferredCollectionName(Person.class);
		List<Person> list = new ArrayList<Person>();
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			Person p = new Person("Bob" + i, i);
			list.add(p);
		}
		mongoTemplate.insert(list, Person.class);
		list.clear();
		long end = System.currentTimeMillis();
		System.out.println("user time by seconds:" + (end - start) / 1000);
		System.out.println(mongoTemplate.getCollection(collectionName).count());
		assertEquals(1000, mongoTemplate.getCollection(collectionName).count());
		Person qp = mongoTemplate.findOne(new Query(new Criteria("age").is(500)), Person.class);
		assertNotNull(qp);
		System.out.println(qp.getName());
		List<Person> qList = mongoTemplate.find(new Query(new Criteria("age").is(50)), Person.class);
		assertNotNull(qList);
		for (Person p : qList) {
			System.out.println(p.getName());
		}
	}

	// @Test
	public void queryingForDocuments() {
		String collectionName = MongoCollectionUtils.getPreferredCollectionName(Person.class);

		Person p1 = new Person("Bob", 33);
		p1.addAccount(new Account("198-998-2188", Account.Type.SAVINGS, 123.55d));
		mongoTemplate.insert(p1);
		Person p2 = new Person("Mary", 25);
		p2.addAccount(new Account("860-98107-681", Account.Type.CHECKING, 400.51d));
		mongoTemplate.insert(p2);
		Person p3 = new Person("Chris", 68);
		p3.addAccount(new Account("761-002-8901", Account.Type.SAVINGS, 10531.00d));
		mongoTemplate.insert(p3);
		Person p4 = new Person("Janet", 33);
		p4.addAccount(new Account("719-100-0019", Account.Type.SAVINGS, 1209.10d));
		mongoTemplate.insert(p4);

		assertEquals(4, mongoTemplate.getCollection(collectionName).count());

		List<Person> result = mongoTemplate.find(new Query(new Criteria("age").lt(50).and("accounts.balance").gt(1000.00d)), Person.class);

		System.out.println(result);
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	// @Test
	public void updatingDocuments() {
		String collectionName = MongoCollectionUtils.getPreferredCollectionName(Person.class);

		Person p1 = new Person("Bob", 33);
		p1.addAccount(new Account("198-998-2188", Account.Type.SAVINGS, 123.55d));
		mongoTemplate.insert(p1);
		Person p2 = new Person("Mary", 25);
		p2.addAccount(new Account("860-98107-681", Account.Type.CHECKING, 400.51d));
		mongoTemplate.insert(p2);
		Person p3 = new Person("Chris", 68);
		p3.addAccount(new Account("761-002-8901", Account.Type.SAVINGS, 10531.00d));
		mongoTemplate.insert(p3);
		Person p4 = new Person("Janet", 33);
		p4.addAccount(new Account("719-100-0019", Account.Type.SAVINGS, 1209.10d));
		mongoTemplate.insert(p4);

		assertEquals(4, mongoTemplate.getCollection(collectionName).count());

		WriteResult wr = mongoTemplate.updateMulti(new Query(new Criteria("accounts.accountType").is(Account.Type.SAVINGS)),
				new Update().inc("accounts.$.balance", 50.00), Person.class);

		assertNotNull(wr);
		assertEquals(3, wr.getN());
	}

	// @Test
	public void testFindAllPerson() {
		String collectionName = MongoCollectionUtils.getPreferredCollectionName(Person.class);
		System.out.println(mongoTemplate.getCollection(collectionName).count());
		List<Person> qList = mongoTemplate.findAll(Person.class);
		assertNotNull(qList);
		for (Person p : qList) {
			StringBuilder sb = new StringBuilder("");
			System.out.println(sb.append("id:").append(p.getId()).append(",name:").append(p.getName()).append(",age:").append(p.getAge()));
		}
	}

	@Test
	public void testFindAllUser() {
		String collectionName = MongoCollectionUtils.getPreferredCollectionName(User.class);
		System.out.println(mongoTemplate.getCollection(collectionName).count());
		List<User> uList = mongoTemplate.find(new Query(new Criteria("userId").is("10001284")), User.class);
		assertNotNull(uList);
		for (User u : uList) {
			StringBuilder sb = new StringBuilder("");
			System.out.println(sb.append("userId:").append(u.getUserId()).append(",username:").append(u.getUsername()).append(",lastAddress:")
					.append(u.getLastAddress()).append(",postcode:").append(u.getPostcode()).append(",serialNumber:").append(u.getSerialNumber()));
		}
	}

	// @Test
	public void insertUser() {
		try {
			List<User> list = new ArrayList<User>();
			long start = System.currentTimeMillis();
			Database db = Database.open(new File("E:\\TDDOWNLOAD\\6月正刊发送库.accdb"), true, true);
			Table table = db.getTable("2011年6月正刊");
			int i = 1;
			for (Map<String, Object> row : table) {
				try {
					String userId = (String) (row.get("userid"));
					String name = (String) (row.get("姓名"));
					String lastAddress = (String) (row.get("最后发送地址"));
					String postcode = (String) (row.get("邮编"));
					String serialNumber = (String) (row.get("编码"));
					logger.info("index:" + (i++) + ",userId:" + userId + ",name:" + name + ",serialNumber:" + serialNumber + ",postcode:" + postcode
							+ ",lastAddress:" + lastAddress);
					User u = new User();
					u.setUserId(userId);
					u.setUsername(name);
					u.setLastAddress(lastAddress);
					u.setPostcode(postcode);
					u.setSerialNumber(serialNumber);
					list.add(u);
					if (list.size() == 5000) {
						mongoTemplate.insert(list, User.class);
						list.clear();
					}
				} catch (Exception e) {
					continue;
				}
			}
			if (list.size() > 0) {
				mongoTemplate.insert(list, User.class);
				list.clear();
			}
			long end = System.currentTimeMillis();
			System.out.println("user time by seconds:" + (end - start) / 1000);
			String collectionName = MongoCollectionUtils.getPreferredCollectionName(User.class);
			System.out.println(mongoTemplate.getCollection(collectionName).count());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
