package com.booklibrary.booklibrary.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booklibrary.booklibrary.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
