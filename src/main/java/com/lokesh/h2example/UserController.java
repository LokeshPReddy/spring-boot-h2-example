package com.lokesh.h2example;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

	@Autowired
	UserRepository userRepository;
	
	@GetMapping("getAllUsers")
	public List<User> getAllusers(){
		return userRepository.findAll();
	}
	
	@PostMapping("saveUser")
	public User saveUser(@RequestBody User user) {
		return userRepository.save(user);
	}
	
	@GetMapping("getUserByid")
	public User getUserById(@RequestParam int id) {
		return userRepository.findById(id).get();
	}
}
