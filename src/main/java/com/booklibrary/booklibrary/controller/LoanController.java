package com.booklibrary.booklibrary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.booklibrary.booklibrary.entity.Loan;
import com.booklibrary.booklibrary.service.LoanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Loan Controller", description = "APIs for managing loans in the library")
@RestController
@RequestMapping("/api/loans")
public class LoanController {
  private final LoanService loanService;

  public LoanController(LoanService loanService) {
    this.loanService = loanService;
  }

  @Operation(summary = "Get all loans", description = "Retrieve a list of all loans in the library")
  @GetMapping
  public List<Loan> getAllLoans() {
    return loanService.getAllLoans();
  }

  @Operation(summary = "Get loan by ID", description = "Retrieve a loan by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Loan found"),
      @ApiResponse(responseCode = "404", description = "Loan not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
    return ResponseEntity.ok(loanService.getLoanById(id));
  }

  @Operation(summary = "Borrow a book", description = "Create a new loan for a member borrowing a book")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Loan created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid loan data or book out of stock"),
      @ApiResponse(responseCode = "404", description = "Book or member not found")
  })
  @PostMapping
  public ResponseEntity<Loan> createLoan(@Valid @RequestBody Loan loan) {
    Loan savedLoan = loanService.createLoan(loan);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedLoan);
  }

  @Operation(summary = "Return a book", description = "Update the loan status to returned and increase the book stock")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Book returned successfully"),
      @ApiResponse(responseCode = "404", description = "Loan not found"),
      @ApiResponse(responseCode = "400", description = "Loan has already been returned")
  })
  @PutMapping("/{id}/return")
  public ResponseEntity<Loan> returnLoan(@PathVariable Long id) {
    return ResponseEntity.ok(loanService.returnLoan(id));
  }
}
