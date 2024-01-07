package com.netlex.demo.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.netlex.demo.model.Sections;

public interface SectionsRepository extends MongoRepository<Sections, String>{
	public Optional<Sections> findById(String code);
}
