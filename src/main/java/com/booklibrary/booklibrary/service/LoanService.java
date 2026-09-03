package com.booklibrary.booklibrary.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.booklibrary.booklibrary.dto.request.LoanRequest;
import com.booklibrary.booklibrary.dto.response.LoanResponse;
import com.booklibrary.booklibrary.entity.Book;
import com.booklibrary.booklibrary.entity.Loan;
import com.booklibrary.booklibrary.entity.LoanStatus;
import com.booklibrary.booklibrary.entity.Member;
import com.booklibrary.booklibrary.exception.BadRequestException;
import com.booklibrary.booklibrary.exception.ResourceNotFoundException;
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

  public Page<LoanResponse> getAllLoans(Pageable pageable) {
    return loanRepository.findAll(pageable).map(this::toResponse);
  }

  public LoanResponse getLoanById(Long id) {
    return loanRepository.findById(id).map(this::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id " + id));
  }

  @Transactional
  public LoanResponse createLoan(LoanRequest request) {
    Book book = bookRepository.findById(request.getBookId())
        .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + request.getBookId()));

    Member member = memberRepository.findById(request.getMemberId())
        .orElseThrow(() -> new ResourceNotFoundException("Member not found with id " + request.getMemberId()));

    if (book.getStock() <= 0) {
      throw new BadRequestException("Book is out of stock");
    }

    book.setStock(book.getStock() - 1);
    bookRepository.save(book);

    Loan loan = new Loan();
    loan.setBook(book);
    loan.setMember(member);
    loan.setBorrowDate(LocalDate.now());
    loan.setDueDate(LocalDate.now().plusDays(14)); // Assuming a 2-week borrowing period
    loan.setStatus(LoanStatus.BORROWED);

    return toResponse(loanRepository.save(loan));
  }

  @Transactional
  public LoanResponse returnLoan(Long id) {
    Loan loan = findLoanEntity(id);

    if (loan.getStatus() == LoanStatus.RETURNED) {
      throw new BadRequestException("Loan with ID " + id + " has already been returned");
    }

    loan.setReturnDate(LocalDate.now());
    loan.setStatus(LoanStatus.RETURNED);

    Book book = bookRepository.findById(loan.getBook().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + loan.getBook().getId()));
    book.setStock(book.getStock() + 1);
    bookRepository.save(book);

    return toResponse(loanRepository.save(loan));
  }

  public List<LoanResponse> getLoansByStatus(LoanStatus status) {
    return loanRepository.findByStatus(status).stream().map(this::toResponse).collect(Collectors.toList());
  }

  public List<LoanResponse> getLoansByMemberId(Long memberId) {
    return loanRepository.findByMemberId(memberId).stream().map(this::toResponse).collect(Collectors.toList());
  }

  public List<LoanResponse> getOverdueLoans() {
    LocalDate currentDate = LocalDate.now();
    return loanRepository.findOverdueLoans(currentDate).stream().map(this::toResponse).collect(Collectors.toList());
  }

  private LoanResponse toResponse(Loan loan) {
    LoanResponse response = new LoanResponse();
    response.setId(loan.getId());
    response.setBookId(loan.getBook().getId());
    response.setMemberId(loan.getMember().getId());
    response.setLoanDate(loan.getBorrowDate());
    response.setDueDate(loan.getDueDate());
    response.setReturnDate(loan.getReturnDate());
    response.setStatus(loan.getStatus());
    return response;
  }

  private Loan findLoanEntity(Long id) {
    return loanRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id " + id));
  }
}
