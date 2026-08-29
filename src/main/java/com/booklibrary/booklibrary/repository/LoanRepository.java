package com.booklibrary.booklibrary.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.booklibrary.booklibrary.entity.Loan;
import com.booklibrary.booklibrary.entity.LoanStatus;

public interface LoanRepository extends JpaRepository<Loan, Long> {
  List<Loan> findByStatus(LoanStatus status);

  List<Loan> findByMemberId(Long memberId);

  @Query("SELECT l FROM Loan l WHERE l.status = 'BORROWED' AND l.dueDate < :currentDate")
  List<Loan> findOverdueLoans(@Param("currentDate") LocalDate currentDate);
}
