package com.netlex.demo.dto;

import java.util.ArrayList;
import java.util.List;

import com.netlex.demo.model.GeologicalClasses;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="setion document")
public class SectionDTO {

	@ApiModelProperty(position = 0, value="A required name field for the section model", example="secion-1", required= true)
	private String name;
	
	@ApiModelProperty(position = 1, value="GeologicalClasses object", example=" [ "
			+ "{ name: Geo class 11, code: GC11}, "
			+ "{ name: Geo class 12, code: GC12} "
			+ "]")
	private List<GeologicalClasses> geologicalClasses = new ArrayList<>();
	
	public SectionDTO() {
		super();
	}

	public SectionDTO(String name, List<GeologicalClasses> geologicalClasses) {
		super();
		this.name = name;
		this.geologicalClasses = geologicalClasses;
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
