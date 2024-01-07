package com.netlex.demo.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;

@ApiModel(value="setion document")
@Document(collection = "sections")
public class Sections {
	@Id
	private String id;
	
	private String name;
	
	private List<GeologicalClasses> geologicalClasses;
	
	public Sections() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<GeologicalClasses> getGeologicalClasses() {
		return geologicalClasses;
	}

	public void setGeologicalClasses(List<GeologicalClasses> geologicalClasses) {
		this.geologicalClasses = geologicalClasses;
	}

	
}
