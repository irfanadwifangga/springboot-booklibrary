package com.booklibrary.booklibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

  @Mock
  private LoanRepository loanRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private LoanService loanService;

  private Book sampleBook(int stock) {
    Book book = new Book();
    book.setId(1L);
    book.setTitle("Dune");
    book.setStock(stock);
    return book;
  }

  private Member sampleMember() {
    Member member = new Member();
    member.setId(1L);
    member.setName("John Doe");
    return member;
  }

  private Loan borrowedLoan(Book book, Member member) {
    Loan loan = new Loan();
    loan.setId(1L);
    loan.setBook(book);
    loan.setMember(member);
    loan.setBorrowDate(LocalDate.now().minusDays(1));
    loan.setDueDate(LocalDate.now().plusDays(13));
    loan.setStatus(LoanStatus.BORROWED);
    return loan;
  }

  @Test
  void createLoan_whenStockAvailable_decreasesStockAndCreatesLoan() {
    Book book = sampleBook(5);
    Member member = sampleMember();

    LoanRequest request = new LoanRequest();
    request.setBookId(1L);
    request.setMemberId(1L);

    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
    when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
      Loan saved = invocation.getArgument(0);
      saved.setId(10L);
      return saved;
    });

    LoanResponse result = loanService.createLoan(request);

    assertEquals(10L, result.getId());
    assertEquals(LoanStatus.BORROWED, result.getStatus());
    assertEquals(4, book.getStock());
    verify(bookRepository).save(book);
  }

  @Test
  void createLoan_whenBookOutOfStock_throwsBadRequestExceptionAndNeverSavesLoan() {
    Book book = sampleBook(0);
    Member member = sampleMember();

    LoanRequest request = new LoanRequest();
    request.setBookId(1L);
    request.setMemberId(1L);

    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

    assertThrows(BadRequestException.class, () -> loanService.createLoan(request));
    verify(loanRepository, never()).save(any(Loan.class));
  }

  @Test
  void createLoan_whenBookNotFound_throwsResourceNotFoundException() {
    LoanRequest request = new LoanRequest();
    request.setBookId(99L);
    request.setMemberId(1L);

    when(bookRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> loanService.createLoan(request));
  }

  @Test
  void createLoan_whenMemberNotFound_throwsResourceNotFoundException() {
    Book book = sampleBook(5);

    LoanRequest request = new LoanRequest();
    request.setBookId(1L);
    request.setMemberId(99L);

    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(memberRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> loanService.createLoan(request));
  }

  @Test
  void returnLoan_whenBorrowed_setsReturnDateAndStatusAndIncreasesStock() {
    Book book = sampleBook(4);
    Member member = sampleMember();
    Loan loan = borrowedLoan(book, member);

    when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

    LoanResponse result = loanService.returnLoan(1L);

    ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
    verify(loanRepository).save(loanCaptor.capture());
    Loan savedLoan = loanCaptor.getValue();

    assertEquals(LoanStatus.RETURNED, savedLoan.getStatus());
    assertNotNull(savedLoan.getReturnDate());
    assertEquals(LoanStatus.RETURNED, result.getStatus());
    assertEquals(5, book.getStock());
    verify(bookRepository).save(book);
  }

  @Test
  void returnLoan_whenAlreadyReturned_throwsBadRequestException() {
    Book book = sampleBook(4);
    Member member = sampleMember();
    Loan loan = borrowedLoan(book, member);
    loan.setStatus(LoanStatus.RETURNED);
    loan.setReturnDate(LocalDate.now());

    when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

    assertThrows(BadRequestException.class, () -> loanService.returnLoan(1L));
    verify(loanRepository, never()).save(any(Loan.class));
  }

  @Test
  void getLoanById_whenExists_returnsLoanResponse() {
    Book book = sampleBook(4);
    Member member = sampleMember();
    Loan loan = borrowedLoan(book, member);

    when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

    LoanResponse result = loanService.getLoanById(1L);

    assertEquals(1L, result.getId());
    assertEquals(1L, result.getBookId());
    assertEquals(1L, result.getMemberId());
    assertEquals(LoanStatus.BORROWED, result.getStatus());
  }

  @Test
  void getLoanById_whenNotFound_throwsResourceNotFoundException() {
    when(loanRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> loanService.getLoanById(99L));
  }

  @Test
  void getLoansByStatus_returnsMappedList() {
    Book book = sampleBook(4);
    Member member = sampleMember();
    Loan loan = borrowedLoan(book, member);

    when(loanRepository.findByStatus(LoanStatus.BORROWED)).thenReturn(List.of(loan));

    List<LoanResponse> result = loanService.getLoansByStatus(LoanStatus.BORROWED);

    assertEquals(1, result.size());
    assertEquals(LoanStatus.BORROWED, result.get(0).getStatus());
  }

  @Test
  void getLoansByMemberId_returnsMappedList() {
    Book book = sampleBook(4);
    Member member = sampleMember();
    Loan loan = borrowedLoan(book, member);

    when(loanRepository.findByMemberId(1L)).thenReturn(List.of(loan));

    List<LoanResponse> result = loanService.getLoansByMemberId(1L);

    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).getMemberId());
  }

  @Test
  void getOverdueLoans_returnsMappedList() {
    Book book = sampleBook(4);
    Member member = sampleMember();
    Loan loan = borrowedLoan(book, member);

    when(loanRepository.findOverdueLoans(any(LocalDate.class))).thenReturn(List.of(loan));

    List<LoanResponse> result = loanService.getOverdueLoans();

    assertEquals(1, result.size());
  }
}
