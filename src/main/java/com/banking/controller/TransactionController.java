package com.banking.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banking.model.Transaction;
import com.banking.model.User;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;

@RestController
public class TransactionController {
	private final UserRepository userRepository;
	private final TransactionRepository transactionRepository;

	public TransactionController(UserRepository userRepository, TransactionRepository transactionRepository) {
		this.userRepository = userRepository;
		this.transactionRepository = transactionRepository;
	}

	@GetMapping("/balance")
	public double getBalance(@RequestParam("username") String username) {
		User user = userRepository.findByUsername(username);
		List<Transaction> transactions = transactionRepository.findByUser(user);

		double balance = 0.0;
		for (Transaction transaction : transactions) {
			if (transaction.getType().equals("Deposit")) {
				balance += transaction.getAmount();
			} else if (transaction.getType().equals("Withdrawal")) {
				balance -= transaction.getAmount();
			}
		}

		return balance;
	}

	@GetMapping("/transactions")
	public List<Transaction> getTransactions(@RequestParam("username") String username) {
		User user = userRepository.findByUsername(username);
		return transactionRepository.findByUser(user);
	}

	@PostMapping("/deposit")
	public ResponseEntity<?> deposit(@RequestParam("username") String username, @RequestParam("amount") double amount) {
		User user = userRepository.findByUsername(username);

		Transaction transaction = new Transaction();
		transaction.setType("Deposit");
		transaction.setAmount(amount);
		transaction.setTimestamp(LocalDateTime.now());
		transaction.setUser(user);

		transactionRepository.save(transaction);

		return ResponseEntity.ok("Deposit successful.");
	}

	@PostMapping("/withdraw")
	public ResponseEntity<?> withdraw(@RequestParam("username") String username,
			@RequestParam("amount") double amount) {
		User user = userRepository.findByUsername(username);
		double balance = getBalance(username);

		if (balance >= amount) {
			Transaction transaction = new Transaction();
			transaction.setType("Withdrawal");
			transaction.setAmount(amount);
			transaction.setTimestamp(LocalDateTime.now());
			transaction.setUser(user);

			transactionRepository.save(transaction);

			return ResponseEntity.ok("Withdrawal successful.");
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient balance.");
		}
	}
}
