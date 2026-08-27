package com.booklibrary.booklibrary.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booklibrary.booklibrary.entity.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}
