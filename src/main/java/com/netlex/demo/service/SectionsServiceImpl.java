package com.netlex.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netlex.demo.model.Sections;
import com.netlex.demo.repository.SectionsRepository;

@Service
public class SectionsServiceImpl implements SectionsService {

	@Autowired
	private SectionsRepository sectionsRepository;
	
	public Optional<Sections> findById(String code) {
		return sectionsRepository.findById(code);
	}
	
	public Sections save(Sections section) {
		return sectionsRepository.save(section);
	}
}
