package com.booklibrary.booklibrary.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.booklibrary.booklibrary.entity.Book;
import com.booklibrary.booklibrary.entity.Loan;
import com.booklibrary.booklibrary.entity.LoanStatus;
import com.booklibrary.booklibrary.entity.Member;
import com.booklibrary.booklibrary.repository.BookRepository;
import com.booklibrary.booklibrary.repository.LoanRepository;
import com.booklibrary.booklibrary.repository.MemberRepository;

import jakarta.transaction.Transactional;

@Service
public class LoanService {
  private final LoanRepository loanRepository;
  private final MemberRepository memberRepository;
  private final BookRepository bookRepository;

  public LoanService(LoanRepository loanRepository, BookRepository bookRepository, MemberRepository memberRepository) {
    this.loanRepository = loanRepository;
    this.bookRepository = bookRepository;
    this.memberRepository = memberRepository;
  }

  public Page<Loan> getAllLoans(Pageable pageable) {
    return loanRepository.findAll(pageable);
  }

  public Loan getLoanById(Long id) {
    return loanRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Loan not found with id " + id));
  }

  @Transactional
  public Loan createLoan(Loan loanRequest) {
    Book book = bookRepository.findById(loanRequest.getBook().getId())
        .orElseThrow(() -> new RuntimeException("Book not found with id " + loanRequest.getBook().getId()));

    Member member = memberRepository.findById(loanRequest.getMember().getId())
        .orElseThrow(() -> new RuntimeException("Member not found with id " + loanRequest.getMember().getId()));

    if (book.getStock() <= 0) {
      throw new RuntimeException("Book is out of stock");
    }

    book.setStock(book.getStock() - 1);
    bookRepository.save(book);

    Loan loan = new Loan();
    loan.setBook(book);
    loan.setMember(member);
    loan.setBorrowDate(LocalDate.now());
    loan.setDueDate(LocalDate.now().plusDays(14)); // Assuming a 2-week borrowing period
    loan.setStatus(LoanStatus.BORROWED);

    return loanRepository.save(loan);
  }

  @Transactional
  public Loan returnLoan(Long id) {
    Loan loan = getLoanById(id);

    if (loan.getStatus() == LoanStatus.RETURNED) {
      throw new RuntimeException("Loan with ID " + id + " has already been returned");
    }

    loan.setReturnDate(LocalDate.now());
    loan.setStatus(LoanStatus.RETURNED);

    Book book = loan.getBook();
    book.setStock(book.getStock() + 1);
    bookRepository.save(book);

    return loanRepository.save(loan);
  }

  public List<Loan> getLoansByStatus(LoanStatus status) {
    return loanRepository.findByStatus(status);
  }

  public List<Loan> getLoansByMemberId(Long memberId) {
    return loanRepository.findByMemberId(memberId);
  }

  public List<Loan> getOverdueLoans() {
    LocalDate currentDate = LocalDate.now();
    return loanRepository.findOverdueLoans(currentDate);
  }
}
