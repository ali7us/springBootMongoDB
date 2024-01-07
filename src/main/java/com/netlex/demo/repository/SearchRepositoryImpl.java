package com.netlex.demo.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.stereotype.Component;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.netlex.demo.model.GeologicalClasses;
import com.netlex.demo.model.Sections;

@Component
public class SearchRepositoryImpl implements SearchRepository {

	private static final Logger LOGGER = LogManager.getLogger(SearchRepositoryImpl.class);
	
	@Autowired
	MongoClient mongoClient;

	@Autowired
	MongoConverter mongoConverter;

	/**
	 * read data from DB based on code provided in geologicalclasses collection
	 */
	@Override
	public List<Sections> findByCode(String code) 
	{
		LOGGER.info("Finding data from Database... ");	
		
		MongoDatabase database = mongoClient.getDatabase("netlex");
		MongoCollection<Document> collection = database.getCollection("sections");
		AggregateIterable<Document> result = collection.aggregate(Arrays.asList(new Document("$search", 
			    new Document("text", 
			    new Document("query", code)
			                .append("path", "geologicalClasses.code"))), 
			    new Document("$sort", 
			    new Document("field1", 1L))));
		
		return writeExcel(result);
	}
	
	/**
	 * read all data from DB in geologicalclasses collection
	 */
	@Override
	public List<Sections> findAll() {
		LOGGER.info("Finding data from Database... ");	
		
		MongoDatabase database = mongoClient.getDatabase("netlex");
		MongoCollection<Document> collection = database.getCollection("sections");
		FindIterable<Document> result = collection.find();
		return writeExcel(result);
	}
	
	/**
	 * Write excel file
	 * overloaded method.
	 * @param result
	 * @return
	 */
	private List<Sections> writeExcel(AggregateIterable<Document> result) 
	{
		final List<Sections> sections = new ArrayList<>();
		
		result.forEach(doc -> {
			sections.add(mongoConverter.read(Sections.class, doc));
		});
		
		Iterator<Document> sec = result.iterator();
		List<Sections> newSections = new ArrayList<>();
		
		while(sec.hasNext()) { 
			Document doc = sec.next();
			List<Document> dd = (List<Document>) doc.get("geologicalClasses");
			for(Sections section : sections) {
				List<GeologicalClasses> geoList = new ArrayList<>();
				if(section.getName().equalsIgnoreCase((String) doc.get("name"))) {
					for(int i = 0; i<dd.size(); i++) {
						if(section.getName().equalsIgnoreCase((String) doc.get("name"))) {
							Document d1 = dd.get(i); 
							GeologicalClasses geologicalClasses = new GeologicalClasses();
							geologicalClasses.setCode(d1.get("code").toString());
							geologicalClasses.setName(d1.get("name").toString());
							geoList.add(geologicalClasses);
						} else {
							break;
						}
					 }
				}
			 if(geoList != null && !geoList.isEmpty()) {
				 section.setGeologicalClasses(geoList);
				 newSections.add(section);
				 
			 }
			}
		}
		
		return newSections;
	}

	/**
	 * write Excel file. 
	 * overloaded method.
	 * @param result
	 * @return
	 */
	private List<Sections> writeExcel(FindIterable<Document> result) 
	{
		final List<Sections> sections = new ArrayList<>();
		
		result.forEach(doc -> {
			sections.add(mongoConverter.read(Sections.class, doc));
		});
		
		Iterator<Document> sec = result.iterator();
		List<Sections> newSections = new ArrayList<>();
		
		while(sec.hasNext()) { 
			Document doc = sec.next();
			List<Document> dd = (List<Document>) doc.get("geologicalClasses");
			if(dd != null) {
				for(Sections section : sections) {
					List<GeologicalClasses> geoList = new ArrayList<>();
					if(section.getName().equalsIgnoreCase((String) doc.get("name"))) {
						for(int i = 0; i<dd.size(); i++) {
							if(section.getName().equalsIgnoreCase((String) doc.get("name"))) {
								Document d1 = dd.get(i); 
								GeologicalClasses geologicalClasses = new GeologicalClasses();
								geologicalClasses.setCode(d1.get("code").toString());
								geologicalClasses.setName(d1.get("name").toString());
								geoList.add(geologicalClasses);
							} else {
								break;
							}
						 }
					}
				 if(geoList != null && !geoList.isEmpty()) {
					 section.setGeologicalClasses(geoList);
					 newSections.add(section);
					 
				 }
				}
			}
			
		}
		
		return newSections;
	}
	
}
