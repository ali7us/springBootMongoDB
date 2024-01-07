package com.netlex.demo.service;

import java.util.Optional;

import com.netlex.demo.model.Sections;

public interface SectionsService {

	public Optional<Sections> findById(String code);
	
	public Sections save(Sections section);
}
