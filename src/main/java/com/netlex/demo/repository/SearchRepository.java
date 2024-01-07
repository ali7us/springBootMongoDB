package com.netlex.demo.repository;

import java.util.List;

import com.netlex.demo.model.Sections;

public interface SearchRepository {

	List<Sections> findByCode(String code);
	List<Sections> findAll();
}
